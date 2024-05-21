package io.hk.rpc.protocol.meta;

import java.io.Serializable;
import java.util.Objects;

/**
 * 服务元数据，注册到注册中心的元数据信息
 */
public class ServiceMeta implements Serializable {

    private static final long serialVersionUID = 6289735590272020366L;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务版本号
     */
    private String serviceVersion;

    /**
     * 服务地址
     */
    private String serviceAddr;

    /**
     * 服务端口
     */
    private int servicePort;

    /**
     * 服务分组
     */
    private String serviceGroup;

    /**
     * 服务提供者实例的权重
     */
    private int weight;

    public ServiceMeta() {
    }

    public ServiceMeta(String serviceName, String serviceVersion, String serviceGroup, String serviceAddr, int servicePort, int weight) {
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
        this.serviceAddr = serviceAddr;
        this.servicePort = servicePort;
        this.serviceGroup = serviceGroup;
//        this.weight = weight;
    }

    public int hashCode() {
        return Objects.hash(serviceName, serviceVersion, serviceAddr, servicePort, serviceGroup, weight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceMeta serviceMeta = (ServiceMeta) o;
        return Objects.equals(serviceName, serviceMeta.serviceName)
                && Objects.equals(serviceVersion, serviceMeta.serviceVersion)
                && Objects.equals(serviceAddr, serviceMeta.serviceAddr)
                && servicePort == serviceMeta.servicePort
                && Objects.equals(serviceGroup, serviceMeta.serviceGroup)
                && weight == serviceMeta.weight;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getServiceAddr() {
        return serviceAddr;
    }

    public void setServiceAddr(String serviceAddr) {
        this.serviceAddr = serviceAddr;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

}