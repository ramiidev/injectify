package com.ramiidev.injectify.infrastructure.logging;

/**
 * Abstracción para inicializar sistemas de logging (Log4j2, Logback, Java Util Logging).
 */
public interface LoggingSystem {

    /**
     * Configura el sistema de logging basado en el entorno.
     */
    void initialize(LogSettings settings);

}