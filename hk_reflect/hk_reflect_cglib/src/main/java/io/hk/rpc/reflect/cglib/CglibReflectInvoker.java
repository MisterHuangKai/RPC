package io.hk.rpc.reflect.cglib;

import io.hk.rpc.reflect.api.ReflectInvoker;
import io.hk.rpc.spi.annotation.SPIClass;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cglib反射调用方法的类
 */
@SPIClass
public class CglibReflectInvoker implements ReflectInvoker {

    private final Logger logger = LoggerFactory.getLogger(CglibReflectInvoker.class);

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
        logger.info("use Cglib reflect type to invoke method ...");
        FastClass fastClass = FastClass.create(serviceClass);
        FastMethod fastMethod = fastClass.getMethod(methodName, parameterTypes);
        return fastMethod.invoke(serviceBean, parameters);
    }

}
