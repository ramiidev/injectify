package com.ramiidev.injectify.shared.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un método para que se ejecute una única vez después de que
 * se haya completado la inyección de dependencias para inicializar el componente.
 * El método no debe tener argumentos.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@SuppressWarnings({"FieldMayBeFinal", "unused"})
public @interface PostConstruct {}
