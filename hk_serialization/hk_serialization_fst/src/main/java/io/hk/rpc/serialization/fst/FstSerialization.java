package io.hk.rpc.serialization.fst;


import io.hk.rpc.common.exception.SerializerException;
import io.hk.rpc.serialization.api.Serialization;
import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fast Serialization, 简称 FST.
 *<p>
 * FST比标准Java序列化更快更安全更少占用内存,线程安全,是一个性能优越的Java序列化库,适用于对性能有严苛要求的高并发和大数据处理场景.
 */
public class FstSerialization implements Serialization {

    private final Logger logger = LoggerFactory.getLogger(FstSerialization.class);

    /**
     * 序列化
     */
    @Override
    public <T> byte[] serialize(T obj) {
        logger.info("execute fst serialize...");
        if (obj == null) {
            throw new SerializerException("serialize object is null.");
        }
        FSTConfiguration fstConfiguration = FSTConfiguration.getDefaultConfiguration();
        return fstConfiguration.asByteArray(obj);
    }

    /**
     * 反序列化
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        logger.info("execute fst deserialize...");
        if (data == null) {
            throw new SerializerException("deserialize data is null.");
        }
        FSTConfiguration fstConfiguration = FSTConfiguration.getDefaultConfiguration();
        return (T) fstConfiguration.asObject(data);
    }

}
