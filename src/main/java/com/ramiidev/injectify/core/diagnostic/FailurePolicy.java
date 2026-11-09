package com.ramiidev.injectify.core.diagnostic;

@FunctionalInterface
public interface FailurePolicy {
    void apply(FailureAnalysis analysis);
}
