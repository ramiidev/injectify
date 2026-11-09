package com.ramiidev.injectify.core.exception;

public class BeanInitializationException extends BeansException {
    public BeanInitializationException(String message) {
        super(message);
    }

    public BeanInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
