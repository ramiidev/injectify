package com.ramiidev.injectify.shared.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface Autowired {

    /**
     * Declara si la dependencia es obligatoria.
     * @return true si es obligatoria (lanza excepción si falta), false si es opcional (se ignora).
     */
    boolean required() default true;
}
