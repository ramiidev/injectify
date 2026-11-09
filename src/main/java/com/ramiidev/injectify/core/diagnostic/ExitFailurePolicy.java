package com.ramiidev.injectify.core.diagnostic;

public class ExitFailurePolicy implements FailurePolicy {
    @Override
    public void apply(FailureAnalysis analysis) {
        // Podrías registrar un hook aquí si necesitas limpiar recursos de Injectify
        System.exit(1);
    }
}
