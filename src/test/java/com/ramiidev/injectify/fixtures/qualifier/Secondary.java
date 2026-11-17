package com.ramiidev.injectify.fixtures.qualifier;

import com.ramiidev.injectify.shared.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier("secondaryProcessor")
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Secondary {
}
