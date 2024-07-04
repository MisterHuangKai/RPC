package io.hk.rpc.test.provider;

import io.hk.rpc.provider.RpcSingleServer;
import org.junit.Test;

/**
 * 测试Java原生启动RPC
 */
public class RpcSingleServerTest {

    @Test
    public void startRpcSingleServer() {
        RpcSingleServer rpcSingleServer = new RpcSingleServer("127.0.0.1:27880", "47.103.9.3:2181", "zookeeper", "io.hk.rpc.test", "jdk");
        rpcSingleServer.startNettyServer();
    }

}
