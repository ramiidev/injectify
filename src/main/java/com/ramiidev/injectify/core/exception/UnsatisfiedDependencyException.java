package com.ramiidev.injectify.core.exception;

import java.lang.reflect.Type;

public class UnsatisfiedDependencyException extends BeansException {
    public UnsatisfiedDependencyException(String beanName, Class<?> dependencyType) {
        this(beanName, dependencyType, null, "punto de inyección desconocido", null);
    }

    public UnsatisfiedDependencyException(
            String beanName,
            Type dependencyType,
            String qualifier,
            String location,
            Throwable cause) {
        super(buildMessage(beanName, dependencyType, qualifier, location), cause);
    }

    private static String buildMessage(
            String beanName, Type dependencyType, String qualifier, String location) {
        String qualifierText = qualifier == null
                ? ""
                : ", qualifier '" + qualifier + "'";
        return "No se pudo resolver la dependencia del bean '" + beanName
                + "' en " + location + ": tipo requerido '" + dependencyType
                + "'" + qualifierText + ". Verifica que exista un único bean compatible.";
    }
}