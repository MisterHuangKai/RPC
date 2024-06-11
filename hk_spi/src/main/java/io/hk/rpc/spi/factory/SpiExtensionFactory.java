package io.hk.rpc.spi.factory;

import io.hk.rpc.spi.annotation.SPIClass;
import io.hk.rpc.spi.loader.ExtensionLoader;

import java.util.Optional;

/**
 * 基于SPI实现的扩展类加载器工厂类
 */
@SPIClass
public class SpiExtensionFactory implements ExtensionFactory {
    @Override
    public <T> T getExtension(final String key, final Class<T> clazz) {
        return Optional.ofNullable(clazz)
                .filter(Class::isInterface)
                .filter(cls -> cls.isAnnotationPresent(SPIClass.class))
                .map(ExtensionLoader::getExtensionLoader)
                .map(ExtensionLoader::getDefaultSpiClassInstance)
                .orElse(null);
    }
}
