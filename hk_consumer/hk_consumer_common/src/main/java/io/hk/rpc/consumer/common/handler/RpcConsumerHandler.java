package io.hk.rpc.consumer.common.handler;

import com.alibaba.fastjson.JSONObject;
import io.hk.rpc.consumer.common.context.RpcContext;
import io.hk.rpc.protocol.RpcProtocol;
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
 * RPC 消费者处理器
 */
public class RpcConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    private final Logger logger = LoggerFactory.getLogger(RpcConsumerHandler.class);

    private volatile Channel channel;
    public SocketAddress remotePeer;

    // 存储 请求ID与RpcResponse协议的映射关系
    private Map<Long, RPCFuture> pendingRPC = new ConcurrentHashMap<>(); // 实现异步转同步的关键

    public Channel getChannel() {
        return channel;
    }

    public SocketAddress getRemotePeer() {
        return remotePeer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    /**
     * 服务消费者,接收服务提供者响应的数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> protocol) throws Exception {
        if (protocol == null) {
            return;
        }

        logger.info("服务消费者接收到的数据===>>>{}", JSONObject.toJSONString(protocol));
        RpcHeader header = protocol.getHeader();
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
     * @param protocol
     * @return
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

}
