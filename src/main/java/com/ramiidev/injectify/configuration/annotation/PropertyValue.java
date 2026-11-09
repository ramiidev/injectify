package com.ramiidev.injectify.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anota un campo dentro de una clase @ConfigurationProperties
 * para vincularlo directamente a una clave específica del archivo
 * application.properties.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PropertyValue {
    /**
     * Para mapear una clave específica. Sobrescribe la convención de nombres.
     * Ejemplo: @PropertyValue(key = "persistence-unit-name") o
     * también @PropertyValue(key = ".*") junto a un Properties para agrupar
     * propiedades huerfanas.
     */
    String key() default "";

    /**
     * Para mapear una clave específica. Sobrescribe la convención de nombres.
     * Ejemplo: @PropertyValue(value = "persistence-unit-name") o
     * también @PropertyValue(value = ".*") junto a un Properties para agrupar
     * propiedades huerfanas.
     */
    String regex() default "";
}