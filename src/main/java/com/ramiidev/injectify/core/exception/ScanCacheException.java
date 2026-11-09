package com.ramiidev.injectify.core.exception;

import com.ramiidev.injectify.shared.exception.InjectifyException;

public class ScanCacheException extends InjectifyException {

    public ScanCacheException(String message) {
        super(message);
    }

    public ScanCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
