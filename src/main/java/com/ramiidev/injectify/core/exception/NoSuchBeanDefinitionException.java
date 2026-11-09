package com.ramiidev.injectify.core.exception;

public class NoSuchBeanDefinitionException extends BeansException {
    public NoSuchBeanDefinitionException(String name) {
        super("No se encontró la definición para el bean: " + name);
    }
}