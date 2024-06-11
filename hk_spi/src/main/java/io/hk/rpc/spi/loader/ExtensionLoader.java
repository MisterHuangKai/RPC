package io.hk.rpc.spi.loader;

import io.hk.rpc.spi.annotation.SPI;
import io.hk.rpc.spi.annotation.SPIClass;
import io.hk.rpc.spi.factory.ExtensionFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    /**
     * Instantiates a new Extension Loader.
     */
    private ExtensionLoader(final Class<T> clazz, final ClassLoader cl) {
        this.clazz = clazz;
        this.classLoader = cl;
        if (!Objects.equals(clazz, ExtensionFactory.class)) {
            // getExtensionLoader:1.(1)(2)  getExtensionClasses:2.(1)(2)(3)(4)
            ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getExtensionClasses();
        }
    }

    /**
     * Gets Extension Loader.
     * <p>
     * 1.(1)
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(final Class<T> clazz) {
        return getExtensionLoader(clazz, ExtensionLoader.class.getClassLoader());
    }

    /**
     * Gets Extension Loader.
     * <p>
     * 1.(2)
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
     * Gets extension class.
     * <p>
     * 2.(1)
     */
    public Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classMap = cachedClasses.getValue();
        if (Objects.isNull(classMap)) {
            synchronized (cachedClasses) {
                classMap = cachedClasses.getValue();
                if (Objects.isNull(classMap)) {
                    classMap = loadExtensionClass();
                    cachedClasses.setValue(classMap);
                }
            }
        }
        return classMap;
    }

    /**
     * 2.(2)
     */
    private Map<String, Class<?>> loadExtensionClass() {
        SPI annotation = clazz.getAnnotation(SPI.class);
        if (Objects.nonNull(annotation)) {
            String value = annotation.value();
            if (StringUtils.isNotBlank(value)) {
                cachedDefaultName = value;
            }
        }
        Map<String, Class<?>> classes = new HashMap<>(16);
        loadDirectory(classes);
        return classes;
    }

    /**
     * 2.(3)
     */
    private void loadDirectory(final Map<String, Class<?>> classes) {
        for (String directory : SPI_DIRECTORIES) {
            String fileName = directory + clazz.getName();
            try {
                Enumeration<URL> urlEnumeration = Objects.nonNull(this.classLoader) ? classLoader.getResources(fileName) : ClassLoader.getSystemResources(fileName);
                if (Objects.nonNull(urlEnumeration)) {
                    while (urlEnumeration.hasMoreElements()) {
                        URL url = urlEnumeration.nextElement();
                        loadResources(classes, url);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("load extension class error {}", fileName, e);
            }
        }
    }

    /**
     * 2.(4)
     */
    private void loadResources(final Map<String, Class<?>> classes, final URL url) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            Properties properties = new Properties();
            properties.load(inputStream);
            properties.forEach((k, v) -> {
                String name = (String) k;
                String classPath = (String) v;
                if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(classPath)) {
                    try {
                        loadClass(classes, name, classPath);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException("load extension resources error", e);
                    }
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("load extension resources error", e);
        }
    }

    /**
     * 2.(5)
     */
    private void loadClass(final Map<String, Class<?>> classes, final String name, final String classPath) throws ClassNotFoundException {
        Class<?> subClass = Objects.nonNull(this.classLoader) ? Class.forName(classPath, true, this.classLoader) : Class.forName(classPath);
        if (!clazz.isAssignableFrom(subClass)) {
            throw new IllegalStateException("load extension resources error," + subClass + " subType is not of " + clazz);
        }
        if (!subClass.isAnnotationPresent(SPIClass.class)) {
            throw new IllegalStateException("load extension resources error," + subClass + " without @" + SPIClass.class + " annotation");
        }
        Class<?> oldClass = classes.get(name);
        if (Objects.isNull(oldClass)) {
            classes.put(name, subClass);
        } else if (!Objects.equals(oldClass, subClass)) {
            throw new IllegalStateException("load extension resources error, Duplicate class " + clazz.getName() + " name " + name + " on " + oldClass.getName() + " or " + subClass.getName());
        }
    }


    /**
     * 直接获取想要的类实例
     */
    public static <T> T getExtension(final Class<T> clazz, String name) {
        return StringUtils.isEmpty(name) ? getExtensionLoader(clazz).getDefaultSpiClassInstance() : getExtensionLoader(clazz).getSpiClassInstance(name);
    }

    /**
     * Gets default spi class instance.
     */
    public T getDefaultSpiClassInstance() {
        getExtensionClasses();
        if (StringUtils.isBlank(cachedDefaultName)) {
            return null;
        }
        return getSpiClassInstance(cachedDefaultName);
    }

    /**
     * Gets spi class.
     */
    public T getSpiClassInstance(final String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullPointerException("get spi class name is null.");
        }
        Holder<Object> objectHolder = cachedInstances.get(name);
        if (Objects.isNull(objectHolder)) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            objectHolder = cachedInstances.get(name);
        }
        Object value = objectHolder.getValue();
        if (Objects.isNull(value)) {
            synchronized (cachedInstances) {
                value = objectHolder.getValue();
                if (Objects.isNull(value)) {
                    value = createExtension(name);
                    objectHolder.setValue(value);
                }
            }
        }
        return (T) value;
    }

    @SuppressWarnings("unchecked")
    private T createExtension(final String name) {
        Class<?> aClass = getExtensionClasses().get(name);
        if (Objects.isNull(aClass)) {
            throw new IllegalArgumentException("name is error.");
        }
        Object obj = spiClassInstances.get(aClass);
        if (Objects.isNull(obj)) {
            try {
                spiClassInstances.putIfAbsent(aClass, aClass.newInstance());
                obj = spiClassInstances.get(aClass);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Extension instance(name: " + name + ", class: " + aClass + ") could not be instantiated: " + e.getMessage(), e);
            }
        }
        return (T) obj;
    }

    /**
     * Get all spi class instances.
     */
    public List<T> getSpiClassInstances() {
        Map<String, Class<?>> extensionClassMap = this.getExtensionClasses();
        if (extensionClassMap.isEmpty()) {
            return Collections.emptyList();
        }
        if (Objects.equals(extensionClassMap.size(), cachedInstances.size())) {
            return (List<T>) this.cachedInstances.values().stream().map(e -> e.getValue()).collect(Collectors.toList());
        }
        List<T> instances = new ArrayList<>();
        extensionClassMap.forEach((name, v) -> {
            T instance = this.getSpiClassInstance(name);
            instances.add(instance);
        });
        return instances;
    }


    /**
     * 自定义 Holder 类
     */
    public static class Holder<T> {

        private volatile T value;

        public T getValue() {
            return value;
        }

        public void setValue(final T value) {
            this.value = value;
        }

    }

}
