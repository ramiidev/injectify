package com.ramiidev.injectify.boot.event;

import com.ramiidev.injectify.configuration.env.MutableEnvironment;

public final class EnvironmentPreparedEvent implements BootstrapEvent {
    private final MutableEnvironment environment;

    public EnvironmentPreparedEvent(MutableEnvironment environment) {
        this.environment = environment;
    }

    public MutableEnvironment getEnvironment() {
        return environment;
    }
}
