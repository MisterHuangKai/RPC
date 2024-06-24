package io.hk.rpc.serialization.api;

import io.hk.rpc.constants.RpcConstants;
import io.hk.rpc.spi.annotation.SPI;

/**
 * 序列化接口
 */

// 使用SPI机制扩展序列化后, 是使用 jdk、json,还是其他的方式, 已经不取决于这里的 RpcConstants.SERIALIZATION_JDK 了。
// 而是在调用的地方的入参中, 例如: RpcConsumerNativeTest 的方法：
//      public void initRpcClient() {
//          rpcClient = new RpcClient("47.103.9.3:2181", "zookeeper", "1.0.0", "hk", "jdk", 3000L, false, false);
//          rpcClient = new RpcClient("47.103.9.3:2181", "zookeeper", "1.0.0", "hk", "json", 3000L, false, false);
//      }
@SPI(RpcConstants.SERIALIZATION_JDK)
public interface Serialization {

    /**
     * 序列化
     */
    <T> byte[] serialize(T obj);

    /**
     * 反序列化
     */
    <T> T deserialize(byte[] data, Class<T> clazz);

}
