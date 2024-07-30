package io.hk.rpc.consumer.common.manager;

import io.hk.rpc.constants.RpcConstants;
import io.hk.rpc.consumer.common.RpcConsumer;
import io.hk.rpc.consumer.common.cache.ConsumerChannelCache;
import io.hk.rpc.consumer.common.handler.RpcConsumerHandler;
import io.hk.rpc.consumer.common.helper.RpcConsumerHandlerHelper;
import io.hk.rpc.protocol.RpcProtocol;
import io.hk.rpc.protocol.enumeration.RpcType;
import io.hk.rpc.protocol.header.RpcHeader;
import io.hk.rpc.protocol.header.RpcHeaderFactory;
import io.hk.rpc.protocol.meta.ServiceMeta;
import io.hk.rpc.protocol.request.RpcRequest;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * 服务消费者连接管理器
 */
public class ConsumerConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerConnectionManager.class);

    /**
     * 扫描并移除不活跃的连接
     */
    public static void scanNotActiveChannel() {
        Set<Channel> channelCache = ConsumerChannelCache.getChannelCache();
        LOGGER.info("============ scanNotActiveChannel ============ size:{}, time:{}", channelCache.size(), DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(LocalDateTime.now()));
        if (channelCache == null || channelCache.isEmpty()) {
            return;
        }
        channelCache.stream().forEach(channel -> {
            if (!channel.isOpen() || !channel.isActive()) {
                LOGGER.info("ConsumerConnectionManager. scan Not Active Channel:{}", channel.remoteAddress());
                channel.close();
                ConsumerChannelCache.remove(channel);
                // 清除 RpcConsumerHandlerHelper 缓存
                // 作业63-x
                RpcConsumerHandlerHelper.remove(channel);
            }
        });
    }

    /**
     * 发送ping消息
     */
    public static void broadcastPingMessageFromConsumer(RpcConsumer rpcConsumer) {
        Set<Channel> channelCache = ConsumerChannelCache.getChannelCache();
        LOGGER.info("============ broadcastPingMessageFromConsumer ============ size:{}, time:{}", channelCache.size(), DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(LocalDateTime.now()));
        if (channelCache == null || channelCache.isEmpty()) {
            return;
        }
//        RpcHeader header = RpcHeaderFactory.getRequestHeader(RpcConstants.SERIALIZATION_PROTOSTUFF, RpcType.HEARTBEAT_FROM_CONSUMER.getType());
//        RpcRequest rpcRequest = new RpcRequest();
//        rpcRequest.setParameters(new Object[]{RpcConstants.HEARTBEAT_PING});
//        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();
//        requestRpcProtocol.setHeader(header);
//        requestRpcProtocol.setBody(rpcRequest);
//        channelCache.stream().forEach(channel -> {
//            if (channel.isOpen() && channel.isActive()) {
//                LOGGER.info("send heartbeat message to service provider, the provider is:{}, the heartbeat message is:{}", channel.remoteAddress(), RpcConstants.HEARTBEAT_PING);
//                channel.writeAndFlush(requestRpcProtocol);
//            }
//        });

        // 作业63-x:
        RpcProtocol<RpcRequest> requestRpcProtocol = getRpcRequestRpcProtocol();
        channelCache.stream().forEach(channel -> {
            if (channel.isOpen() && channel.isActive()) {
                // 1.检查是否超过最大次数
                boolean isOverflow = ConsumerChannelCache.isWaitTimesOverflow(channel);
                if (isOverflow) {
                    // 是: 关闭通道清除缓存,并重连提供者
                    clearAndReconnectProvider(channel, rpcConsumer);
                } else {
                    // 否: 正常心跳请求,心跳后计数+1
                    sendPing(requestRpcProtocol, channel);
                }
            }
        });
    }

    /**
     * 清除channel相关的缓存资源并重新连接服务提供者
     * <p>作业63-x
     */
    private static void clearAndReconnectProvider(Channel channel, RpcConsumer rpcConsumer) {
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        String address = socketAddress.getAddress().getHostAddress();
        int port = socketAddress.getPort();

        // 1.关闭通道并清理ConsumerChannelCache
        channel.close();
        ConsumerChannelCache.remove(channel);

        // 2.通过RpcConsumer重连并获取 RpcConsumerHandler
        try {
            RpcConsumerHandlerHelper.remove(channel);
            LOGGER.info("消费者发送给提供者的心跳请求,超过 {} 次没有回复,开始重新连接提供者: {}", RpcConstants.MAX_WAITING_PONG_TIMES, channel.remoteAddress());
            RpcConsumerHandler consumerHandler = rpcConsumer.getRpcConsumerHandler(address, port);
            // 3.覆盖RpcConsumerHandlerHelper中的缓存
            ServiceMeta serviceMeta = new ServiceMeta();
            serviceMeta.setServiceAddr(address);
            serviceMeta.setServicePort(port);
            RpcConsumerHandlerHelper.put(serviceMeta, consumerHandler);
            LOGGER.info("当前RpcConsumerHandlerHelper中缓存的数量: {}", RpcConsumerHandlerHelper.size());
        } catch (InterruptedException e) {
            LOGGER.error("消费者重连提供者{} 异常", channel.remoteAddress(), e);
            e.printStackTrace();
        }
    }

    private static void sendPing(RpcProtocol<RpcRequest> requestRpcProtocol, Channel channel) {
        LOGGER.info("send heartbeat message to service provider, the provider is: {}, the heartbeat message is: {}", channel.remoteAddress(), RpcConstants.HEARTBEAT_PING);
        channel.writeAndFlush(requestRpcProtocol);
        int count = ConsumerChannelCache.increaseWaitTimes(channel);
        LOGGER.info("发送提供者:{} 的心跳请求,当前心跳响应等待:{} 次. ", channel.remoteAddress(), count);
    }

    private static RpcProtocol<RpcRequest> getRpcRequestRpcProtocol() {
        RpcHeader header = RpcHeaderFactory.getRequestHeader(RpcConstants.SERIALIZATION_PROTOSTUFF, RpcType.HEARTBEAT_FROM_CONSUMER.getType());
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setParameters(new Object[]{RpcConstants.HEARTBEAT_PING});
        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();
        requestRpcProtocol.setHeader(header);
        requestRpcProtocol.setBody(rpcRequest);
        return requestRpcProtocol;
    }

}
