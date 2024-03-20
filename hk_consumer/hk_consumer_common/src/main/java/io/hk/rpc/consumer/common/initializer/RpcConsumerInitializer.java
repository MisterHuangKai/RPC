package io.hk.rpc.consumer.common.initializer;

import io.hk.rpc.codec.RpcDecoder;
import io.hk.rpc.codec.RpcEncoder;
import io.hk.rpc.consumer.common.handler.RpcConsumerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 *
 */
public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new RpcEncoder());
        pipeline.addLast(new RpcDecoder());
        pipeline.addLast(new RpcConsumerHandler());
    }

}
