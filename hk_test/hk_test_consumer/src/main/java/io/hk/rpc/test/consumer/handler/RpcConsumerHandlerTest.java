package io.hk.rpc.test.consumer.handler;

import io.hk.rpc.consumer.common.RpcConsumer;
import io.hk.rpc.protocol.RpcProtocol;
import io.hk.rpc.protocol.header.RpcHeaderFactory;
import io.hk.rpc.protocol.request.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试服务消费者（消费者整合注册中心后，需要引入注册中心服务）
 */
public class RpcConsumerHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerHandlerTest.class);

    public static void main(String[] args) throws InterruptedException {
        RpcConsumer rpcConsumer = RpcConsumer.getInstance();
        rpcConsumer.sendRequest(getRpcRequestProtocol());
        Thread.sleep(2000);
        rpcConsumer.close();
    }

    private static RpcProtocol<RpcRequest> getRpcRequestProtocol() {
        // 模拟发送数据
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk"));

        RpcRequest request = new RpcRequest();
        request.setClassName("io.hk.rpc.test.api.DemoService");
        // todo

    }


}
