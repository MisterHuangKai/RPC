package io.hk.rpc.consumer;

import io.hk.rpc.common.exception.RegistryException;
import io.hk.rpc.consumer.common.RpcConsumer;
import io.hk.rpc.proxy.api.ProxyFactory;
import io.hk.rpc.proxy.api.async.IAsyncObjectProxy;
import io.hk.rpc.proxy.api.config.ProxyConfig;
import io.hk.rpc.proxy.api.object.ObjectProxy;
import io.hk.rpc.proxy.jdk.JdkProxyFactory;
import io.hk.rpc.registry.api.RegistryService;
import io.hk.rpc.registry.api.config.RegistryConfig;
import io.hk.rpc.registry.zookeeper.ZookeeperRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 服务消费客户端
 *
 * @author HuangKai
 * @date 2024/5/14
 */
public class RpcClient {

    private final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private RegistryService registryService;

    /**
     * 服务版本号
     */
    private String serviceVersion;
    /**
     * 服务分组
     */
    private String serviceGroup;
    /**
     * 超时时间
     */
    private long timeout;
    /**
     * 序列化类型
     */
    private String serializationType;
    /**
     * 是否异步调用
     */
    private boolean async;
    /**
     * 是否单向调用
     */
    private boolean oneway;

    public RpcClient(String registryAddress, String registryType, String serviceVersion, String serviceGroup, String serializationType, long timeout,  boolean async, boolean oneway) {
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.serializationType = serializationType;
        this.timeout = timeout;
        this.async = async;
        this.oneway = oneway;
        this.registryService = this.getRegistryService(registryAddress, registryType);
    }

    /**
     * 创建RegistryService接口类型的对象,并进行初始化
     *
     * @param registryAddress
     * @param registryType
     * @return registryService
     */
    private RegistryService getRegistryService(String registryAddress, String registryType) {
        if (StringUtils.isEmpty(registryType)){
            throw new IllegalArgumentException("registryType is null");
        }
        // todo 后续SPI扩展
        RegistryService registryService = new ZookeeperRegistryService();
        try {
            registryService.init(new RegistryConfig(registryAddress, registryType, "1"));
        } catch (Exception e) {
            logger.error("RpcClient init registry service throws exception:{}", e);
            throw new RegistryException(e.getMessage(), e);
        }
        return registryService;
    }

    public <T> T create(Class<T> clazz) {
        ProxyFactory proxyFactory = new JdkProxyFactory<T>();
        proxyFactory.init(new ProxyConfig<>(clazz, serviceVersion, serviceGroup, serializationType, timeout, registryService, RpcConsumer.getInstance(), async, oneway));
        return proxyFactory.getProxy(clazz);
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> clazz) {
        return new ObjectProxy<T>(clazz, serviceVersion, serviceGroup, serializationType, timeout, registryService, RpcConsumer.getInstance(), async, oneway);
    }

    public void shutdown() {
        RpcConsumer.getInstance().close();
    }


}