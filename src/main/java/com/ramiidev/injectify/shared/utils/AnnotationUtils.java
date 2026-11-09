package com.ramiidev.injectify.shared.utils;

import com.ramiidev.injectify.shared.annotation.AliasFor;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationUtils {

    private AnnotationUtils() { }

    /**
     * Busca el valor del nombre del bean (Component.value) inspeccionando
     * la clase y sus meta-anotaciones usando @AliasFor.
     */
    public static String findComponentBeanName(Class<?> targetClass) {

        // 1. Búsqueda Directa: ¿Tiene @Component la clase?
        if (targetClass.isAnnotationPresent(Component.class)) {
            return targetClass.getAnnotation(Component.class).value();
        }

        // 2. Búsqueda Recursiva (Meta-anotaciones)
        for (Annotation annotation : targetClass.getAnnotations()) {
            // Saltamos anotaciones de Java (@Retention, @Target, etc.)
            if (annotation.annotationType().getPackageName().startsWith("java.lang")) {
                continue;
            }

            // Verificamos si esta anotación (ej. @Service) tiene @Component
            if (annotation.annotationType().isAnnotationPresent(Component.class)) {

                // MAGIA: Buscamos qué método dentro de @Service tiene el @AliasFor
                String value = getValueFromAlias(annotation);
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
        }

        return null; // No se encontró nombre explícito
    }

    private static String getValueFromAlias(Annotation annotation) {
        try {
            // Recorremos los métodos de la anotación (ej. Service.value())
            for (Method method : annotation.annotationType().getDeclaredMethods()) {

                if (method.isAnnotationPresent(AliasFor.class)) {
                    AliasFor alias = method.getAnnotation(AliasFor.class);

                    // Verificamos que el alias apunte a Component.class y al atributo "value"
                    if (alias.annotation() == Component.class && "value".equals(alias.attribute())) {

                        // Invocamos el método en la instancia de la anotación
                        Object result = method.invoke(annotation);

                        if (result instanceof String stringResult) {
                            return  stringResult;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Loguear error si es necesario, pero mejor fallar silenciosamente y seguir buscando
        }
        return null;
    }
}