package com.ramiidev.injectify.core.diagnostic;

import com.ramiidev.injectify.boot.autoconfigure.InjectifyImportsLoader;

import java.util.List;

public final class FailureAnalyzerRegistry {

    private static final List<FailureAnalyzer> ANALYZERS =
            InjectifyImportsLoader.loadFactories(FailureAnalyzer.class);

    private FailureAnalyzerRegistry() {}

    public static FailureAnalysis analyze(Throwable failure) {
        for (FailureAnalyzer analyzer : ANALYZERS) {
            FailureAnalysis analysis = analyzer.analyze(failure);
            if (analysis != null) {
                return analysis;
            }
        }
        return null;
    }
}
