package io.hk.rpc.registry.zookeeper;

import io.hk.rpc.common.helper.RpcServiceHelper;
import io.hk.rpc.protocol.meta.ServiceMeta;
import io.hk.rpc.registry.api.RegistryService;
import io.hk.rpc.registry.api.config.RegistryConfig;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * 基于Zookeeper的注册服务
 *
 * @author HuangKai
 * @date 2024/5/21
 */
public class ZookeeperRegistryService implements RegistryService {

    /**
     * 初始化CuratorFramework客户端时,进行连接重试的间隔时间
     */
    public static final int BASE_SLEEP_TIME_MS = 1000;
    /**
     * 初始化CuratorFramework客户端时,进行连接重试的最大重试次数
     */
    public static final int MAX_RETRIES = 3;
    /**
     * 服务注册到Zookeeper的根路径
     */
    public static final String ZK_BASE_PATH = "/hk_rpc";

    /**
     * 服务注册与发现的ServiceDiscovery类实例
     */
    private ServiceDiscovery<ServiceMeta> serviceDiscovery;

    /**
     * 构建CuratorFramework客户端,并初始化serviceDiscovery
     */
    @Override
    public void init(RegistryConfig registryConfig) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(registryConfig.getRegistryAddr(), new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
        client.start();
        JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(ServiceMeta.class);
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
                .client(client)
                .serializer(serializer)
                .basePath(ZK_BASE_PATH)
                .build();
        this.serviceDiscovery.start();
    }

    /**
     * 使用serviceDiscovery将ServiceMeta元数据注册到Zookeeper中
     */
    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup()))
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.registerService(serviceInstance);
    }

    /**
     * 移除Zookeeper中注册的对应的元数据
     */
    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(serviceMeta.getServiceName())
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    /**
     * 根据传入的serviceName和invokerHashCode,从zookeeper中获取(一个)对应的ServiceMeta元数据信息
     */
    @Override
    public ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception {
        Collection<ServiceInstance<ServiceMeta>> serviceInstances = serviceDiscovery.queryForInstances(serviceName);
        ServiceInstance<ServiceMeta> instance = this.selectOneServiceInstance((List<ServiceInstance<ServiceMeta>>) serviceInstances);
        if (instance != null){
            return instance.getPayload();
        }
        return null;
    }

    /**
     * 通过调用serviceDiscovery对象的close()方法,关闭与Zookeeper的连接。即销毁与Zookeeper的连接。
     */
    @Override
    public void destroy() throws IOException {
        serviceDiscovery.close();
    }

    /**
     * 从serviceInstances中随机返回一个元数据
     *
     * @param serviceInstances
     * @return ServiceInstance<ServiceMeta>
     */
    private ServiceInstance<ServiceMeta> selectOneServiceInstance(List<ServiceInstance<ServiceMeta>> serviceInstances) {
        if (serviceInstances == null || serviceInstances.isEmpty()) {
            return null;
        }
        Random random = new Random();
        int index = random.nextInt(serviceInstances.size());
        return serviceInstances.get(index);
    }

}
