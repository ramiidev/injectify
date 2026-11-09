package com.ramiidev.injectify.core.exception.support;

import com.ramiidev.injectify.boot.autoconfigure.InjectifyImportsLoader;
import com.ramiidev.injectify.core.diagnostic.FailureAnalysis;
import com.ramiidev.injectify.core.diagnostic.FailureAnalysisReporter;
import com.ramiidev.injectify.core.diagnostic.FailureAnalyzerRegistry;
import com.ramiidev.injectify.core.diagnostic.FailurePolicy;

import java.util.List;

public final class InjectifyExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final FailurePolicy failurePolicy;
    private final List<FailureAnalysisReporter> reporters;

    public InjectifyExceptionHandler(FailurePolicy failurePolicy) {
        this.failurePolicy = failurePolicy;
        this.reporters = InjectifyImportsLoader.loadFactories(FailureAnalysisReporter.class);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        process(e);
    }

    public void process(Throwable failure) {
        // 1. Analizamos la excepción COMPLETA
        FailureAnalysis analysis = FailureAnalyzerRegistry.analyze(failure);

        if (analysis != null) {
            // 2. Reportamos (Logging + UI si están configurados)
            for (FailureAnalysisReporter reporter : reporters) {
                try {
                    reporter.report(analysis);
                } catch (Exception ex) {
                    IO.println("Un reporter falló al intentar mostrar el error: " + reporter.getClass().getName());
                }
            }
            // 3. Aplicamos la política (Ej: System.exit)
            failurePolicy.apply(analysis);
        } else {
            // 4. Fallback tradicional si ningún analizador reconoció el error
            IO.println("Excepción no manejada por Injectify en el hilo: " + Thread.currentThread().getName());
            failure.printStackTrace();
            System.exit(1);
        }
    }
}