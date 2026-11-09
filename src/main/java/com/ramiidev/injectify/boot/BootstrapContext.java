package com.ramiidev.injectify.boot;

import com.ramiidev.injectify.boot.event.BootstrapEventPublisher;
import com.ramiidev.injectify.configuration.env.MutableEnvironment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Un contexto de arranque simple responsable de cargar la configuración
 * inicial de la aplicación.
 * Su única misión es crear y preparar el ApplicationPropertySource.
 */
public final class BootstrapContext {

    private final Map<Class<?>, Object> bootstrapRegistry = new ConcurrentHashMap<>();
    private final BootstrapEventPublisher publisher = new BootstrapEventPublisher();
    private MutableEnvironment environment;

    /**
     * Permite a una fase guardar un objeto creado durante el arranque.
     */
    public <T> void register(Class<T> type, T instance) {
        bootstrapRegistry.put(type, instance);
    }

    // Nuevo método: Permite iterar sobre todo lo registrado de forma segura
    public void forEachRegistered(BiConsumer<Class<?>, Object> action) {
        bootstrapRegistry.forEach(action);
    }

    public BootstrapEventPublisher getPublisher() {
        return publisher;
    }

    public MutableEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(MutableEnvironment environment) {
        this.environment = environment;
    }


}