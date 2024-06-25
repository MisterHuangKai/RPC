package io.hk.rpc.serialization.kryo;


import io.hk.rpc.serialization.api.Serialization;

/**
 * @param
 * @author HuangKai
 * @date 2024/6/25
 * @return
 */
public class KryoSerialization implements Serialization {
    /**
     * 序列化
     */
    @Override
    public <T> byte[] serialize(T obj) {
        return new byte[0];
    }

    /**
     * 反序列化
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return null;
    }
}
