package com.ramiidev.injectify.core.diagnostic;

@FunctionalInterface
public interface FailureAnalyzer {

    /**
     * Analiza una excepción y retorna un análisis si reconoce el error.
     * Retorna null si no sabe cómo manejar esta excepción.
     */
    FailureAnalysis analyze(Throwable failure);
}
