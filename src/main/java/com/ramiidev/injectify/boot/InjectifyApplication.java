package com.ramiidev.injectify.boot;

import com.ramiidev.injectify.boot.phase.BannerPhase;
import com.ramiidev.injectify.boot.phase.CliArgumentsPhase;
import com.ramiidev.injectify.boot.phase.EnvironmentPhase;
import com.ramiidev.injectify.configuration.env.Environment;
import com.ramiidev.injectify.core.beans.lifecycle.DisposableBean;
import com.ramiidev.injectify.core.context.ApplicationContext;
import com.ramiidev.injectify.core.context.support.AnnotationConfigAbstractApplicationContext;
import com.ramiidev.injectify.core.convert.support.ApplicationConversionService;
import com.ramiidev.injectify.core.convert.support.ConversionService;
import com.ramiidev.injectify.core.diagnostic.ExitFailurePolicy;
import com.ramiidev.injectify.core.exception.support.InjectifyExceptionHandler;
import com.ramiidev.injectify.infrastructure.logging.support.DeferredLogFactory;
import com.ramiidev.injectify.infrastructure.logging.support.DeferredLogger;

import java.beans.Introspector;

/**
 * El lanzador principal del framework. Proporciona un punto de entrada
 * simple y único para iniciar la aplicación, orquestando el proceso de
 * arranque en dos fases.
 */
public final class InjectifyApplication {

    private static final DeferredLogger LOG = DeferredLogFactory.getLog(InjectifyApplication.class);
    private static Class<?> primarySource;
    private static ApplicationContext context;

    // Se bloquea la instanciación por completo
    private InjectifyApplication() {
    }

    /**
     * Atajo clásico para arrancar la app sin configuración explícita.
     */
    public static void run(Class<?> primarySource, String[] args) {
        InjectifyApplication.primarySource = primarySource;
        run(args);
    }

    public static ApplicationContext getContext() {
        if (context == null) {
            throw new IllegalStateException("El contexto de la aplicación aún no ha sido inicializado.");
        }
        return context;
    }

    /**
     * El núcleo de ejecución real.
     * Tiene visibilidad 'package-private' para que solo InjectifyApplicationBuilder
     * pueda invocarlo, manteniendo tu API pública inmaculada.
     */
    static void run(String[] args) {

        InjectifyExceptionHandler exceptionHandler = new InjectifyExceptionHandler(new ExitFailurePolicy());
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);

        try {
            // 1. BOOTSTRAP (Fase Rápida - Estilos y Entorno)
            ConversionService conversionService = ApplicationConversionService.getSharedInstance();
            BootstrapContext bootstrapContext = new BootstrapRunner()
                    .addPhase(new BannerPhase())
                    .addPhase(new EnvironmentPhase(conversionService))
                    .addPhase(new CliArgumentsPhase(args))
                    .run();
            Environment environment = bootstrapContext.getEnvironment();

            // 3. CREACIÓN DEL CONTEXTO PESADO (IoC Container)
            context = new AnnotationConfigAbstractApplicationContext(
                    environment,
                    conversionService,
                    primarySource
            );

            bootstrapContext.forEachRegistered((type, instance) -> {
                String beanName = Introspector.decapitalize(type.getSimpleName());
                context.registerSingleton(beanName, instance);
                LOG.debug("Registrado Singleton de Bootstrap -> %s : %s"
                        .formatted(beanName, type.getName()));
            });

            // Arranca la Inyección de Dependencias
            context.refresh();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (context instanceof DisposableBean disposableBean) {
                    disposableBean.destroy();
                }
            }));

        } catch (Exception e) {
            exceptionHandler.process(e);
            Thread.currentThread().interrupt();
        }
    }
}

