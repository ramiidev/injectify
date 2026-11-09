package com.ramiidev.injectify.core.convert;

/**
 * Una fábrica para crear convertidores dinámicos para una jerarquía de clases (como Enums).
 * S: Tipo fuente (generalmente String)
 * R: Tipo destino base (Range, ejemplo: Enum)
 */
public interface ConverterFactory<S, R> {
    <T extends R> Converter<S, T> getConverter(Class<T> targetType);
}
