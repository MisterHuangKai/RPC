package io.hk.rpc.provider.common.server;

/**
 * @author binghe(公众号：冰河技术)
 * @version 1.0.0
 * @description 启动 RPC 服务的接口
 */
public interface Server {

    /**
     * 启动Netty服务
     */
    void startNettyServer();
}
