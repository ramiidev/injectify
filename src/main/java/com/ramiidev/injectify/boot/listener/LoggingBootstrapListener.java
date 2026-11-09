package com.ramiidev.injectify.boot.listener;

import com.ramiidev.injectify.boot.event.BootstrapEvent;
import com.ramiidev.injectify.boot.event.BootstrapEventListener;
import com.ramiidev.injectify.boot.event.EnvironmentPreparedEvent;
import com.ramiidev.injectify.configuration.env.Environment;
import com.ramiidev.injectify.infrastructure.logging.LogSettings;
import com.ramiidev.injectify.infrastructure.logging.LoggingSystem;
import com.ramiidev.injectify.infrastructure.logging.support.DeferredLogFactory;
import com.ramiidev.injectify.infrastructure.logging.support.DeferredLogger;
import com.ramiidev.injectify.infrastructure.logging.support.Log4J2LoggingSystem;

public final class LoggingBootstrapListener implements BootstrapEventListener<EnvironmentPreparedEvent> {

    @Override
    public boolean supports(BootstrapEvent event) {
        return event instanceof EnvironmentPreparedEvent;
    }

    @Override
    public void onEvent(EnvironmentPreparedEvent event) {
        Environment environment = event.getEnvironment();
        LogSettings logSettings = LogSettings.from(environment);

        LoggingSystem loggingSystem = new Log4J2LoggingSystem();
        loggingSystem.initialize(logSettings);

        DeferredLogFactory.getAll().forEach(DeferredLogger::activate);
        DeferredLogFactory.clear();
    }
}
