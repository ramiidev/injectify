package com.ramiidev.injectify.shared.annotation.stereotype;

import com.ramiidev.injectify.shared.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface ExceptionHandler {

    @AliasFor(annotation = Component.class)
    String value() default "";
}
