package com.ramiidev.injectify.boot;

import com.ramiidev.injectify.boot.autoconfigure.InjectifyImportsLoader;
import com.ramiidev.injectify.boot.event.BootstrapEventListener;

import java.util.ArrayList;
import java.util.List;

public final class BootstrapRunner {

    private final List<BootstrapPhase> phases = new ArrayList<>();

    public BootstrapRunner addPhase(BootstrapPhase phase) {
        phases.add(phase);
        return this;
    }

    public BootstrapContext run() {
        // 1. Nace el contexto (el estado compartido de la aplicación)
        BootstrapContext context = new BootstrapContext();

        // 2. CRÍTICO: Registramos los listeners ANTES de correr las fases.
        // Si no lo hacemos aquí, cuando EnvironmentPhase dispare el evento,
        // nadie estará escuchando.
        registerDefaultListeners(context);

        // 3. Ejecutamos las fases en orden (Banner -> Env -> CLI -> Logging -> etc.)
        for (BootstrapPhase phase : phases) {
            phase.run(context);
        }

        return context;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerDefaultListeners(BootstrapContext context) {
        // Cargar listeners definidos en META-INF/injectify.factories
        // Usamos (List) cast para evitar errores de compilación con genéricos
        List<BootstrapEventListener<?>> listeners =
                (List) InjectifyImportsLoader.loadFactories(BootstrapEventListener.class);

        for (BootstrapEventListener<?> listener : listeners) {
            context.getPublisher().register(listener);
            // Si quieres depurar:
            // System.out.println("[Internal] Listener registrado: " + listener.getClass().getSimpleName());
        }
    }
}
