package com.ramiidev.injectify.configuration.annotation;

import com.ramiidev.injectify.boot.autoconfigure.AutoConfigureOrder;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
@AutoConfigureOrder(AutoConfigureOrder.ORDER_PROPERTIES)
public @interface ConfigurationProperties {
    /**
     * El prefijo principal de la aplicación (ej. "injectify").
     */
    String prefix() default "injectify";

    /**
     * El nombre de la sección de propiedades (ej. "app", "database").
     */
    String value();
}
