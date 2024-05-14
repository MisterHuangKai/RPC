package io.hk.rpc.proxy.jdk;

import io.hk.rpc.proxy.api.consumer.Consumer;
import io.hk.rpc.proxy.api.object.ObjectProxy;

import java.lang.reflect.Proxy;

/**
 * JDK 动态代理工厂类
 *
 * @author HuangKai
 * @date 2024/5/14
 */
public class JdkProxyFactory {

    /**
     * 服务版本号
     */
    private String serviceVersion;
    /**
     * 服务分组
     */
    private String serviceGroup;
    /**
     * 超时时间,默认15s
     */
    private long timeout = 15000L;
    /**
     * 服务消费者
     */
    private Consumer consumer;
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

    public JdkProxyFactory(String serviceVersion, String serviceGroup, String serializationType, long timeout, Consumer consumer, boolean async, boolean oneway) {
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.serializationType = serializationType;
        this.timeout = timeout;
        this.consumer = consumer;
        this.async = async;
        this.oneway = oneway;
    }

    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                new ObjectProxy<T>(clazz, serviceVersion, serviceGroup, serializationType, timeout, consumer, async, oneway)
        );
    }

}
