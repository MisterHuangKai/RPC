package io.hk.rpc.consumer.common.context;


import io.hk.rpc.proxy.api.future.RPCFuture;

/**
 * 保存 RPC 上下文
 *
 * @date 2024/5/9
 */
public class RpcContext {

    private RpcContext() {
    }

    /**
     * RpcContext实例
     */
    private static final RpcContext AGENT = new RpcContext();

    /**
     * 存放 RPCFuture 的 InheritableThreadLocal
     */
    private static final InheritableThreadLocal<RPCFuture> RPC_FUTURE_INHERITABLE_THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * 获取上下文
     * @return RPC服务的上下文信息
     */
    public static RpcContext getContext() {
        return AGENT;
    }

    /**
     * 将 RPCFuture 保存到线程的上下文
     * @param rpcFuture
     */
    public void setRPCFuture(RPCFuture rpcFuture) {
        RPC_FUTURE_INHERITABLE_THREAD_LOCAL.set(rpcFuture);
    }

    /**
     * 获取 RPCFuture
     * @return
     */
    public RPCFuture getRPCFuture() {
        return RPC_FUTURE_INHERITABLE_THREAD_LOCAL.get();
    }

    /**
     * 移除 RPCFuture
     */
    public void removeRPCFuture() {
        RPC_FUTURE_INHERITABLE_THREAD_LOCAL.remove();
    }

}
