package io.hk.rpc.proxy.api.object;

import io.hk.rpc.proxy.api.consumer.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 对象代理类
 *
 * @author HuangKai
 * @date 2024/5/13
 */
public class ObjectProxy<T> implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectProxy.class);

    /**
     * 接口的Class对象
     */
    private Class<T> clazz;

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

    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    public ObjectProxy(Class<T> clazz, String serviceVersion, String serviceGroup, String serializationType, long timeout, Consumer consumer, boolean async, boolean oneway) {
        this.clazz = clazz;
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.serializationType = serializationType;
        this.timeout = timeout;
        this.consumer = consumer;
        this.async = async;
        this.oneway = oneway;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {

        }


        return null;
    }
}
