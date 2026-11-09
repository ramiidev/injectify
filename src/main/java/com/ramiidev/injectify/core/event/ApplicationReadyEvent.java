package com.ramiidev.injectify.core.event;

/**
 * Evento que indica que Injectify ha terminado de cargar completamente
 * su contexto y la interfaz gráfica principal ya está visible.
 */
public class ApplicationReadyEvent extends ApplicationEvent {

    private final long startupTimeMs;

    public ApplicationReadyEvent(long startupTimeMs) {
        super();
        this.startupTimeMs = startupTimeMs;
    }

    public long getStartupTimeMs() {
        return startupTimeMs;
    }
}