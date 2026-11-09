package com.ramiidev.injectify.configuration.exception;

import com.ramiidev.injectify.shared.exception.InjectifyException;

public class ConfigurationBindingException extends InjectifyException {
    public ConfigurationBindingException(String message) {
        super(message);
    }

    public ConfigurationBindingException(String message, Throwable cause) {
        super(message, cause);
    }
}