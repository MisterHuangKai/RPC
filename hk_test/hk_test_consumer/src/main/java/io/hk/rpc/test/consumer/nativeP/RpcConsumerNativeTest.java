package io.hk.rpc.test.consumer.nativeP;

import io.hk.rpc.consumer.RpcClient;
import io.hk.rpc.proxy.api.async.IAsyncObjectProxy;
import io.hk.rpc.proxy.api.future.RPCFuture;
import io.hk.rpc.test.provider.DemoService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param
 * @author HuangKai
 * @date 2024/5/14
 * @return
 */
public class RpcConsumerNativeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerNativeTest.class);

    public static void main(String[] args) {
        RpcClient rpcClient = new RpcClient("1.0.0", "hk", 3000L, "jdk", false, false);
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("huangkai?");
        LOGGER.info("返回的结果数据===>>>" + result);
        rpcClient.shutdown();
    }

    @Test
    public void testAsyncInterfaceRpc() throws Exception {
        RpcClient rpcClient = new RpcClient("1.0.0", "hk", 3000L, "jdk", true, false);
        IAsyncObjectProxy iAsyncObjectProxy = rpcClient.createAsync(DemoService.class);
        RPCFuture rpcFuture = iAsyncObjectProxy.call("hello", "HuangKai");
        LOGGER.info("返回的结果数据===>>>" + rpcFuture.get());
        rpcClient.shutdown();
    }

}
