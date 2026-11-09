package com.ramiidev.injectify.shared.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConditionalOnClass {

    /**
     * Clases que DEBEN estar presentes en el Classpath.
     * Úsalo si tienes la clase disponible en tiempo de compilación.
     */
    Class<?>[] value() default {};

    /**
     * Nombres totalmente calificados de las clases que DEBEN estar presentes.
     * Úsalo para dependencias opcionales que podrían no estar en el proyecto.
     * Ej: "com.itextpdf.kernel.pdf.PdfDocument"
     */
    String[] name() default {};
}