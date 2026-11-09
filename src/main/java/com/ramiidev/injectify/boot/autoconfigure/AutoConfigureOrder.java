package com.ramiidev.injectify.boot.autoconfigure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indica el orden de prioridad para cargar clases de auto-configuración.
 * Los valores más bajos tienen mayor prioridad (se cargan antes).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoConfigureOrder {

    /**
     * El valor del orden. Por defecto es 0 (Neutral).
     */
    int value() default DEFAULT_ORDER;

    // --- CONSTANTES DE ORDENAMIENTO ---

    /** Prioridad máxima. Para Properties y configuración de bajo nivel. */
    int ORDER_PROPERTIES = -100;

    /** Prioridad estándar. Para servicios del Core (Base de datos, Seguridad). */
    int ORDER_CORE = 0;

    /** Prioridad baja. Para UI, Ventanas y Controladores visuales. */
    int ORDER_UI = 100;

    int DEFAULT_ORDER = ORDER_CORE;
}
