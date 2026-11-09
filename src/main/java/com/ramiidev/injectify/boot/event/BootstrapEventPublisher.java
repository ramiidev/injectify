package com.ramiidev.injectify.boot.event;

import java.util.ArrayList;
import java.util.List;

public final class BootstrapEventPublisher {

    private final List<BootstrapEventListener<?>> listeners = new ArrayList<>();

    public void register(BootstrapEventListener<?> listener) {
        listeners.add(listener);
    }

    @SuppressWarnings("unchecked")
    public void publish(BootstrapEvent event) {
        for (BootstrapEventListener<?> listener : listeners) {
            if (listener.supports(event)) {
                ((BootstrapEventListener<BootstrapEvent>) listener).onEvent(event);
            }
        }
    }
}
