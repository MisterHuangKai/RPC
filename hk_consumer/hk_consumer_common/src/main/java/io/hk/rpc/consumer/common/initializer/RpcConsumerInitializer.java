package io.hk.rpc.consumer.common.initializer;

import io.hk.rpc.codec.RpcDecoder;
import io.hk.rpc.codec.RpcEncoder;
import io.hk.rpc.consumer.common.handler.RpcConsumerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * RPC消费者初始化
 */
public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * HK: netty的常规操作,将RpcEncoder、RpcDecoder、RpcConsumerHandler放入数据处理链中
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new RpcEncoder());
        pipeline.addLast(new RpcDecoder());
        pipeline.addLast(new RpcConsumerHandler());
    }

}
