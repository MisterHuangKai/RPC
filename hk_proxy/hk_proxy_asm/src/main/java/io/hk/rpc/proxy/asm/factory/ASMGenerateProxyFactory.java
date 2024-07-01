package io.hk.rpc.proxy.asm.factory;

import org.objectweb.asm.ClassWriter;

/**
 * 自定义ASM代理工厂: 生成ASM动态代理对象的工厂类
 */
public class ASMGenerateProxyFactory {

    private static final Integer DEFAULT_NUM = 1;

    public static byte[] generateClass(Class<?>[] interfaces, String proxyClassName) {
        // 创建一个ClassWriter对象,自动计算栈帧和局部变量表大小
        new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS).var

    }



}
