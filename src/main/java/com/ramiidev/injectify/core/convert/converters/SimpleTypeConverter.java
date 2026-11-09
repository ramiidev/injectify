package com.ramiidev.injectify.core.convert.converters;

import com.ramiidev.injectify.core.convert.support.ConversionService;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class SimpleTypeConverter {

    private SimpleTypeConverter() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object convert(String raw, Class<?> target, ConversionService cs) {
        // 1. Si es null (la clave no existe), retornamos null y dejamos que el binder decida
        if (raw == null) return null;

        if (target.isEnum()) {
            String value = raw.trim();

            // 2. ERROR DE VALOR VACÍO (injectify.scan.mode=)
            // Si el usuario puso la clave pero la dejó en blanco, lanzamos error explicativo.
            if (value.isEmpty()) {
                throw new IllegalArgumentException(
                        "El valor de configuración para el Enum '" + target.getSimpleName() + "' no puede estar vacío. " +
                                "Opciones válidas: " + getEnumValues(target)
                );
            }

            // 3. Búsqueda Insensible a Mayúsculas (GENERATE == generate == Generate)
            for (Object enumConstant : target.getEnumConstants()) {
                if (((Enum) enumConstant).name().equalsIgnoreCase(value)) {
                    return enumConstant;
                }
            }

            // 4. ERROR DE VALOR INVÁLIDO (injectify.scan.mode=INVALIDO)
            throw new IllegalArgumentException(
                    "No se puede convertir el valor '" + raw + "' al tipo " + target.getSimpleName() + ". " +
                            "Valores permitidos: " + getEnumValues(target)
            );
        }

        return cs.convert(raw, target);
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
