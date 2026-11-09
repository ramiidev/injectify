package com.ramiidev.injectify.core.exception;

import com.ramiidev.injectify.shared.exception.InjectifyException;

public class BootstrapInitializationException extends InjectifyException {
    public BootstrapInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
