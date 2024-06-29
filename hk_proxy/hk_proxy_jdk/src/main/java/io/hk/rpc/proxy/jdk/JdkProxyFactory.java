package io.hk.rpc.proxy.jdk;

import io.hk.rpc.proxy.api.BaseProxyFactory;
import io.hk.rpc.proxy.api.ProxyFactory;
import io.hk.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;

/**
 * JDK 动态代理工厂类
 *
 * @author HuangKai
 * @date 2024/5/14
 */
@SPIClass
public class JdkProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    private final Logger logger = LoggerFactory.getLogger(JdkProxyFactory.class);

    @Override
    public <T> T getProxy(Class<T> clazz) {
        logger.info("基于JDK动态代理...");
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, objectProxy);
    }

}
