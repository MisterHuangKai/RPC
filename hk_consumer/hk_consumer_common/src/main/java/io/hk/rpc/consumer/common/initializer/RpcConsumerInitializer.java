package io.hk.rpc.consumer.common.initializer;

import io.hk.rpc.codec.RpcDecoder;
import io.hk.rpc.codec.RpcEncoder;
import io.hk.rpc.constants.RpcConstants;
import io.hk.rpc.consumer.common.handler.RpcConsumerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * RPC消费者初始化
 */
public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel> {

    private int heartbeatInterval;

    public RpcConsumerInitializer(int heartbeatInterval) {
        if (heartbeatInterval > 0) {
            this.heartbeatInterval = heartbeatInterval;
        }
    }

    /**
     * HK: netty的常规操作,将RpcEncoder、RpcDecoder、RpcConsumerHandler放入数据处理链中
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(RpcConstants.CODEC_ENCODER, new RpcEncoder());
        pipeline.addLast(RpcConstants.CODEC_DECODER, new RpcDecoder());
        pipeline.addLast(RpcConstants.CODEC_CLIENT_IDLE_HANDLER, new IdleStateHandler(heartbeatInterval, 0, 0, TimeUnit.MILLISECONDS));
        pipeline.addLast(RpcConstants.CODEC_HANDLER, new RpcConsumerHandler());
    }

}
