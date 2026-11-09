package com.ramiidev.injectify.boot.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InjectifyImportsLoader {

    private static final Logger log = LoggerFactory.getLogger(InjectifyImportsLoader.class);
    private static final String FACTORIES_RESOURCE_LOCATION = "META-INF/injectify.factories";
    private static final Map<ClassLoader, Map<String, List<String>>> cache = new ConcurrentHashMap<>();

    private InjectifyImportsLoader() {
        // Utility class
    }

    public static <T> List<T> loadFactories(Class<T> factoryType) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<String> factoryNames = loadImportNames(classLoader, factoryType);
        List<T> result = new ArrayList<>();

        for (String name : factoryNames) {
            try {
                Class<?> instanceClass = Class.forName(name, true, classLoader);
                if (factoryType.isAssignableFrom(instanceClass)) {
                    T instance = factoryType.cast(instanceClass.getDeclaredConstructor().newInstance());
                    result.add(instance);
                }
            } catch (Exception ex) {
                log.warn("No se pudo instanciar la clase desde {}: {}", FACTORIES_RESOURCE_LOCATION, name, ex);
            }
        }
        return result;
    }

    public static Set<Class<?>> loadFactoryClasses(Class<?> factoryType) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<String> factoryNames = loadImportNames(classLoader, factoryType);
        Set<Class<?>> result = new LinkedHashSet<>();

        for (String name : factoryNames) {
            try {
                Class<?> clazz = Class.forName(name, true, classLoader);
                if (factoryType.isAssignableFrom(clazz)) {
                    result.add(clazz);
                }
            } catch (ClassNotFoundException ex) {
                log.warn("No se pudo cargar la clase de auto-configuración: {}", name);
            } catch (NoClassDefFoundError ex) {
                log.warn("Falta una dependencia para la clase: {}", name);
            }
        }
        return result;
    }

    private static List<String> loadImportNames(ClassLoader classLoader, Class<?> factoryType) {
        ClassLoader classLoaderToUse = classLoader != null ? classLoader : ClassLoader.getSystemClassLoader();

        Map<String, List<String>> importsByType = cache.computeIfAbsent(classLoaderToUse, cl -> {
            Map<String, List<String>> result = new ConcurrentHashMap<>();
            try {
                Enumeration<URL> urls = cl.getResources(FACTORIES_RESOURCE_LOCATION);
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    try (var reader = new java.io.InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                        Properties properties = new Properties();
                        properties.load(reader);
                        for (String typeName : properties.stringPropertyNames()) {
                            List<String> names = result.computeIfAbsent(typeName, ignored -> new ArrayList<>());
                            for (String name : properties.getProperty(typeName).split(",")) {
                                String trimmedName = name.trim();
                                if (!trimmedName.isEmpty() && !names.contains(trimmedName)) {
                                    names.add(trimmedName);
                                }
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                log.error("Error leyendo el archivo de factories: {}", FACTORIES_RESOURCE_LOCATION, ex);
            }
            result.replaceAll((key, value) -> Collections.unmodifiableList(value));
            return result;
        });

        return importsByType.getOrDefault(factoryType.getName(), Collections.emptyList());
    }

    /**
     * Libera la memoria utilizada por la caché de factories.
     * Debe ser invocado por el evento ApplicationReadyEvent.
     */
    public static void clearCache() {
        if (!cache.isEmpty()) {
            cache.clear();
            log.info("Optimizando RAM: Caché de factories liberada exitosamente.");
        }
    }
}