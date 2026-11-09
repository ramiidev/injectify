package com.ramiidev.injectify.boot.event;

public interface BootstrapEventListener<E extends BootstrapEvent> {
    boolean supports(BootstrapEvent event);
    void onEvent(E event);
}
