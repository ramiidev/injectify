package com.ramiidev.injectify.core.diagnostic;

public record FailureAnalysis(String description, String action, Throwable cause) {

}
