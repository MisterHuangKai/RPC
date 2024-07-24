package io.hk.rpc.consumer.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.hk.rpc.constants.RpcConstants;
import io.hk.rpc.consumer.common.cache.ConsumerChannelCache;
import io.hk.rpc.consumer.common.context.RpcContext;
import io.hk.rpc.protocol.RpcProtocol;
import io.hk.rpc.protocol.enumeration.RpcStatus;
import io.hk.rpc.protocol.enumeration.RpcType;
import io.hk.rpc.protocol.header.RpcHeader;
import io.hk.rpc.protocol.request.RpcRequest;
import io.hk.rpc.protocol.response.RpcResponse;
import io.hk.rpc.proxy.api.future.RPCFuture;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC服务消费者的Handler处理类
 */
public class RpcConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    private final Logger logger = LoggerFactory.getLogger(RpcConsumerHandler.class);

    private volatile Channel channel;
    public SocketAddress remotePeer;

    // 存储 请求ID与RpcResponse协议的映射关系
    private Map<Long, RPCFuture> pendingRPC = new ConcurrentHashMap<>(); // 实现异步转同步的关键

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
        ConsumerChannelCache.add(channel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ConsumerChannelCache.remove(ctx.channel());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        ConsumerChannelCache.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    /**
     * 接收服务提供者响应的数据,并调用handlerMessage()处理数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> protocol) throws Exception {
        logger.info("服务消费者接收到的数据===>>>{}", JSONObject.toJSONString(protocol));
        if (protocol == null) {
            return;
        }
        this.handlerMessage(protocol, ctx.channel());
    }

    /**
     * 判断服务提供者响应的结果数据类型是心跳消息还是响应消息:<p>
     * Ⅰ.服务提供者响应的心跳消息;<p>
     * Ⅱ.服务提供者发送的心跳消息;<p>
     * Ⅲ.响应消息:正常的rpc请求.
     */
    private void handlerMessage(RpcProtocol<RpcResponse> protocol, Channel channel) {
        RpcHeader header = protocol.getHeader();
        logger.info("RpcConsumerHandler.handlerMessage: MsgType:{}", header.getMsgType());
        if (header.getMsgType() == (byte) RpcType.HEARTBEAT_TO_CONSUMER.getType()) { // 服务提供者响应的心跳消息
            this.handlerHeartbeatMessageToConsumer(protocol, channel);
        } else if (header.getMsgType() == (byte) RpcType.HEARTBEAT_FROM_PROVIDER.getType()) { // 服务提供者发送的心跳消息
            this.handlerHeartbeatMessageFromProvider(protocol, channel);
        } else if (header.getMsgType() == (byte) RpcType.RESPONSE.getType()) { // 响应消息
            this.handlerResponseMessage(protocol, header);
        }
    }

    /**
     * Ⅰ.处理服务提供者响应的心跳消息
     */
    private void handlerHeartbeatMessageToConsumer(RpcProtocol<RpcResponse> protocol, Channel channel) {
        // 由于心跳是服务消费者向服务提供者发起,服务提供者接收到心跳消息后,会立即响应。所以在服务消费者接收到服务提供者响应的心跳消息后,可不必处理。
        // 此处简单打印即可,实际场景可不做处理
        logger.info("receive service provider heartbeat message, the provider is:{}, the heartbeat message is:{}", channel.remoteAddress(), protocol.getBody().getResult());
    }

    /**
     * Ⅱ.处理服务提供者发送的心跳消息
     * 接收服务提供者发送过来的心跳ping消息,并响应pong消息
     */
    private void handlerHeartbeatMessageFromProvider(RpcProtocol<RpcResponse> protocol, Channel channel) {
        RpcHeader header = protocol.getHeader();
        header.setMsgType((byte) RpcType.HEARTBEAT_TO_PROVIDER.getType());
        header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        RpcRequest request = new RpcRequest();
        request.setParameters(new Object[]{RpcConstants.HEARTBEAT_PONG});
        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();
        requestRpcProtocol.setHeader(header);
        requestRpcProtocol.setBody(request);
        channel.writeAndFlush(requestRpcProtocol);
    }

    /**
     * Ⅲ.处理响应消息
     * 获取到响应的结果信息后,会唤醒阻塞的线程,向客户端响应数据
     */
    private void handlerResponseMessage(RpcProtocol<RpcResponse> protocol, RpcHeader header) {
        long requestId = header.getRequestId();
        RPCFuture rpcFuture = pendingRPC.remove(requestId);
        if (rpcFuture != null) {
            rpcFuture.done(protocol);
        }
    }

    /**
     * 服务消费者,向服务提供者发送请求
     */
    public RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, boolean async, boolean oneway) {
        logger.info("服务消费者发送的数据===>>>{}", JSONObject.toJSONString(protocol));
        return oneway ? this.sendRequestOneway(protocol) : async ? this.sendRequestAsync(protocol) : this.sendRequestSync(protocol);
    }

    /**
     * 同步调用方法
     */
    private RPCFuture sendRequestSync(RpcProtocol<RpcRequest> protocol) {
        logger.info("调用:同步.");
        RPCFuture rpcFuture = this.getRpcFuture(protocol);
        channel.writeAndFlush(protocol);
        return rpcFuture;
    }

    /**
     * 异步调用方法
     */
    private RPCFuture sendRequestAsync(RpcProtocol<RpcRequest> protocol) {
        logger.info("调用:异步.");
        RPCFuture rpcFuture = this.getRpcFuture(protocol);
        // 如果是异步调用,则将 RPCFuture 放入 RpcContext
        RpcContext.getContext().setRPCFuture(rpcFuture);
        channel.writeAndFlush(protocol);
        return null;
    }

    /**
     * 单向调用
     */
    private RPCFuture sendRequestOneway(RpcProtocol<RpcRequest> protocol) {
        logger.info("调用:单向.");
        channel.writeAndFlush(protocol);
        return null;
    }

    /**
     * 返回一个new的RpcRuture
     */
    private RPCFuture getRpcFuture(RpcProtocol<RpcRequest> protocol) {
        RPCFuture rpcFuture = new RPCFuture(protocol);
        RpcHeader header = protocol.getHeader();
        long requestId = header.getRequestId();
        pendingRPC.put(requestId, rpcFuture);
        return rpcFuture;
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public Channel getChannel() {
        return channel;
    }

    public SocketAddress getRemotePeer() {
        return remotePeer;
    }

}
