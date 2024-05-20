package io.hk.rpc.proxy.jdk;

import io.hk.rpc.proxy.api.BaseProxyFactory;
import io.hk.rpc.proxy.api.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 * JDK 动态代理工厂类
 *
 * @author HuangKai
 * @date 2024/5/14
 */
public class JdkProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    @Override
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, objectProxy);
    }

}
