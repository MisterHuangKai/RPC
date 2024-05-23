package io.hk.rpc.proxy.api.consumer;

import io.hk.rpc.protocol.RpcProtocol;
import io.hk.rpc.protocol.request.RpcRequest;
import io.hk.rpc.proxy.api.future.RPCFuture;
import io.hk.rpc.registry.api.RegistryService;

/**
 * 服务消费者
 */
public interface Consumer {

    /**
     * 消费者发送 request 请求
     */
    RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception;

}
