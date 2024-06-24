package io.hk.rpc.serialization.hessian2;

import com.caucho.hessian.io.Hessian2Output;
import io.hk.rpc.common.exception.SerializerException;
import io.hk.rpc.serialization.api.Serialization;
import io.hk.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

/**
 * Hessian2序列化与反序列化
 */
@SPIClass
public class Hessian2Serialization implements Serialization {

    private static Logger logger = LoggerFactory.getLogger(Hessian2Serialization.class);

    /**
     * 序列化
     *
     * @param obj
     */
    @Override
    public <T> byte[] serialize(T obj) {
        logger.info("execute hessian2 serialize...");
        if (obj == null) {
            throw new SerializerException("serialize object is null.");
        }
        byte[] bytes = new byte[0];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(outputStream);


        return new byte[0];
    }

    /**
     * 反序列化
     *
     * @param data
     * @param clazz
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return null;
    }
}
