package com.ramiidev.injectify.boot.autoconfigure;

import com.ramiidev.injectify.shared.annotation.stereotype.Component;
import com.ramiidev.injectify.shared.annotation.stereotype.Configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación principal para arrancar una aplicación Injectify.
 * <p>
 * Incluye automáticamente:
 * - @Configuration: Marca la clase como fuente de beans.
 * - @Component: Permite que sea escaneada.
 * - @EnableAutoConfiguration: Habilita la carga de configuraciones por defecto.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Configuration
@Component
@EnableAutoConfiguration
public @interface StartInjectifyApplication {
    /**
     * Paquete base para escanear componentes. Si se deja vacío, se usa el paquete de la clase anotada.
     */
    String value() default "";
}
