package io.hk.rpc.test.consumer.nativeP;

import io.hk.rpc.consumer.RpcClient;
import io.hk.rpc.proxy.api.async.IAsyncObjectProxy;
import io.hk.rpc.proxy.api.future.RPCFuture;
import io.hk.rpc.test.provider.DemoService;
import org.junit.Before;
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

    private RpcClient rpcClient;

    @Before
    public void initRpcClient() {
        rpcClient = new RpcClient("47.103.9.3:2181", "zookeeper", "1.0.0", "hk", "hessian2", 3000L, false, false);
    }

    @Test
    public void testInterfaceRpc() {
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("huangDad");
        LOGGER.info("返回的结果数据===>>>" + result);
        rpcClient.shutdown();
    }

    public static void main(String[] args) {
        RpcClient rpcClient = new RpcClient("47.103.9.3:2181", "zookeeper", "1.0.0", "hk", "jdk", 3000L, false, false);
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("huangkai?");
        LOGGER.info("返回的结果数据===>>>" + result);
        rpcClient.shutdown();
    }

    @Test
    public void testAsyncInterfaceRpc() throws Exception {
        RpcClient rpcClient = new RpcClient("47.103.9.3:2181", "zookeeper", "1.0.0", "hk", "jdk", 3000L, true, false);
        IAsyncObjectProxy iAsyncObjectProxy = rpcClient.createAsync(DemoService.class);
        RPCFuture rpcFuture = iAsyncObjectProxy.call("hello", "HuangKai");
        LOGGER.info("返回的结果数据===>>>" + rpcFuture.get());
        rpcClient.shutdown();
    }

}
