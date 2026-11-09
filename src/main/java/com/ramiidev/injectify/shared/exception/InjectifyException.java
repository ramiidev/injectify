package com.ramiidev.injectify.shared.exception;

public abstract class InjectifyException extends RuntimeException{
    protected InjectifyException(String message) {
        super(message);
    }
    protected InjectifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
