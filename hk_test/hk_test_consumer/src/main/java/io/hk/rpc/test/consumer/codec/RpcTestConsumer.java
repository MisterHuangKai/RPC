package io.hk.rpc.test.consumer.codec;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 测试消费端
 *
 * HK: RpcTestConsumer类是模拟的服务消费者的启动类，主要就是启动服务消费者并连接到服务提供者，
 *  后续会自动触发RpcTestConsumerHandler类中的channelActive()方法，向服务提供者发送数据，并在
 *  RpcTestConsumerHandler类中的channelRead0()方法中接收服务提供者响应的数据。
 */
public class RpcTestConsumer {

    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
        try {
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcTestConsumerInitializer());

            bootstrap.connect("127.0.0.1", 27880).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Thread.sleep(2000);
            eventLoopGroup.shutdownGracefully();
        }

    }

}
