package io.hk.rpc.test.consumer.codec;

import com.alibaba.fastjson.JSONObject;
import io.hk.rpc.protocol.RpcProtocol;
import io.hk.rpc.protocol.header.RpcHeaderFactory;
import io.hk.rpc.protocol.request.RpcRequest;
import io.hk.rpc.protocol.response.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC消费者处理器
 * <p>
 * HK:
 *  1. channelActive()方法中,模拟封装了请求数据协议,并将数据发送到服务提供者。
 *  2. channelRead0()方法中,直接打印收到的从服务提供者响应过来的数据。
 */
public class RpcTestConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    private final Logger logger = LoggerFactory.getLogger(RpcTestConsumerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("发送数据开始...");
        // 模拟发送数据
        RpcRequest request = new RpcRequest();
        request.setClassName("io.hk.rpc.test.provider.DemoService");
        request.setMethodName("hello");
        request.setParameters(new Object[]{"hk"});
        request.setParameterTypes(new Class[]{String.class});
        request.setVersion("1.0.0");
        request.setGroup("hk");
        request.setAsync(false);
        request.setOneway(false);
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk"));
        protocol.setBody(request);
        logger.info("服务消费者发送的数据 ===>>> {}", JSONObject.toJSONString(protocol));
        ctx.writeAndFlush(protocol);
        logger.info("发送数据完毕...");


    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> protocol) throws Exception {
        logger.info("服务消费者接收到的数据 ===>>> {}", JSONObject.toJSONString(protocol));
    }


}
