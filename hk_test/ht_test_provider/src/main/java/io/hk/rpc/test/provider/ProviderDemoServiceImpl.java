package io.hk.rpc.test.provider;

import io.hk.rpc.annotation.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DemoService实现类
 */
@RpcService(interfaceClass = DemoService.class, interfaceClassName = "io.hk.rpc.test.provider.DemoService", version = "1.0.0", group = "hk")
public class ProviderDemoServiceImpl implements DemoService {

    private final Logger logger = LoggerFactory.getLogger(ProviderDemoServiceImpl.class);

    @Override
    public String hello(String name) {
        logger.info("调用hello方法传入的参数为 ===>>> {}", name);
        return "hello ! " + name;
    }

}
