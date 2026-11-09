package com.ramiidev.injectify.core.event;

/**
 * Interfaz que deben implementar todos los componentes que deseen
 * escuchar eventos de la aplicación.
 */
public interface ApplicationEventListener<E extends ApplicationEvent> {

    /**
     * Determina si este listener está interesado en el evento publicado.
     */
    boolean supports(ApplicationEvent event);

    /**
     * Lógica a ejecutar cuando se recibe el evento.
     */
    void onEvent(E event);
}
