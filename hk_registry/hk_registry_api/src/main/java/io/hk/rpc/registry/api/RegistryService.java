package io.hk.rpc.registry.api;

import io.hk.rpc.protocol.meta.ServiceMeta;
import io.hk.rpc.registry.api.config.RegistryConfig;
import io.hk.rpc.spi.annotation.SPI;

import java.io.IOException;

/**
 * 服务注册与发现
 */
@SPI
public interface RegistryService {

    /**
     * 服务注册
     */
    void register(ServiceMeta serviceMeta) throws Exception;

    /**
     * 服务取消注册
     */
    void unRegister(ServiceMeta serviceMeta) throws Exception;

    /**
     * 服务发现
     *
     * @param serviceName     服务名称
     * @param invokerHashCode HashCode值
     */
    ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception;

    /**
     * 服务销毁
     */
    void destroy() throws IOException;

    /**
     * 默认初始化方法
     */
    default void init(RegistryConfig registryConfig) throws Exception {

    }

}
