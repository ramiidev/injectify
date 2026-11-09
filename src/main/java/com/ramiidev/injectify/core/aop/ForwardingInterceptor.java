package com.ramiidev.injectify.core.aop;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;

/**
 * Interceptor que redirige todas las llamadas del Proxy al objeto real.
 * Al ser una clase pública independiente, ByteBuddy no tiene problemas para verla.
 */
public class ForwardingInterceptor {

    private final Object target;

    public ForwardingInterceptor(Object target) {
        this.target = target;
    }

    @RuntimeType
    public Object intercept(@Origin Method method, @AllArguments Object[] args) throws Exception {

        try {
            return method.invoke(target, args);
        } catch (Exception e) {
            throw e;
        }
    }
}
