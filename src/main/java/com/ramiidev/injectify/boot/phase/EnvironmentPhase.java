package com.ramiidev.injectify.boot.phase;

import com.ramiidev.injectify.boot.BootstrapContext;
import com.ramiidev.injectify.boot.BootstrapPhase;
import com.ramiidev.injectify.boot.event.EnvironmentPreparedEvent;
import com.ramiidev.injectify.configuration.env.MutableEnvironment;
import com.ramiidev.injectify.configuration.env.support.ApplicationPropertySource;
import com.ramiidev.injectify.configuration.env.support.DefaultEnvironment;
import com.ramiidev.injectify.core.convert.support.ConversionService;
import com.ramiidev.injectify.infrastructure.logging.support.DeferredLogFactory;
import com.ramiidev.injectify.infrastructure.logging.support.DeferredLogger;

public final class EnvironmentPhase implements BootstrapPhase {

    private final DeferredLogger log = DeferredLogFactory.getLog(EnvironmentPhase.class);
    private final ConversionService conversionService;

    public EnvironmentPhase(ConversionService conversionService) {
        this.conversionService = conversionService;

    }

    @Override
    public void run(BootstrapContext context) {

        log.debug("Inicializando environment");

        ApplicationPropertySource appSource = new ApplicationPropertySource();
        MutableEnvironment env = new DefaultEnvironment(appSource, conversionService);
        context.setEnvironment(env);

        log.info("Environment cargado correctamente");

        context.getPublisher().publish(new EnvironmentPreparedEvent(env));
    }
}