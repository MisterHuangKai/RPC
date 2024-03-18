package io.hk.rpc.test.provider;

import io.hk.rpc.provider.RpcSingleServer;
import org.junit.Test;

/**
 * 测试Java原生启动RPC
 */
public class RpcSingleServerTest {

    @Test
    public void startRpcSingleServer() {
//        RpcSingleServer rpcSingleServer = new RpcSingleServer("127.0.0.1:27880", "io.hk.rpc.test", "jdk");
        RpcSingleServer rpcSingleServer = new RpcSingleServer("127.0.0.1:27880", "io.hk.rpc.test", "cglib");
        rpcSingleServer.startNettyServer();
    }

}
