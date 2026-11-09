package com.ramiidev.injectify.core.convert.converters;

import com.ramiidev.injectify.core.convert.Converter;
import com.ramiidev.injectify.core.convert.ConverterFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

    private final Map<Class<?>, Converter<String, ?>> cache = new ConcurrentHashMap<>();

    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return (Converter<String, T>) cache.computeIfAbsent(targetType, type -> new StringToEnum(type));
    }

    private static class StringToEnum<T extends Enum> implements Converter<String, T> {
        private final Class<T> enumType;

        public StringToEnum(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            // 1. Si es null, retornamos null (igual que tu lógica original)
            if (source == null) {
                return null;
            }

            String value = source.trim();

            // 2. ERROR DE VALOR VACÍO
            if (value.isEmpty()) {
                throw new IllegalArgumentException(
                        "El valor de configuración para el Enum '" + enumType.getSimpleName() + "' no puede estar vacío. " +
                                "Opciones válidas: " + getEnumValues(enumType)
                );
            }

            // 3. Búsqueda Insensible a Mayúsculas (Tu lógica preservada)
            for (T enumConstant : enumType.getEnumConstants()) {
                if (enumConstant.name().equalsIgnoreCase(value)) {
                    return enumConstant;
                }
            }

            // 4. ERROR DE VALOR INVÁLIDO
            throw new IllegalArgumentException(
                    "No se puede convertir el valor '" + source + "' al tipo " + enumType.getSimpleName() + ". " +
                            "Valores permitidos: " + getEnumValues(enumType)
            );
        }

        /**
         * Helper para obtener la lista bonita de valores: "[NONE, GENERATE, ACTIVATE]"
         */
        private static String getEnumValues(Class<?> enumType) {
            return Arrays.stream(enumType.getEnumConstants())
                    .map(obj -> ((Enum<?>) obj).name())
                    .collect(Collectors.joining(", ", "[", "]"));
        }
    }
}
