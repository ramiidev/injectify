package com.ramiidev.injectify.shared.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indica que se debe inyectar un valor de configuración en un campo o parámetro.
 * Soporta placeholders como "${app.name}" que serán resueltos contra el Environment.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {

    /**
     * La expresión del valor a inyectar (ej. "${my.property}" o un valor literal).
     */
    String value();
}
