package com.ramiidev.injectify.infrastructure.logging.support;

import com.ramiidev.injectify.infrastructure.logging.LogEvent;
import com.ramiidev.injectify.infrastructure.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class DeferredLogger {

    private final Class<?> sourceClass; // Guardamos quién soy
    private final Queue<LogEvent> events = new ConcurrentLinkedQueue<>();
    private Logger delegate;

    // Constructor package-private: Solo la Factory debe crear instancias
    DeferredLogger(Class<?> sourceClass) {
        this.sourceClass = sourceClass;
    }

    public void info(String message) {
        log(LogLevel.INFO, message, null);
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message, null);
    }

    public void warn(String message, Throwable ex) {
        log(LogLevel.WARN, message, ex);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message, null);
    }

    public void error(String message, Throwable ex) {
        log(LogLevel.ERROR, message, ex);
    }

    private void log(LogLevel level, String msg, Throwable ex) {
        Logger logger = this.delegate;

        if (logger != null) {
            // Si ya hay delegado, pasamos directo (Zero-overhead path)
            level.log(logger, msg, ex);
        } else {
            // Si no, al buffer
            events.add(new LogEvent(level, msg, ex));
        }
    }

    // ======================
    // Activación (Switchover)
    // ======================

    public void activate() {
        // Optimización rápida
        if (this.delegate != null) return;

        synchronized (this) {
            if (this.delegate != null) return;

            // Creamos el logger real usando la clase que guardamos al nacer
            Logger realLogger = LoggerFactory.getLogger(this.sourceClass);
            this.delegate = realLogger;

            // Reproducimos (Replay) los eventos en orden
            for (LogEvent event : events) {
                event.level().log(realLogger, event.message(), event.throwable());
            }
            // Liberamos la memoria del buffer
            events.clear();
        }
    }

    // Getter útil para la Factory
    public Class<?> getSourceClass() {
        return sourceClass;
    }
}
