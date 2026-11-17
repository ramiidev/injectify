package com.ramiidev.injectify.core.beans.support;

import com.ramiidev.injectify.core.exception.BeanInitializationException;
import com.ramiidev.injectify.core.exception.BeanInstantiationException;
import com.ramiidev.injectify.core.exception.NoUniqueBeanDefinitionException;
import com.ramiidev.injectify.core.exception.UnsatisfiedDependencyException;
import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.Qualifier;
import com.ramiidev.injectify.shared.annotation.Value;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Resolves bean dependencies and reports the exact injection point on failure.
 */
final class DependencyResolver {

    private final DefaultBeanFactory beanFactory;

    DependencyResolver(DefaultBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    Constructor<?> findConstructorToUse(Class<?> beanClass, String beanName) {
        Constructor<?> selected = null;
        for (Constructor<?> constructor : beanClass.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                if (selected != null) {
                    throw new BeanInstantiationException(
                            beanClass,
                            "El bean '" + beanName + "' declara más de un constructor @Autowired."
                    );
                }
                selected = constructor;
            }
        }

        if (selected != null) {
            return selected;
        }

        try {
            return beanClass.getDeclaredConstructor();
        } catch (NoSuchMethodException exception) {
            throw new BeanInstantiationException(
                    beanClass,
                    "El bean '" + beanName + "' no tiene constructor por defecto ni un constructor @Autowired.",
                    exception
            );
        }
    }

    Object[] resolveParameters(String beanName, java.lang.reflect.Executable executable) {
        Parameter[] parameters = executable.getParameters();
        Object[] dependencies = new Object[parameters.length];

        for (int index = 0; index < parameters.length; index++) {
            Parameter parameter = parameters[index];
            String location = executable instanceof Method
                    ? "método '" + executable.getName() + "'"
                    : "constructor de '" + executable.getDeclaringClass().getName() + "'";
            dependencies[index] = resolve(
                    beanName,
                    parameter.getParameterizedType(),
                    parameter.getAnnotations(),
                    location + ", parámetro " + parameter.getName()
            );
        }
        return dependencies;
    }

    void populateBean(String beanName, Object beanInstance) {
        try {
            performSetterInjection(beanName, beanInstance);
            performFieldInjection(beanName, beanInstance);
        } catch (BeanInitializationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BeanInitializationException(
                    "No se pudo inyectar el bean '" + beanName + "' de tipo '"
                            + beanInstance.getClass().getName() + "'. Causa: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    private void performSetterInjection(String beanName, Object instance)
            throws IllegalAccessException, InvocationTargetException {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Autowired.class)) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                throw new BeanInitializationException(
                        "El bean '" + beanName + "' tiene el método @Autowired '"
                                + method.getName() + "' con " + method.getParameterCount()
                                + " parámetros; se requiere exactamente uno."
                );
            }

            Autowired autowired = method.getAnnotation(Autowired.class);
            Parameter parameter = method.getParameters()[0];
            Object dependency;
            try {
                dependency = resolve(
                        beanName,
                        parameter.getParameterizedType(),
                        parameter.getAnnotations(),
                        "setter '" + method.getName() + "', parámetro " + parameter.getName()
                );
            } catch (UnsatisfiedDependencyException exception) {
                if (!autowired.required()) {
                    continue;
                }
                throw exception;
            }

            method.setAccessible(true);
            try {
                method.invoke(instance, dependency);
            } catch (InvocationTargetException exception) {
                throw new BeanInitializationException(
                        "El setter @Autowired '" + method.getName() + "' del bean '"
                                + beanName + "' lanzó una excepción.",
                        exception.getCause()
                );
            }
        }
    }

    private void performFieldInjection(String beanName, Object instance)
            throws IllegalAccessException {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Value.class)) {
                Value value = field.getAnnotation(Value.class);
                Object resolved = beanFactory.resolveValue(value.value(), field.getType());
                field.setAccessible(true);
                field.set(instance, resolved);
                continue;
            }

            if (!field.isAnnotationPresent(Autowired.class)) {
                continue;
            }

            Autowired autowired = field.getAnnotation(Autowired.class);
            try {
                Object dependency = resolve(
                        beanName,
                        field.getGenericType(),
                        field.getAnnotations(),
                        "campo '" + field.getName() + "'"
                );
                field.setAccessible(true);
                field.set(instance, dependency);
            } catch (UnsatisfiedDependencyException exception) {
                if (autowired.required()) {
                    throw exception;
                }
            }
        }
    }

    private Object resolve(String beanName, Type type, Annotation[] annotations, String location) {
        String qualifier = extractQualifier(annotations);
        try {
            Object dependency = beanFactory.resolveDependencyByType(type, annotations);
            if (dependency == null) {
                throw missing(beanName, type, qualifier, location, null);
            }
            return dependency;
        } catch (NoUniqueBeanDefinitionException exception) {
            throw new UnsatisfiedDependencyException(
                    beanName,
                    type,
                    qualifier,
                    location,
                    exception
            );
        }
    }

    private UnsatisfiedDependencyException missing(
            String beanName, Type type, String qualifier, String location, Throwable cause) {
        return new UnsatisfiedDependencyException(
                beanName,
                type,
                qualifier,
                location,
                cause
        );
    }

    private String extractQualifier(Annotation[] annotations) {
        return Arrays.stream(annotations)
                .map(this::qualifierName)
                .filter(name -> name != null && !name.isBlank())
                .findFirst()
                .orElse(null);
    }

    private String qualifierName(Annotation annotation) {
        if (annotation.annotationType() == Qualifier.class) {
            return ((Qualifier) annotation).value();
        }
        Qualifier meta = annotation.annotationType().getAnnotation(Qualifier.class);
        if (meta == null) {
            return null;
        }
        if (!meta.value().isBlank()) {
            return meta.value();
        }
        String simpleName = annotation.annotationType().getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}
