package com.ramiidev.injectify.infrastructure.logging.support;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Fábrica central para crear loggers que funcionan antes de que
 * el sistema de logging real (Logback/Log4j2) esté inicializado.
 */
public class DeferredLogFactory {

    // Mantiene un registro de todos los loggers creados para poder activarlos luego
    private static final Queue<DeferredLogger> LOGGERS = new ConcurrentLinkedQueue<>();

    private DeferredLogFactory() {}

    /**
     * Crea un nuevo logger diferido asociado a la clase origen.
     * Úsalo en lugar de LoggerFactory.getLogger() durante la fase de bootstrap.
     */
    public static DeferredLogger getLog(Class<?> source) {
        DeferredLogger logger = new DeferredLogger(source);
        LOGGERS.add(logger);
        return logger;
    }

    // Método para obtener todos y activarlos
    public static Queue<DeferredLogger> getAll() {
        return LOGGERS;
    }

    // Método helper para limpiar referencias si hiciera falta
    public static void clear() {
        LOGGERS.clear();
    }
}
