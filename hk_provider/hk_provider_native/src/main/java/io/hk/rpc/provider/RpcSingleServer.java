package io.hk.rpc.provider;

import io.hk.rpc.provider.common.scanner.RpcServiceScanner;
import io.hk.rpc.provider.common.server.BaseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 以Java原生方式启动启动Rpc
 */
public class RpcSingleServer extends BaseServer {

    private final Logger logger = LoggerFactory.getLogger(RpcSingleServer.class);

    public RpcSingleServer(String serverAddress, String registryAddress, String registryType, String registryLoadBalanceType, String scanPackage, String reflectType,int heartbeatInterval, int scanNotActiveChannelInterval) {
        //调用父类构造方法
        super(serverAddress, registryAddress, registryType, registryLoadBalanceType, reflectType, heartbeatInterval, scanNotActiveChannelInterval);
        try {
            this.handlerMap = RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService(this.host, this.port, scanPackage, this.registryService);
        } catch (Exception e) {
            logger.error("RPC Server init error", e);
        }
    }

}
