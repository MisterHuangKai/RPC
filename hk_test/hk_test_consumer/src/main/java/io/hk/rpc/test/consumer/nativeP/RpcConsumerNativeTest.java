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

    public static void main(String[] args){
        RpcClient rpcClient = new RpcClient("47.103.9.3:2181", "zookeeper", "random", "asm", "1.0.0", "hk",
                "hessian2", 3000, false, false, 10000, 30000, 2000, 5);
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("binghe");
        LOGGER.info("返回的结果数据 ===>>> " + result);
        // rpcClient.shutdown();
    }

    @Before
    public void initRpcClient() {
        rpcClient = new RpcClient("47.103.9.3:2181", "zookeeper", "random", "asm", "1.0.0", "hk", "hessian2", 3000L, false, false, 10000, 60000, 2000, 5);
    }

    @Test
    public void testNoShutdown() {
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("huangDad");
        LOGGER.info("返回的结果数据 ===>>>" + result);
//        rpcClient.shutdown();
    }

    @Test
    public void testInterfaceRpc() {
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("huangDad");
        LOGGER.info("返回的结果数据 ===>>>" + result);
        rpcClient.shutdown();
    }

    @Test
    public void testAsyncInterfaceRpc() throws Exception {
        RpcClient rpcClient = new RpcClient("47.103.9.3:2181", "zookeeper", "random", "jdk", "1.0.0", "hk", "hessian2", 3000L, true, false, 30000, 600000, 2000, 5);
        IAsyncObjectProxy iAsyncObjectProxy = rpcClient.createAsync(DemoService.class);
        RPCFuture rpcFuture = iAsyncObjectProxy.call("hello", "HuangKai");
        LOGGER.info("返回的结果数据 ===>>>" + rpcFuture.get());
        rpcClient.shutdown();
    }

}
