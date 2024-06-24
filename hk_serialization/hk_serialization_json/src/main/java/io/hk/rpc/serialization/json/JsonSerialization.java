package io.hk.rpc.serialization.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.hk.rpc.common.exception.SerializerException;
import io.hk.rpc.serialization.api.Serialization;
import io.hk.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * JSON序列化与反序列化
 */
@SPIClass
public class JsonSerialization implements Serialization {

    private static Logger logger = LoggerFactory.getLogger(JsonSerialization.class);

    // ObjectMapper是Jackson库中的一个核心类,它提供了将Java对象与JSON数据相互转换的功能。
    // Jackson是一个广泛使用的Java库,用于处理JSON数据。
    private static ObjectMapper objMapper = new ObjectMapper();

    static {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        objMapper.setDateFormat(format);
        objMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        objMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        objMapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        objMapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
        objMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
    }

    /**
     * 序列化
     */
    @Override
    public <T> byte[] serialize(T obj) {
        logger.info("execute json serialize...");
        if (obj == null) {
            throw new SerializerException("serialize object is null.");
        }
        byte[] bytes = new byte[0];
        try {
            bytes = objMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new SerializerException(e.getMessage(), e);
        }
        return bytes;
    }

    /**
     * 反序列化
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        logger.info("execute json deserialize...");
        if (data == null) {
            throw new SerializerException("deserialize data is null.");
        }
        T obj = null;
        try {
            obj = objMapper.readValue(data, clazz);
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        }
        return obj;
    }

}
