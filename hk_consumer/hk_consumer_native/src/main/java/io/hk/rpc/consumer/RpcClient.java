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
import io.hk.rpc.spi.loader.ExtensionLoader;
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
     * 代理
     */
    private String proxy;

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
    /**
     * 心跳间隔时间,默认30秒
     */
    private int heartbeatInterval;
    /**
     * 扫描空闲连接时间,默认60秒
     */
    private int scanNotActiveChannelInterval;


    public RpcClient(String registryAddress, String registryType, String registryLoadBalanceType, String proxy, String serviceVersion, String serviceGroup, String serializationType, long timeout, boolean async, boolean oneway, int heartbeatInterval, int scanNotActiveChannelInterval) {
        this.proxy = proxy;
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.serializationType = serializationType;
        this.timeout = timeout;
        this.async = async;
        this.oneway = oneway;
        this.heartbeatInterval = heartbeatInterval;
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        this.registryService = this.getRegistryService(registryAddress, registryType, registryLoadBalanceType);
    }

    /**
     * 创建RegistryService接口类型的对象,并进行初始化
     *
     * @param registryAddress
     * @param registryType
     * @return registryService
     */
    private RegistryService getRegistryService(String registryAddress, String registryType, String registryLoadBalanceType) {
        if (StringUtils.isEmpty(registryType)) {
            throw new IllegalArgumentException("registryType is null");
        }
        // todo 后续SPI扩展
        RegistryService registryService = new ZookeeperRegistryService();
        try {
            registryService.init(new RegistryConfig(registryAddress, registryType, registryLoadBalanceType));
        } catch (Exception e) {
            logger.error("RpcClient init registry service throws exception:{}", e);
            throw new RegistryException(e.getMessage(), e);
        }
        return registryService;
    }

    /**
     * 基于SPI创建动态代理
     *
     * @param clazz
     * @return <T> Class<T>
     */
    public <T> T create(Class<T> clazz) {
//        ProxyFactory proxyFactory = new JdkProxyFactory<T>();
        ProxyFactory proxyFactory = ExtensionLoader.getExtension(ProxyFactory.class, proxy);
        proxyFactory.init(new ProxyConfig<>(clazz, serviceVersion, serviceGroup, serializationType, timeout, registryService, RpcConsumer.getInstance(heartbeatInterval, scanNotActiveChannelInterval), async, oneway));
        return proxyFactory.getProxy(clazz);
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> clazz) {
        return new ObjectProxy<T>(clazz, serviceVersion, serviceGroup, serializationType, timeout, registryService, RpcConsumer.getInstance(heartbeatInterval, scanNotActiveChannelInterval), async, oneway);
    }

    public void shutdown() {
        RpcConsumer.getInstance(heartbeatInterval, scanNotActiveChannelInterval).close();
    }


}