package io.hk.rpc.serialization.hessian2;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import io.hk.rpc.common.exception.SerializerException;
import io.hk.rpc.serialization.api.Serialization;
import io.hk.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hessian2序列化与反序列化
 */
@SPIClass
public class Hessian2Serialization implements Serialization {

    private final Logger logger = LoggerFactory.getLogger(Hessian2Serialization.class);

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
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
        try {
            hessian2Output.startMessage();
            hessian2Output.writeObject(obj);
            hessian2Output.flush();
            hessian2Output.completeMessage();
            bytes = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        } finally {
            try {
                if (hessian2Output != null) {
                    hessian2Output.close();
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
                throw new SerializerException(e.getMessage(), e);
            }
        }
        return bytes;
    }

    /**
     * 反序列化
     *
     * @param data
     * @param clazz
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        logger.info("execute hessian2 deserialize...");
        if (data == null) {
            throw new SerializerException("deserialize data is null.");
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        Hessian2Input hessian2Input = new Hessian2Input(byteArrayInputStream);
        T obj = null;
        try {
            hessian2Input.startMessage();
            obj = (T) hessian2Input.readObject();
            hessian2Input.completeMessage();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        } finally {
            try {
                if (null != hessian2Input) {
                    hessian2Input.close();
                    byteArrayInputStream.close();
                }
            } catch (IOException e) {
                throw new SerializerException(e.getMessage(), e);
            }
        }
        return obj;
    }

}
