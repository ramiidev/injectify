package com.ramiidev.injectify.core.exception;

import com.ramiidev.injectify.shared.exception.InjectifyException;

public class BeansException extends InjectifyException {
    public BeansException(String message) {
        super(message);
    }

    public BeansException(String message, Throwable cause) {
        super(message, cause);
    }
}
