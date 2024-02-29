package io.hk.rpc.test.provider;

import io.hk.rpc.annotation.RpcService;

/**
 * DemoService实现类
 */
@RpcService(interfaceClass = DemoService.class, interfaceClassName = "io.hk.rpc.test.provider.DemoService", group = "hk")
public class ProviderDemoServiceImpl implements DemoService {
}
