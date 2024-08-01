package io.hk.rpc.consumer.common;

import io.hk.rpc.common.helper.RpcServiceHelper;
import io.hk.rpc.common.threadpool.ClientThreadPool;
import io.hk.rpc.constants.RpcConstants;
import io.hk.rpc.consumer.common.handler.RpcConsumerHandler;
import io.hk.rpc.consumer.common.helper.RpcConsumerHandlerHelper;
import io.hk.rpc.consumer.common.initializer.RpcConsumerInitializer;
import io.hk.rpc.consumer.common.manager.ConsumerConnectionManager;
import io.hk.rpc.loadbalancer.context.ConnectionsContext;
import io.hk.rpc.protocol.RpcProtocol;
import io.hk.rpc.protocol.meta.ServiceMeta;
import io.hk.rpc.protocol.request.RpcRequest;
import io.hk.rpc.proxy.api.consumer.Consumer;
import io.hk.rpc.proxy.api.future.RPCFuture;
import io.hk.rpc.registry.api.RegistryService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 服务消费者
 */
public class RpcConsumer implements Consumer {
    private final Logger logger = LoggerFactory.getLogger(RpcConsumer.class);

    private final Bootstrap bootstrap;

    private final EventLoopGroup eventLoopGroup;

    private static volatile RpcConsumer instance;

    /**
     * 定时任务类型的线程池:
     * 在服务消费者端会使用这个定时任务线程池向服务提供者定时发送心跳数据.
     */
    private ScheduledExecutorService executorService;
    /**
     * 心跳间隔时间,默认30秒
     */
    private int heartbeatInterval = 30000;
    /**
     * 扫描并移除空闲连接时间,默认60秒
     */
    private int scanNotActiveChannelInterval = 60000;
    /**
     * 服务订阅重试机制的 重试间隔时间
     */
    private int retryInterval = 1000;
    /**
     * 服务订阅重试机制的 重试次数
     */
    private int retryTimes = 3;

    private RpcConsumer(int heartbeatInterval, int scanNotActiveChannelInterval, int retryInterval, int retryTimes) {
        if (heartbeatInterval > 0) {
            this.heartbeatInterval = heartbeatInterval;
        }
        if (scanNotActiveChannelInterval > 0) {
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        }
        this.retryInterval = retryInterval <= 0 ? RpcConstants.DEFAULT_RETRY_INTERVAL : retryInterval;
        this.retryTimes = retryTimes <= 0 ? RpcConstants.DEFAULT_RETRY_TIMES : retryTimes;

        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new RpcConsumerInitializer(heartbeatInterval));

        this.startHeartbeat();
    }

    public static RpcConsumer getInstance(int heartbeatInterval, int scanNotActiveChannelInterval, int retryInterval, int retryTimes) {
        if (instance == null) {
            synchronized (RpcConsumer.class) {
                if (instance == null) {
                    instance = new RpcConsumer(heartbeatInterval, scanNotActiveChannelInterval, retryInterval, retryTimes);
                }
            }
        }
        return instance;
    }

    public void close() {
        RpcConsumerHandlerHelper.closeRpcClientHandler();
        eventLoopGroup.shutdownGracefully();
        ClientThreadPool.shutdown();
        executorService.shutdown();
    }

    @Override
    public RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception {
        RpcRequest request = protocol.getBody();
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getVersion(), request.getGroup());
        Object[] params = request.getParameters();
        int invokerHashCode = (params == null || params.length == 0) ? serviceKey.hashCode() : params[0].hashCode();
        // 通过服务注册中心,发现服务
//        ServiceMeta serviceMeta = registryService.discovery(serviceKey, invokerHashCode);
        ServiceMeta serviceMeta = this.getServiceMeta(registryService, serviceKey, invokerHashCode);

        if (serviceMeta != null) {
            RpcConsumerHandler handler = RpcConsumerHandlerHelper.get(serviceMeta);
            // 缓存中无RpcClientHandler
            if (handler == null) {
                handler = getRpcConsumerHandler(serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                RpcConsumerHandlerHelper.put(serviceMeta, handler);
            } else if (!handler.getChannel().isActive()) { // 缓存中存在RpcClientHandler,但不活跃
                handler.close();
                handler = getRpcConsumerHandler(serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                RpcConsumerHandlerHelper.put(serviceMeta, handler);
            }
            return handler.sendRequest(protocol, request.getAsync(), request.getOneway());
        }
        return null;
    }

    /**
     * 创建连接并返回RpcClientHandler
     */
    public RpcConsumerHandler getRpcConsumerHandler(String serviceAddress, int port) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(serviceAddress, port).sync();
        channelFuture.addListener((ChannelFutureListener) listener -> {
            if (channelFuture.isSuccess()) {
                logger.info("connect rpc server {} on port {} success.", serviceAddress, port);
                // 添加连接信息,在服务消费者端记录每个服务提供者实例的连接次数
                ServiceMeta serviceMeta = new ServiceMeta();
                serviceMeta.setServiceAddr(serviceAddress);
                serviceMeta.setServicePort(port);
                ConnectionsContext.add(serviceMeta);
            } else {
                logger.error("connect rpc server {} on port {} failed.", serviceAddress, port);
                channelFuture.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });
        return channelFuture.channel().pipeline().get(RpcConsumerHandler.class);
    }

    /**
     * 1. 通过定时任务线程池调用ConsumerConnectionManager中的scanNotActiveChannel()方法,每隔60秒扫描并移除ConsumerChannelCache中不活跃的Channel.<p>
     * 2. 通过定时任务线程池调用ConsumerConnectionManager中的broadcastPingMessageFromConsumer()方法,每隔30秒向服务提供者发送心跳数据.
     */
    private void startHeartbeat() {
        executorService = Executors.newScheduledThreadPool(2);

        executorService.scheduleAtFixedRate(() -> {
            ConsumerConnectionManager.scanNotActiveChannel();
        }, 10, scanNotActiveChannelInterval, TimeUnit.MILLISECONDS);

        executorService.scheduleAtFixedRate(() -> {
            ConsumerConnectionManager.broadcastPingMessageFromConsumer(this);
        }, 3, heartbeatInterval, TimeUnit.MILLISECONDS);
    }


    /**
     * 尝试获取服务提供者元数据, 如果获取成功, 则直接返回; 如果获取失败, 则进行重试.
     */
    private ServiceMeta getServiceMeta(RegistryService registryService, String serviceKey, int invokerHashCode) throws Exception {
        // 首次获取服务元数据信息, 如果获取成功则直接返回, 否则进行重试
        logger.info("获取服务提供者元数据...");
        ServiceMeta serviceMeta = registryService.discovery(serviceKey, invokerHashCode); // todo localIp

        // 启动重试机制
        if (serviceMeta == null) {
            for (int i = 1; i <= retryTimes; i++) {
                logger.info("获取服务提供者元数据, 第【 {} 】次重试...", i);
                serviceMeta = registryService.discovery(serviceKey, invokerHashCode); // todo localIp
                if (serviceMeta != null) {
                    break;
                }
                Thread.sleep(retryInterval);
            }
        }
        return serviceMeta;
    }

}
