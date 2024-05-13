package io.hk.rpc.proxy.api.future;

import io.hk.rpc.common.threadpool.ClientThreadPool;
import io.hk.rpc.protocol.RpcProtocol;
import io.hk.rpc.protocol.request.RpcRequest;
import io.hk.rpc.protocol.response.RpcResponse;
import io.hk.rpc.proxy.api.callback.AsyncRPCCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * RPC框架获取异步结果的自定义Future
 */
public class RPCFuture extends CompletableFuture<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RPCFuture.class);

    // 内部类 Sync 的实例对象
    private Sync sync;
    // RpcRequest类型的协议对象
    private RpcProtocol<RpcRequest> requestRpcProtocol;
    // RpcResponse 类型的协议对象
    private RpcProtocol<RpcResponse> responseRpcProtocol;
    // 开始时间
    private long startTime;
    // 默认的超时时间
    private long responseTimeThreshold = 5000;

    // 存放回调接口
    private List<AsyncRPCCallback> pendingCallbacks = new ArrayList<>();
    // 添加和执行回调方法时,进行加锁与解锁
    private ReentrantLock lock = new ReentrantLock();

    public RPCFuture(RpcProtocol<RpcRequest> requestRpcProtocol) {
        this.sync = new Sync();
        this.requestRpcProtocol = requestRpcProtocol;
        this.startTime = System.currentTimeMillis();
    }

    static class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = 1L;

        // future status
        private final int done = 1;
        private final int pending = 0;

        protected boolean tryAcquire(int acquires) {
            return getState() == done;
        }

        protected boolean tryRelease(int releases) {
            if (getState() == pending) {
                if (compareAndSetState(pending, done)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isDone() {
            getState();
            return getState() == done;
        }

    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    // 阻塞获取responseRpcProtocol协议对象中的实际结果数据
    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(-1);
        if (this.responseRpcProtocol != null) {
            return this.responseRpcProtocol.getBody().getResult();
        } else {
            return null;
        }
    }

    // 超时阻塞获取responseRpcProtocol协议对象中的实际结果数据
    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (success) {
            if (this.responseRpcProtocol != null) {
                return this.responseRpcProtocol.getBody().getResult();
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Timeout exception. Request id:" + this.requestRpcProtocol.getHeader().getRequestId() + ". Request class name:" + this.requestRpcProtocol.getBody().getClassName() + ". Request method:" + this.requestRpcProtocol.getBody().getMethodName());
        }
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    /**
     * 当服务消费者接收到服务提供者响应的结果时,就会调用done()方法,并传入RpcResponse类型的协议对象,此时会唤醒阻塞的线程获取响应的结果数据。
     */
    public void done(RpcProtocol<RpcResponse> responseRpcProtocol) {
        this.responseRpcProtocol = responseRpcProtocol;
        sync.release(1);
        // 新增的调用invokeCallbacks()方法
        invokeCallbacks();
        // Threshold
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            LOGGER.warn("Service response time is too slow. Request id = " + responseRpcProtocol.getHeader().getRequestId() + ". Response Time = " + responseTime + " ms");
        }
    }

    /**
     * 用于异步执行回调方法
     */
    private void runCallback(final AsyncRPCCallback callback) {
        final RpcResponse rpcResponse = this.responseRpcProtocol.getBody();
        ClientThreadPool.submit(() -> {
            if (!rpcResponse.isError()) {
                callback.onSuccess(rpcResponse.getResult());
            } else {
                callback.onException(new RuntimeException("Response error", new Throwable(rpcResponse.getError())));
            }
        });
    }

    /**
     * 用于外部服务添加回调接口实例对象到 pendingCallbacks 集合中
     */
    public RPCFuture addCallback(AsyncRPCCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    /**
     * 用于依次执行 pendingCallbacks 集合中回调接口的方法
     */
    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncRPCCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }


}


