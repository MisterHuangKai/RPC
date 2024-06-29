package io.hk.rpc.serialization.protostuff;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import io.hk.rpc.common.exception.SerializerException;
import io.hk.rpc.serialization.api.Serialization;
import io.hk.rpc.spi.annotation.SPIClass;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protostuff序列化与反序列化
 */
@SPIClass
public class ProtostuffSerialization implements Serialization {

    private final Logger logger = LoggerFactory.getLogger(ProtostuffSerialization.class);

    private Map<Class<?>, Schema<?>> cachedSchemas = new ConcurrentHashMap<>();

    private Objenesis objenesis = new ObjenesisStd(true);

    private <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) cachedSchemas.get(clazz);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(clazz);
            if (schema != null) {
                cachedSchemas.put(clazz, schema);
            }
        }
        return schema;
    }

    /**
     * 序列化
     */
    @Override
    public <T> byte[] serialize(T obj) {
        logger.info("execute protostuff serialize...");
        if (obj == null) {
            throw new SerializerException("serialize object is null.");
        }
        Class<T> clazz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(clazz);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        logger.info("execute protostuff deserialize...");
        if (data == null) {
            throw new SerializerException("deserialize data is null.");
        }
        try {
            T message = objenesis.newInstance(clazz);
            Schema<T> schema = getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }

}
