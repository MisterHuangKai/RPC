package io.hk.rpc.proxy.api;

import io.hk.rpc.proxy.api.config.ProxyConfig;
import io.hk.rpc.spi.annotation.SPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代理工厂接口
 *
 * @author HuangKai
 * @date 2024/5/17
 */
@SPI
public interface ProxyFactory {

    Logger LOGGER = LoggerFactory.getLogger(ProxyFactory.class);

    /**
     * 获取代理对象
     */
    <T> T getProxy(Class<T> clazz);

    /**
     * 默认初始化方法
     */
    default <T> void init(ProxyConfig<T> proxyConfig) {
        LOGGER.info("ProxyFactory init.");
    }

}
