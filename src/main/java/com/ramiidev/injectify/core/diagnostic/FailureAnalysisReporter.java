package com.ramiidev.injectify.core.diagnostic;

@FunctionalInterface
public interface FailureAnalysisReporter {
    void report(FailureAnalysis analysis);
}
