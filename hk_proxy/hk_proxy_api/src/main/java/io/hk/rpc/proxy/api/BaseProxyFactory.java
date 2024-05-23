package io.hk.rpc.proxy.api;

import io.hk.rpc.proxy.api.config.ProxyConfig;
import io.hk.rpc.proxy.api.object.ObjectProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基础代理工厂类
 *
 * @author HuangKai
 * @date 2024/5/17
 */
public abstract class BaseProxyFactory<T> implements ProxyFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseProxyFactory.class);

    protected ObjectProxy<T> objectProxy;

    @Override
    public <T> void init(ProxyConfig<T> proxyConfig) {
        LOGGER.info("BaseProxyFactory: abstract class init method.");
        this.objectProxy = new ObjectProxy(proxyConfig.getClazz(),
                proxyConfig.getServiceVersion(),
                proxyConfig.getServiceGroup(),
                proxyConfig.getSerializationType(),
                proxyConfig.getTimeout(),
                proxyConfig.getRegistryService(),
                proxyConfig.getConsumer(),
                proxyConfig.getAsync(),
                proxyConfig.getOneway());
    }

}
