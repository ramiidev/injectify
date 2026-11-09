package com.ramiidev.injectify.boot.phase;

import com.ramiidev.injectify.boot.BootstrapContext;
import com.ramiidev.injectify.boot.BootstrapPhase;
import com.ramiidev.injectify.configuration.env.MutableEnvironment;
import com.ramiidev.injectify.configuration.env.support.MapPropertySource;
import com.ramiidev.injectify.infrastructure.logging.support.DeferredLogFactory;
import com.ramiidev.injectify.infrastructure.logging.support.DeferredLogger;

import java.util.HashMap;
import java.util.Map;

public final class CliArgumentsPhase implements BootstrapPhase {

    private static final  String ARG_PREFIX = "--";

    private final DeferredLogger log = DeferredLogFactory.getLog(CliArgumentsPhase.class);
    private final String[] args;

    public CliArgumentsPhase(String... args) {
        this.args = args;
    }

    @Override
    public void run(BootstrapContext context) {
        if (args == null || args.length == 0) {
            return;
        }

        MutableEnvironment environment = context.getEnvironment();

        if (environment == null) {
            throw new IllegalStateException(
                    "No se puede procesar los argumentos CLI porque el Environment no ha sido inicializado. "
                            + "Ejecuta EnvironmentPhase antes de CliArgumentsPhase."
            );
        }

        Map<String, String> cliMap = new HashMap<>();

        for (String arg : args) {
            if (arg.startsWith(ARG_PREFIX) && arg.contains("=")) {
                String[] parts = arg.substring(2).split("=", 2);
                cliMap.put(parts[0], parts[1]);
            }
        }

        if (!cliMap.isEmpty()) {
            environment.addFirstPropertySource(new MapPropertySource(cliMap));
            log.debug("CLI args cargados: " + cliMap.size());
        }
    }
}
