package com.ramiidev.injectify.core.exception;

import com.ramiidev.injectify.shared.exception.InjectifyException;

public class UnsatisfiedDependencyException extends InjectifyException {
    public UnsatisfiedDependencyException(String beanName, Class<?> dependencyType) {
        super("No se pudo resolver la dependencia obligatoria de tipo '" + dependencyType.getName() + "' para el bean/componente '" + beanName + "'.");
    }
}