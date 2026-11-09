package com.ramiidev.injectify.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indica que un campo es un objeto de configuración anidado.
 * El binder instanciará este objeto y procesará sus campos recursivamente,
 * añadiendo un prefijo adicional si se especifica.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NestedConfigurationProperty {
    /**
     * (Opcional) Define un prefijo específico para este grupo anidado.
     * Si se deja vacío, se usará el nombre del campo o la clave de @PropertyValue.
     */
    String prefix() default "";
}