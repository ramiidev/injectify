package com.ramiidev.injectify.boot.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Habilita el mecanismo de auto-configuración de Injectify.
 * <p>
 * Esto le dice al framework que busque el archivo 'META-INF/injectify.factories'
 * y cargue las configuraciones predeterminadas (beans por defecto) que
 * coincidan con las librerías presentes en el classpath.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableAutoConfiguration {
}
