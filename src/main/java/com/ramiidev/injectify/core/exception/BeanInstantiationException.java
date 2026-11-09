package com.ramiidev.injectify.core.exception;

public class BeanInstantiationException extends BeansException {
    public BeanInstantiationException(Class<?> beanClass, String message) {
        super("Fallo al instanciar el bean de clase '" + beanClass.getName() + "': " + message);
    }

    public BeanInstantiationException(Class<?> beanClass, String message, Throwable cause) {
        super("Fallo al instanciar el bean de clase '" + beanClass.getName() + "': " + message, cause);
    }
}
