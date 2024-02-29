package io.hk.rpc.provider.common.server;

/**
 * 启动 RPC 服务的接口
 */
public interface Server {

    /**
     * 启动Netty服务
     */
    void startNettyServer();
}
