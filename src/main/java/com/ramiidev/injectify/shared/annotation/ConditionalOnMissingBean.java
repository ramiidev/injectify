package com.ramiidev.injectify.shared.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indica que un bean solo debe registrarse si NO existe ya un bean
 * del tipo especificado (o del mismo tipo que el método) en el ApplicationContext.
 * <p>
 * Es útil para definir configuraciones por defecto que el usuario puede sobrescribir
 * simplemente definiendo su propio bean.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ConditionalOnMissingBean {

    /**
     * El tipo de bean que se debe verificar.
     * Si no se especifica, se infiere del tipo de retorno del método.
     */
    Class<?>[] value() default {};

    String[] name() default {};
}