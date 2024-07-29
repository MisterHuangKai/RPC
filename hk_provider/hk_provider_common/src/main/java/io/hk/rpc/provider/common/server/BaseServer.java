package io.hk.rpc.provider.common.server;

import io.hk.rpc.codec.RpcDecoder;
import io.hk.rpc.codec.RpcEncoder;
import io.hk.rpc.constants.RpcConstants;
import io.hk.rpc.provider.common.handler.RpcProviderHandler;
import io.hk.rpc.provider.common.manager.ProviderConnectionManager;
import io.hk.rpc.registry.api.RegistryService;
import io.hk.rpc.registry.api.config.RegistryConfig;
import io.hk.rpc.registry.zookeeper.ZookeeperRegistryService;
import io.hk.rpc.spi.loader.ExtensionLoader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 基础服务
 */
public class BaseServer implements Server {

    private final Logger logger = LoggerFactory.getLogger(BaseServer.class);

    /**
     * 主机域名或者IP地址
     */
    protected String host = "127.0.0.1";
    /**
     * 端口号
     */
    protected int port = 27110;
    /**
     * 反射类型
     */
    private String reflectType;
    //
    protected String serverRegistryHost;
    //
    protected int serverRegistryPort;
    /**
     * 心跳间隔时间,默认30秒
     */
    private int heartbeatInterval = 30000;
    /**
     * 扫描并移除空闲连接时间,默认60秒
     */
    private int scanNotActiveChannelInterval = 60000;
    /**
     * 心跳定时任务线程池
     */
    private ScheduledExecutorService executorService;
    /**
     * 服务注册与发现
     */
    protected RegistryService registryService;

    // 存储的是实体类关系
    protected Map<String, Object> handlerMap = new HashMap<>();

    public BaseServer(String serverAddress, String registryAddress, String registryType, String registryLoadBalanceType, String reflectType, int heartbeatInterval, int scanNotActiveChannelInterval) {
        if (!StringUtils.isEmpty(serverAddress)) {
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }
        this.reflectType = reflectType;
        if(heartbeatInterval > 0){
            this.heartbeatInterval = heartbeatInterval;
        }
        if(scanNotActiveChannelInterval > 0){
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        }
        this.registryService = this.getRegistryService(registryAddress, registryType, registryLoadBalanceType);
    }

    private RegistryService getRegistryService(String registryAddress, String registryType, String registryLoadBalanceType) {
        RegistryService registryService = null;
        try {
            registryService = ExtensionLoader.getExtension(RegistryService.class, registryType);
            registryService.init(new RegistryConfig(registryAddress, registryType, registryLoadBalanceType));
        } catch (Exception e) {
            logger.error("RPC Server init error.", e);
        }
        return registryService;
    }

    /**
     * 启动定时任务,定时扫描并移除不活跃连接,定时向服务消费者发送心跳
     */
    private void startHeartbeat() {
        executorService = Executors.newScheduledThreadPool(2);

        executorService.scheduleAtFixedRate(() -> {
            ProviderConnectionManager.scanNotActiveChannel();
        }, 10, scanNotActiveChannelInterval, TimeUnit.MILLISECONDS);

        executorService.scheduleAtFixedRate(() -> {
            ProviderConnectionManager.broadcastPingMessageFromProvider();
        }, 3, heartbeatInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * IdleStateHandler:
     *     <p>除了手动写定时任务实现心跳检测机制外,借助于Netty的IdleStateHandler,也可以实现心跳检测机制。
     *     <p>Netty中的IdleStateHandler对象本质上是一个Handler处理器,配置在Netty的责任链里,当发送请求或者收到响应时,都会经过该对象处理。在双方通讯
     * 开始后该对象会创建一些空闲检测定时器,用于检测读事件(收到请求会触发读事件)和写事件(连接、发送请求会触发写事件)。当在指定的空闲时间内没有
     * 收到读事件或写事件,便会触发超时事件,然后IdleStateHandler将超时事件交给责任链里面的下一个Handler。
     */
    @Override
    public void startNettyServer() {
        // 启动心跳检测机制
        this.startHeartbeat();
        // 启动netty服务的经典代码模板
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(RpcConstants.CODEC_DECODER, new RpcDecoder())
                                    .addLast(RpcConstants.CODEC_ENCODER, new RpcEncoder())
                                    .addLast(RpcConstants.CODEC_SERVER_IDLE_HANDLER, new IdleStateHandler(0, 0, heartbeatInterval, TimeUnit.MILLISECONDS))
                                    .addLast(RpcConstants.CODEC_HANDLER, new RpcProviderHandler(reflectType, handlerMap));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            logger.info("Server started on {}:{}", host, port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("RPC Server start error", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
