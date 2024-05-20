package io.hk.rpc.proxy.api;

import io.hk.rpc.proxy.api.config.ProxyConfig;
import io.hk.rpc.proxy.api.object.ObjectProxy;

/**
 * 基础代理工厂类
 *
 * @author HuangKai
 * @date 2024/5/17
 */
public abstract class BaseProxyFactory<T> implements ProxyFactory {

    protected ObjectProxy<T> objectProxy;

    @Override
    public <T> void init(ProxyConfig<T> proxyConfig) {
        this.objectProxy = new ObjectProxy(proxyConfig.getClazz(),
                proxyConfig.getServiceVersion(),
                proxyConfig.getServiceGroup(),
                proxyConfig.getSerializationType(),
                proxyConfig.getTimeout(),
                proxyConfig.getConsumer(),
                proxyConfig.getAsync(),
                proxyConfig.getOneway());
    }

}
