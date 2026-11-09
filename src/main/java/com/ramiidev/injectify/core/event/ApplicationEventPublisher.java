package com.ramiidev.injectify.core.event;

import java.util.ArrayList;
import java.util.List;

/**
 * El motor que se encarga de recibir eventos y distribuirlos
 * a todos los listeners interesados en la fase de aplicación.
 */
public final class ApplicationEventPublisher {

    private final List<ApplicationEventListener<?>> listeners = new ArrayList<>();

    public void register(ApplicationEventListener<?> listener) {
        listeners.add(listener);
    }

    @SuppressWarnings("unchecked")
    public void publish(ApplicationEvent event) {
        for (ApplicationEventListener<?> listener : listeners) {
            if (listener.supports(event)) {
                ((ApplicationEventListener<ApplicationEvent>) listener).onEvent(event);
            }
        }
    }
}
