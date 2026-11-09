package com.ramiidev.injectify.core.diagnostic;

import com.ramiidev.injectify.boot.autoconfigure.InjectifyImportsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class FailureAnalysisReporterT {

    private static final Logger log = LoggerFactory.getLogger(FailureAnalysisReporterT.class);

    private FailureAnalysisReporterT() {}

    /**
     * Este es el método que llamarás desde tu Main cuando todo explote.
     */
    public static boolean reportException(Throwable failure) {
        // 1. Cargamos todos los analizadores registrados en los JARs
        List<FailureAnalyzer> analyzers = InjectifyImportsLoader.loadFactories(FailureAnalyzer.class);

        // 2. Preguntamos a cada uno si reconoce el error
        for (FailureAnalyzer analyzer : analyzers) {
            FailureAnalysis analysis = analyzer.analyze(failure);

            if (analysis != null) {
                // 3. ¡Bingo! Alguien reconoció el error. Lo imprimimos bonito.
                printAnalysis(analysis);
                return true; // Reportado con éxito
            }
        }

        // Nadie supo qué pasó, retornamos false para que se imprima el stacktrace normal
        return false;
    }

    private static void printAnalysis(FailureAnalysis analysis) {
        StringBuilder builder = new StringBuilder(256);
        String logMessage = builder
                .append("\n\n")
                .append("***************************\n")
                .append("APPLICATION FAILED TO START\n")
                .append("***************************\n\n")
                .append("Description:\n")
                .append(analysis.description())
                .append("\n\n")
                .append("Action:\n")
                .append(analysis.action())
                .append("\n")
                .toString();

        log.error(logMessage);
    }
}
