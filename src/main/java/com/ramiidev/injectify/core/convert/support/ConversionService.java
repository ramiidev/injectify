package com.ramiidev.injectify.core.convert.support;

import com.ramiidev.injectify.core.convert.Converter;
import com.ramiidev.injectify.core.convert.ConverterFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/// Mejorar el servicio de converision
public class ConversionService {

    private final Map<Class<?>, Converter<String, ?>> converters = new ConcurrentHashMap<>();
    private final Map<Class<?>, ConverterFactory<String, ?>> converterFactories = new ConcurrentHashMap<>();

    // --- Registro ---

    public <T> void addConverter(Class<T> targetType, Converter<String, T> converter) {
        converters.put(targetType, converter);
    }

    public void addConverterFactory(Class<?> targetBaseType, ConverterFactory<String, ?> factory) {
        converterFactories.put(targetBaseType, factory);
    }

    // --- Ejecución ---

    @SuppressWarnings("unchecked")
    public <T> T convert(String source, Class<T> targetType) {
        if (source == null) return null;
        if (targetType == String.class) return (T) source;

        // 1. Búsqueda Directa (Prioridad Alta)
        Converter<String, ?> converter = converters.get(targetType);
        if (converter != null) {
            return (T) converter.convert(source);
        }

        // 2. Búsqueda por Fábrica Genérica (Prioridad Media)
        // Recorremos las fábricas registradas para ver si alguna aplica al targetType
        for (Map.Entry<Class<?>, ConverterFactory<String, ?>> entry : converterFactories.entrySet()) {
            Class<?> baseType = entry.getKey(); // Ej: Enum.class

            // ¿Es targetType (LogMode) una subclase de baseType (Enum)?
            if (baseType.isAssignableFrom(targetType)) {
                ConverterFactory<String, T> factory = (ConverterFactory<String, T>) entry.getValue();
                // ¡Eureka! Encontramos una fábrica capaz de manejar este tipo
                return factory.getConverter(targetType).convert(source);
            }
        }

        throw new IllegalArgumentException("No se encontró convertidor para: " + targetType.getName());
    }
}
