package io.hk.rpc.proxy.asm;

import io.hk.rpc.proxy.api.BaseProxyFactory;
import io.hk.rpc.proxy.api.ProxyFactory;
import io.hk.rpc.proxy.asm.proxy.ASMProxy;
import io.hk.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ASM动态代理
 */
@SPIClass
public class AsmProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    private final Logger logger = LoggerFactory.getLogger(AsmProxyFactory.class);

    /**
     * 获取代理对象
     */
    @Override
    public <T> T getProxy(Class<T> clazz) {
        logger.info("基于ASM动态代理...");
        try {
            return (T) ASMProxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{clazz}, objectProxy);
        } catch (Exception e) {
            logger.error("asm proxy throws exception:{}", e.getMessage());
        }
        return null;
    }

}
