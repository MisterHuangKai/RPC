package io.hk.rpc.proxy.asm.factory;

import io.hk.rpc.proxy.asm.proxy.ASMProxy;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 自定义ASM代理工厂: 生成ASM动态代理对象的工厂类
 */
public class ASMGenerateProxyFactory {

    private static final Integer DEFAULT_NUM = 1;

    public static byte[] generateClass(Class<?>[] interfaces, String proxyClassName) {
        // 创建一个ClassWriter对象,自动计算栈帧和局部变量表大小
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        // 创建的java版本、访问标志、类名、父类、接口
        String internalName = Type.getInternalName(ASMProxy.class);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, proxyClassName, null, internalName, getInterfacesName(interfaces));
        // 创建<init>
        createInit(cw);
        // 创建static
        addStatic(cw, interfaces);
        // 创建<clinit>
        addClinit(cw, interfaces, proxyClassName);
        // 实现接口方法
        addInterfacesImpl(cw, interfaces, proxyClassName);
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static String[] getInterfacesName(Class<?>[] interfaces) {
        String[] interfacesName = new String[interfaces.length];
        return Arrays.stream(interfaces).map(Type::getInternalName).collect(Collectors.toList()).toArray(interfacesName);
    }

    /**
     * 创建init方法
     * 调用父类的构造方法
     * 0 aload_0
     * 1 aload_1
     * 2 invokespecial #1 <proxy/ASMProxy.<init> : (Ljava/lang/reflect/InvocationHandler;)V>
     * 5 return
     * @param cw
     */
    private static void createInit(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/reflect/InvocationHandler;)V", null, null);
        mv.visitCode();
        // 将this入栈
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        // 将参数入栈
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        // 调用父类初始化方法
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(ASMProxy.class), "<init>", "(Ljava/lang/reflect/InvocationHandler;)V", false);
        // 返回
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    /**
     * 创建static字段
     * @param cw
     * @param interfaces
     */
    private static void addStatic(ClassWriter cw, Class<?>[] interfaces) {
        for (Class<?> anInterface : interfaces) {
            for (int i = 0; i < anInterface.getMethods().length; i++) {
                String methodName = "_" + anInterface.getSimpleName() + "_" + i;

            }
        }
    }


}
