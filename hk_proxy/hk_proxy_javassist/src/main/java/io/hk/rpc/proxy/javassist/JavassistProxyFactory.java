package io.hk.rpc.proxy.javassist;

import io.hk.rpc.proxy.api.BaseProxyFactory;
import io.hk.rpc.proxy.api.ProxyFactory;
import io.hk.rpc.spi.annotation.SPIClass;
import javassist.util.proxy.MethodHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Javassist动态代理
 */
@SPIClass
public class JavassistProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    private final Logger logger = LoggerFactory.getLogger(JavassistProxyFactory.class);

    private javassist.util.proxy.ProxyFactory proxyFactory = new javassist.util.proxy.ProxyFactory();

    /**
     * 获取代理对象
     */
    @Override
    public <T> T getProxy(Class<T> clazz) {
        logger.info("基于Javassist动态代理...");
        try {
            // 设置代理类的父类
            proxyFactory.setInterfaces(new Class[]{clazz});
            proxyFactory.setHandler(new MethodHandler() {
                @Override
                public Object invoke(Object obj, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                    return objectProxy.invoke(obj, thisMethod, args);
                }
            });
            // 通过字节码技术动态创建子类实例
            return (T) proxyFactory.createClass().newInstance();
        } catch (Exception e) {
            logger.info("javassist proxy throws exception:{}", e.getMessage());
        }
        return null;
    }

}
