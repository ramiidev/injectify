package com.ramiidev.injectify.infrastructure.logging;

public record LogEvent(LogLevel level, String message, Throwable throwable) {
}
