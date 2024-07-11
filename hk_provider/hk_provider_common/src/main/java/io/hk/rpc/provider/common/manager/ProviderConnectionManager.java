package io.hk.rpc.provider.common.manager;

import io.hk.rpc.constants.RpcConstants;
import io.hk.rpc.protocol.RpcProtocol;
import io.hk.rpc.protocol.enumeration.RpcType;
import io.hk.rpc.protocol.header.RpcHeader;
import io.hk.rpc.protocol.header.RpcHeaderFactory;
import io.hk.rpc.protocol.response.RpcResponse;
import io.hk.rpc.provider.common.cache.ProviderChannelCache;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * 服务提供者连接管理器
 */
public class ProviderConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderConnectionManager.class);

    /**
     * 扫描并移除不活跃的连接
     */
    public static void scanNotActiveChannel() {
        Set<Channel> channelCache = ProviderChannelCache.getChannelCache();
        LOGGER.info("============ scanNotActiveChannel ============ size:{}, time:{}", channelCache.size(), DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(LocalDateTime.now()));
        if (channelCache == null || channelCache.isEmpty()) {
            return;
        }
        channelCache.stream().forEach(channel -> {
            if (!channel.isOpen() || !channel.isActive()) {
                LOGGER.info("ProviderConnectionManager. scan Not Active Channel:{}", channel.remoteAddress());
                channel.close();
                ProviderChannelCache.remove(channel);
            }
        });
    }

    /**
     * 发送ping消息
     */
    public static void broadcastPingMessageFromProvider() {
        Set<Channel> channelCache = ProviderChannelCache.getChannelCache();
        LOGGER.info("============ broadcastPingMessageFromProvider ============ size:{}, time:{}", channelCache.size(), DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(LocalDateTime.now()));
        if (channelCache == null || channelCache.isEmpty()) {
            return;
        }
        RpcHeader header = RpcHeaderFactory.getRequestHeader(RpcConstants.SERIALIZATION_PROTOSTUFF, RpcType.HEARTBEAT_FROM_PROVIDER.getType());
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setResult(RpcConstants.HEARTBEAT_PING);
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(rpcResponse);
        channelCache.stream().forEach(channel -> {
            if (channel.isOpen() && channel.isActive()) {
                LOGGER.info("send heartbeat message to service consumer, the consumer is:{}, the heartbeat message is:{}", channel.remoteAddress(), rpcResponse.getResult());
                channel.writeAndFlush(responseRpcProtocol);
            }
        });
    }

}