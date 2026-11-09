package com.ramiidev.injectify.shared.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class ReflectionUtils {
    private ReflectionUtils() {}

    public static <T> T instantiate(Class<T> type) {
        try {
            Constructor<T> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo instanciar " + type.getName(), ex);
        }
    }

    public static void setField(Object target, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo asignar el campo " + field.getName(), ex);
        }
    }

    /**
     * Extrae el tipo genérico de una lista (ej: List<Integer> -> Integer.class).
     * Si no tiene genéricos (List raw), asume String.class.
     */
    public static Class<?> getGenericType(Type genericType) {
        if (genericType instanceof ParameterizedType paramType) {
            Type[] args = paramType.getActualTypeArguments();
            if (args.length > 0 && args[0] instanceof Class<?>) {
                return (Class<?>) args[0];
            }
        }
        return String.class; // Default seguro
    }
}
