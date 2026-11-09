package com.ramiidev.injectify.configuration.exception;

import com.ramiidev.injectify.shared.exception.InjectifyException;

public class ConfigurationNotFoundException extends InjectifyException {
    public ConfigurationNotFoundException(String message) {
        super(message);
    }

    public ConfigurationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
