package io.hk.rpc.serialization.kryo;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import io.hk.rpc.common.exception.SerializerException;
import io.hk.rpc.serialization.api.Serialization;
import io.hk.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Kryo序列化与反序列化
 */
@SPIClass
public class KryoSerialization implements Serialization {

    private final Logger logger = LoggerFactory.getLogger(KryoSerialization.class);

    /**
     * 序列化
     */
    @Override
    public <T> byte[] serialize(T obj) {
        logger.info("execute kryo serialize...");
        if (obj == null) {
            throw new SerializerException("serialize object is null.");
        }
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.register(obj.getClass(), new JavaSerializer());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Output output = new Output(outputStream);
        kryo.writeClassAndObject(output, obj);
        output.flush();
        output.close();
        byte[] bytes = outputStream.toByteArray();
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        }
        return bytes;
    }

    /**
     * 反序列化
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        logger.info("execute kryo deserialize...");
        if (data == null) {
            throw new SerializerException("deserialize data is null.");
        }
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.register(clazz, new JavaSerializer());
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        Input input = new Input(inputStream);
        return (T) kryo.readClassAndObject(input);
    }
}
