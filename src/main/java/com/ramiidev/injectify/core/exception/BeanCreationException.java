package com.ramiidev.injectify.core.exception;

public class BeanCreationException extends BeansException {
    public BeanCreationException(String beanName, String msg) {
        super("Error creando el bean '" + beanName + "': " + msg);
    }

    public BeanCreationException(String beanName, String msg, Throwable cause) {
        super("Error creando el bean '" + beanName + "': " + msg, cause);
    }
}