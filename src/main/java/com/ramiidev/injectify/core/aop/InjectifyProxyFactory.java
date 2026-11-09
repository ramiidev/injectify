package com.ramiidev.injectify.core.aop;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

public class InjectifyProxyFactory {

    // Instancia compartida de Objenesis (es thread-safe)
    private static final Objenesis objenesis = new ObjenesisStd();

    public static Object createProxy(Object targetInstance) {
        Class<?> targetClass = targetInstance.getClass();

        try {
            // 1. Configuración del Builder (sin hacer make() todavía)
            DynamicType.Builder<?> builder = new ByteBuddy()
                    .subclass(targetClass)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(new ForwardingInterceptor(targetInstance)));

            // 2. [CORRECCIÓN] Usamos try-with-resources para el objeto 'Unloaded'.
            // Esto asegura que ByteBuddy limpie cualquier recurso temporal después de cargar la clase.
            try (DynamicType.Unloaded<?> unloaded = builder.make()) {

                Class<?> proxyClass = unloaded
                        // Usamos WRAPPER (lo que vimos antes para evitar warnings de Unsafe)
                        .load(targetClass.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                        .getLoaded();

                // 3. Instanciar sin constructor
                return objenesis.newInstance(proxyClass);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error creando proxy ByteBuddy para: " + targetClass.getName(), e);
        }
    }
}