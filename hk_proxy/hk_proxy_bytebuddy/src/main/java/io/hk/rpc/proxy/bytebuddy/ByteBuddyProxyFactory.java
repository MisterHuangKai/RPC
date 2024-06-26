package io.hk.rpc.proxy.bytebuddy;

import io.hk.rpc.proxy.api.BaseProxyFactory;
import io.hk.rpc.proxy.api.ProxyFactory;
import io.hk.rpc.spi.annotation.SPIClass;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ByteBuddy动态代理
 */
@SPIClass
public class ByteBuddyProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    private final Logger logger = LoggerFactory.getLogger(ByteBuddyProxyFactory.class);

    /**
     * 获取代理对象
     */
    @Override
    public <T> T getProxy(Class<T> clazz) {
        logger.info("基于ByteBuddy动态代理...");
        try {
            return (T) new ByteBuddy()
                    .subclass(Object.class)
                    .implement(clazz)
                    .intercept(InvocationHandlerAdapter.of(objectProxy))
                    .make()
                    .load(ByteBuddyProxyFactory.class.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            logger.error("bytebuddy proxy throws exception:{}", e.getMessage());
        }
        return null;
    }

}
