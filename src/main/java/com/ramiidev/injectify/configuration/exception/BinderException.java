package com.ramiidev.injectify.configuration.exception;

import com.ramiidev.injectify.shared.exception.InjectifyException;

public class BinderException extends InjectifyException {

    public BinderException(String message) {
        super(message);
    }

    public BinderException(String message, Throwable cause) {
        super(message, cause);
    }
}
