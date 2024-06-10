package io.hk.rpc.spi.loader;

import io.hk.rpc.spi.annotation.SPI;
import io.hk.rpc.spi.factory.ExtensionFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.Holder;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现SPI机制最核心的类
 */
public final class ExtensionLoader<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionLoader.class);

    private static final String SERVICES_DIRECTORY = "META-INF/services/";
    private static final String HK_DIRECTORY = "META-INF/hk/";
    private static final String HK_DIRECTORY_EXTERNAL = "META-INF/hk/external/";
    private static final String HK_DIRECTORY_INTERNAL = "META-INF/hk/internal/";

    private static final String[] SPI_DIRECTORIES = new String[]{SERVICES_DIRECTORY, HK_DIRECTORY, HK_DIRECTORY_EXTERNAL, HK_DIRECTORY_INTERNAL};

    private static final Map<Class<?>, ExtensionLoader<?>> LOADERS = new ConcurrentHashMap<>();

    private final Class<T> clazz;

    private final ClassLoader classLoader;

    private String cachedDefaultName;

    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    private final Map<Class<?>, Object> spiClassInstances = new ConcurrentHashMap<>();

    private ExtensionLoader(final Class<T> clazz, final ClassLoader cl) {
        this.clazz = clazz;
        this.classLoader = cl;
        if (!Objects.equals(clazz, ExtensionFactory.class)) {
            ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getExtensionClass();
        }
    }

    /**
     * 获取ExtensionLoader
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(final Class<T> clazz) {
        return getExtensionLoader(clazz, ExtensionLoader.class.getClassLoader());
    }

    /**
     * 获取ExtensionLoader
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(final Class<T> clazz, final ClassLoader loader) {
        Objects.requireNonNull(clazz, "extension class is null");

        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("extension class (" + clazz + ") is not interface.");
        }
        if (!clazz.isAnnotationPresent(SPI.class)) {
            throw new IllegalArgumentException("extension class (" + clazz + ") without @" + SPI.class + " Annotation.");
        }
        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) LOADERS.get(clazz);
        if (Objects.nonNull(extensionLoader)) {
            return extensionLoader;
        }
        LOADERS.putIfAbsent(clazz, new ExtensionLoader<>(clazz, loader));
        return (ExtensionLoader<T>) LOADERS.get(clazz);

    }

    /**
     * 直接获取想要的类实例
     */
    public static <T> T getExtension(final Class<T> clazz, String name) {
        return StringUtils.isEmpty(name) ? getExtensionLoader(clazz).getDefaultSpiClassInstance() : getExtensionLoader(clazz).getSpiClassInstance(name);
    }

    /**
     * gets default spi class instance
     */
    public T getDefaultSpiClassInstance() {
        getExtensionClasses();
        if(StringUtils.isBlank(cachedDefaultName)){
            return null;
        }
        return getSpiClassInstance(cachedDefaultName);
    }

    private void getExtensionClasses() {
    }

    private T getSpiClassInstance(String name) {
    }



    public static void main(String[] args) {
        Objects obj = null;
        Objects.requireNonNull(obj, "extension class is null");
        System.out.println(obj);
    }


}
