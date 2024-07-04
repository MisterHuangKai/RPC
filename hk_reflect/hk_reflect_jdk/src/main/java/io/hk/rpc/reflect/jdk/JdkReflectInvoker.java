package io.hk.rpc.reflect.jdk;

import io.hk.rpc.reflect.api.ReflectInvoker;
import io.hk.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * JDK反射调用方法的类
 */
@SPIClass
public class JdkReflectInvoker implements ReflectInvoker {

    private final Logger logger = LoggerFactory.getLogger(JdkReflectInvoker.class);

    /**
     * 调用真实方法的SPI通用接口
     *
     * @param serviceBean    方法所在的对象实例
     * @param serviceClass   方法所在对象实例的Class对象
     * @param methodName     方法的名称
     * @param parameterTypes 方法的参数类型数组
     * @param parameters     方法的参数数组
     * @return 方法调用的结果信息
     * @throws Throwable 抛出的异常
     */
    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        logger.info("use JDK reflect type to invoke method ...");
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        // 值为true则指示, 反射的对象在使用时应该取消Java语言访问检查。
        // 值为false则指示, 反射的对象应该实施Java语言访问检查。
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }

}
