package com.ramiidev.injectify.shared.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AliasFor {

    /**
     * La anotación objetivo a la que estamos haciendo alias (ej. Component.class).
     */
    Class<? extends Annotation> annotation() default Annotation.class;

    /**
     * El nombre del atributo en la anotación objetivo (por defecto "value").
     */
    String attribute() default "value";
}