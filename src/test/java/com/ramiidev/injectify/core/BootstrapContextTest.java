package com.ramiidev.injectify.core;

import com.ramiidev.injectify.boot.BootstrapContext;
import com.ramiidev.injectify.boot.BootstrapRunner;
import com.ramiidev.injectify.boot.phase.CliArgumentsPhase;
import com.ramiidev.injectify.boot.phase.EnvironmentPhase;
import com.ramiidev.injectify.configuration.env.MutableEnvironment;
import com.ramiidev.injectify.configuration.env.support.DefaultEnvironment;
import com.ramiidev.injectify.configuration.env.support.MapPropertySource;
import com.ramiidev.injectify.core.convert.support.ApplicationConversionService;
import com.ramiidev.injectify.core.convert.support.ConversionService;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BootstrapContextTest {

    @Test
    void testCliArgumentsOverrideFiles() {
        // 1. SETUP: Arrancamos normal (leyendo application.properties si existe)
        BootstrapContext ctx = new BootstrapRunner()
                .addPhase(new EnvironmentPhase(ApplicationConversionService.getSharedInstance()))
                .addPhase(new CliArgumentsPhase("--injectify.logging.level=TRACE"))
                .run();

        // 3. ASSERT: Verificamos que el argumento CLI ganó
        String level = ctx.getEnvironment().getProperty("injectify.logging.level");
        assertEquals("TRACE", level, "El argumento CLI debería sobrescribir todo");
    }

    @Test
    void testPureEnvironmentWithoutFiles() {
        // 1. SETUP: Creamos un entorno "vacío" o controlado para el test
        // Usamos un mapa base vacío en lugar de leer el disco
        ConversionService conversionService = ApplicationConversionService.getSharedInstance();
        MapPropertySource emptySource = new MapPropertySource(Collections.emptyMap());
        MutableEnvironment mockEnv = new DefaultEnvironment(emptySource, conversionService);

        // 2. El entorno debe existir antes de procesar argumentos CLI.
        BootstrapContext ctx = new BootstrapContext();
        new EnvironmentPhase(conversionService).run(ctx);
        ctx.setEnvironment(mockEnv);
        new CliArgumentsPhase("--app.name=TestUnitario").run(ctx);

        // 4. ASSERT
        assertEquals("TestUnitario", ctx.getEnvironment().getProperty("app.name"));
    }

    @Test
    void testCliArgumentsFailClearlyWithoutEnvironment() {
        BootstrapContext ctx = new BootstrapContext();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new CliArgumentsPhase("--app.name=TestUnitario").run(ctx)
        );

        assertEquals(
                "No se puede procesar los argumentos CLI porque el Environment no ha sido inicializado. "
                        + "Ejecuta EnvironmentPhase antes de CliArgumentsPhase.",
                exception.getMessage()
        );
    }
}
