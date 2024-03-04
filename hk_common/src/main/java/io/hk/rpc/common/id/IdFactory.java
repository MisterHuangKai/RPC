package io.hk.rpc.common.id;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;

/**
 * 简易ID工厂类
 */
public class IdFactory {

    private final static AtomicLong REQUEST_ID_GEN = new AtomicLong(0);

    public static Long getId() {
        return REQUEST_ID_GEN.incrementAndGet();
    }


    /**
     * 统计0的个数
     */

    // 创建Long型原子计数器
    private static AtomicLong atomicLong = new AtomicLong();
    // 创建数据源
    private static Integer[] arrayOne = new Integer[]{0, 1, 2, 3, 0, 5, 6, 0, 56, 0};
    private static Integer[] arrayTwo = new Integer[]{10, 1, 2, 3, 0, 5, 6, 0, 56, 0};

    public static void main(String[] args) throws InterruptedException {
        // 线程one统计数组arrayOne中0的个数
        Thread threadOne = new Thread(new Runnable() {
            @Override
            public void run() {
                for (Integer integer : arrayOne) {
                    if (integer.intValue() == 0) {
                        // 调用unsafe方法，原子性设置value值为原始值加1，返回值为递增后的值
                        atomicLong.incrementAndGet();
                    }
                }
            }
        });
        // 线程two统计数组arrayTwo中0的个数
        Thread threadTwo = new Thread(new Runnable() {
            @Override
            public void run() {
                for (Integer integer : arrayTwo) {
                    if (integer.intValue() == 0) {
                        atomicLong.incrementAndGet();
                    }
                }
            }
        });
        // 启动子线程
        threadOne.start();
        threadTwo.start();
        // 等待线程执行完毕
        threadOne.join();
        threadTwo.join();
        System.out.println("count 0:" + atomicLong.get());
    }

}
