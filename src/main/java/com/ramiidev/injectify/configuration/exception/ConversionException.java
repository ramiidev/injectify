package com.ramiidev.injectify.configuration.exception;

import com.ramiidev.injectify.shared.exception.InjectifyException;

public class ConversionException extends InjectifyException {
    public ConversionException(String message) {
        super(message);
    }
    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
