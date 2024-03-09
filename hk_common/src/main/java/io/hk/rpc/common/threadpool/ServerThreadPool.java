package io.hk.rpc.common.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池工具类
 * <p>
 * HK: 在服务提供者一端执行异步任务
 */
public class ServerThreadPool {

    private static ThreadPoolExecutor threadPoolExecutor;

    static {
        threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600, TimeUnit.SECONDS, new ArrayBlockingQueue<>(65536));
    }

    public static void submit(Runnable runnable) {
        threadPoolExecutor.submit(runnable);
    }

    public static void shutdown() {
        threadPoolExecutor.shutdown();
    }

}
