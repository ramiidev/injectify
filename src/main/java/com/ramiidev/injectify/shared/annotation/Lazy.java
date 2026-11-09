package com.ramiidev.injectify.shared.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// AGREGAR SOPORTE PARA ANOTACIONES INTERNAS
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lazy {

    /**
     * Si es true, el bean se inicializará perezosamente (al pedirlo).
     * Si es false, se inicializará al arrancar (Eager).
     * Por defecto es true (si pones la etiqueta, asumes que quieres pereza).
     */
    boolean value() default true;
}
