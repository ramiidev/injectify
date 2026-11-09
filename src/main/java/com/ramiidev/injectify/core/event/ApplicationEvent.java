package com.ramiidev.injectify.core.event;

/**
 * Clase base para todos los eventos del ciclo de vida de la aplicación.
 */
public abstract class ApplicationEvent {

    private final long timestamp;

    ApplicationEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
