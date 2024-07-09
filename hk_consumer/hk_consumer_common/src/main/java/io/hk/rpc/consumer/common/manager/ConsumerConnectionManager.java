package io.hk.rpc.consumer.common.manager;

import io.hk.rpc.constants.RpcConstants;
import io.hk.rpc.consumer.common.cache.ConsumerChannelCache;
import io.hk.rpc.protocol.RpcProtocol;
import io.hk.rpc.protocol.enumeration.RpcType;
import io.hk.rpc.protocol.header.RpcHeader;
import io.hk.rpc.protocol.header.RpcHeaderFactory;
import io.hk.rpc.protocol.request.RpcRequest;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if (channelCache == null || channelCache.isEmpty()) {
            return;
        }
        channelCache.stream().forEach(channel -> {
            if (!channel.isOpen() || !channel.isActive()) {
                LOGGER.info("scan Not Active Channel:{}", channel.remoteAddress());
                channel.close();
                ConsumerChannelCache.remove(channel);
            }
        });
    }

    /**
     * 发送ping消息
     */
    public static void broadcastPingMessageFromConsumer() {
        Set<Channel> channelCache = ConsumerChannelCache.getChannelCache();
        if (channelCache == null || channelCache.isEmpty()) {
            return;
        }
        RpcHeader header = RpcHeaderFactory.getRequestHeader(RpcConstants.SERIALIZATION_PROTOSTUFF, RpcType.HEARTBEAT_FROM_CONSUMER.getType());
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setParameters(new Object[]{RpcConstants.HEARTBEAT_PING});
        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();
        requestRpcProtocol.setHeader(header);
        requestRpcProtocol.setBody(rpcRequest);
        channelCache.stream().forEach(channel -> {
            if (channel.isOpen() && channel.isActive()) {
                LOGGER.info("send heartbeat message to service provider, the provider is:{}, the heartbeat message is:{}", channel.remoteAddress(), RpcConstants.HEARTBEAT_PING);
                channel.writeAndFlush(requestRpcProtocol);
            }
        });
    }

}
