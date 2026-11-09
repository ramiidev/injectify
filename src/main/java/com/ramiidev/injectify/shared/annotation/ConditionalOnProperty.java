package com.ramiidev.injectify.shared.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConditionalOnProperty {

    /**
     * El nombre de la propiedad a comprobar. Ej: "app.db.type"
     */
    String name();

    /**
     * El valor esperado de la propiedad. Ej: "postgres".
     * Si se deja vacío, solo se comprueba que la propiedad exista (sin importar su valor).
     */
    String havingValue() default "";

    /**
     * Si es true, la condición se cumple (pasa) si la propiedad NO está definida.
     * Por defecto es false (si no está, no pasa).
     */
    boolean matchIfMissing() default false;
}
