package io.hk.rpc.test.consumer.codec;

import io.hk.rpc.codec.RpcDecoder;
import io.hk.rpc.codec.RpcEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;


/**
 * RPC测试消费者的 初始化程序
 */
public class RpcTestConsumerInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * HK: netty的常规操作,将RpcEncoder、RpcDecoder、RpcTestConsumerHandler放入数据处理链中
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline cp = socketChannel.pipeline();
        cp.addLast(new RpcEncoder());
        cp.addLast(new RpcDecoder());
        cp.addLast(new RpcTestConsumerHandler());

    }
}
