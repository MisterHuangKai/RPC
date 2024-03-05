package io.hk.rpc.serialization.jdk;

import io.hk.rpc.common.exception.SerializerException;
import io.hk.rpc.serialization.api.Serialization;
import io.hk.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;

/**
 * Jdk Serialization
 */
@SPIClass
public class JdkSerialization implements Serialization {

    private final Logger logger = LoggerFactory.getLogger(JdkSerialization.class);

    @Override
    public <T> byte[] serialize(T obj) {
        logger.info("execute jdk serialize...");
        if (obj == null) {
            throw new SerializerException("serialize object is null");
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        logger.info("execute jdk deserialize...");
        if (data == null) {
            throw new SerializerException("deserialize data is null");
        }
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        try {
            // 创建文件输出流
            FileOutputStream fos = new FileOutputStream("D:/demo/test.txt");
            // 创建相应的对象输出流
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            // 写入输出流数据
            oos.writeInt(1314);
            oos.writeObject("这些东西将会写入test.txt文件中");
            oos.writeObject(new Date());
            oos.close();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
