package io.hk.rpc.test.consumer.nativeP;

import io.hk.rpc.consumer.RpcClient;
import io.hk.rpc.test.provider.DemoService;
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
        RpcClient rpcClient = new RpcClient("1.0.0", "hk", 3000L,"jdk" , false, false);
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("huangkai?");
        LOGGER.info("返回的结果数据===>>>" + result);
        rpcClient.shutdown();
    }

}
