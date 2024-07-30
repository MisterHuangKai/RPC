package io.hk.rpc.consumer.common.helper;

import io.hk.rpc.consumer.common.handler.RpcConsumerHandler;
import io.hk.rpc.protocol.meta.ServiceMeta;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于缓存服务消费者处理器RpcConsumerHandler类的实例
 * <p>
 * HK备注：一个ConcurrentHashMap，及get、put、clear方法
 */
public class RpcConsumerHandlerHelper {

    private static Map<String, RpcConsumerHandler> rpcConsumerHandlerMap;

    static {
        rpcConsumerHandlerMap = new ConcurrentHashMap<>();
    }

    private static String getKey(ServiceMeta serviceMeta) {
        return serviceMeta.getServiceAddr().concat("_").concat(String.valueOf(serviceMeta.getServicePort()));
    }

    public static void put(ServiceMeta serviceMeta, RpcConsumerHandler rpcConsumerHandler) {
        rpcConsumerHandlerMap.put(getKey(serviceMeta), rpcConsumerHandler);
    }

    public static RpcConsumerHandler get(ServiceMeta serviceMeta) {
        return rpcConsumerHandlerMap.get(getKey(serviceMeta));
    }

    public static void remove(Channel channel) {
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        String address = socketAddress.getAddress().getHostAddress();
        int port = socketAddress.getPort();
        rpcConsumerHandlerMap.remove(generateKey(address, port));
    }

    public static int size() {
        return rpcConsumerHandlerMap.size();
    }

    public static void closeRpcClientHandler() {
        Collection<RpcConsumerHandler> rpcClientHandlers = rpcConsumerHandlerMap.values();
        if (!rpcClientHandlers.isEmpty()) {
            rpcClientHandlers.stream().forEach((rpcConsumerHandler) -> {
                rpcConsumerHandler.close();
            });
        }
        rpcConsumerHandlerMap.clear();
    }

    private static String generateKey(String address, int port) {
        return address.concat("_").concat(String.valueOf(port));
    }

}
