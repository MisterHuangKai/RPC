package io.hk.rpc.proxy.asm.proxy;

import io.hk.rpc.proxy.asm.classloader.ASMClassLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义的ASM代理:
 * <p>
 * 作为代理类需要继承的父类,提供一个静态的newProxyInstance()方法,
 * newProxyInstance里面调用ASMProxyFactory生成字节码二进制流,
 * 然后调用自定义的类加载器来生成Class.
 */
public class ASMProxy {

    protected InvocationHandler invocationHandler;

    // 代理类名计数器
    private static final AtomicInteger PROXY_CNT = new AtomicInteger(0);

    private static final String PROXY_CLASS_NAME_PRE = "$Proxy";

    private ASMProxy(InvocationHandler handler) {
        invocationHandler = handler;
    }

    public static Object newProxyInstance(ClassLoader classLoader, Class<?>[] interfaces, InvocationHandler handler) throws Exception {
        // 生成代理类Class
        Class<?> proxyClass = generate(interfaces);
        Constructor<?> constructor = proxyClass.getConstructor(InvocationHandler.class);
        return constructor.newInstance(handler);
    }

    /**
     * 生成代理类Class
     *
     * @param interfaces 接口的Class类型
     * @return 代理类的Class对象
     * @throws ClassNotFoundException
     */
    private static Class<?> generate(Class<?>[] interfaces) throws ClassNotFoundException {
        String proxyClassName = PROXY_CLASS_NAME_PRE + PROXY_CNT.getAndIncrement();
        byte[] codes = ASMGenerateProxyFactory.generateClass(interfaces, proxyClassName);
        // 使用自定义类加载器加载字节码
        ASMClassLoader asmClassLoader = new ASMClassLoader();
        asmClassLoader.add(proxyClassName, codes);
        return asmClassLoader.loadClass(proxyClassName);
    }

}
