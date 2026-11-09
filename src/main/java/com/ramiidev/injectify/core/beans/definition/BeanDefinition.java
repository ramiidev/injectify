package com.ramiidev.injectify.core.beans.definition;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Contiene la metadata de un bean. Es la "receta" que el framework
 * utiliza para saber cómo crear, configurar e inyectar un componente.
 */
public class BeanDefinition {

    private final String beanName;
    private final Class<?> beanClass;

    /**
     * Este es el campo MÁGICO para los genéricos.
     * Si tienes una clase WindowStageManager<LoginState>,
     * beanClass será WindowStageManager.class
     * resolvableType será ParameterizedTypeImpl(WindowStageManager<LoginState>)
     */
    private final Type resolvableType;
    private String factoryMethodName;
    private String factoryBeanName;
    private boolean isSingleton = true;
    private BeanScope scope = BeanScope.SINGLETON;

    // Por defecto es FALSE (Eager / Ansioso).
    // Queremos que las cosas fallen rápido al iniciar si algo está mal.
    private boolean lazyInit = false;

    // =================================================================================
    // CONSTRUCTORES
    // =================================================================================

    /**
     * Constructor completo para definiciones con genéricos específicos.
     * Útil para Factory Methods o registro manual con tipos complejos.
     */
    public BeanDefinition(String beanName, Class<?> beanClass, Type resolvableType) {
        this.beanName = Objects.requireNonNull(beanName, "El nombre del bean no puede ser nulo");
        this.beanClass = Objects.requireNonNull(beanClass, "La clase del bean no puede ser nula");
        this.resolvableType = resolvableType != null ? resolvableType : beanClass;
    }

    /**
     * Constructor de conveniencia para beans simples (sin genéricos complejos).
     */
    public BeanDefinition(String beanName, Class<?> beanClass) {
        this(beanName, beanClass, beanClass);
    }

    // =================================================================================
    // GETTERS Y SETTERS
    // =================================================================================

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public Type getResolvableType() {
        return resolvableType;
    }

    public String getFactoryMethodName() {
        return factoryMethodName;
    }

    public void setFactoryMethodName(String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public boolean isFromFactoryMethod() {
        return factoryMethodName != null;
    }

    public boolean isSingleton() {
        return isSingleton;
    }

    // =================================================================================
    // MANEJO DEL SCOPE
    // =================================================================================

    public BeanScope getScope() {
        return scope;
    }

    public void setScope(BeanScope scope) {
        this.scope = scope;
        this.isSingleton = (scope == BeanScope.SINGLETON);
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "beanName='" + beanName + '\'' +
                ", beanClass=" + beanClass +
                ", resolvableType=" + resolvableType +
                ", factoryMethodName='" + factoryMethodName + '\'' +
                ", factoryBeanName='" + factoryBeanName + '\'' +
                ", isSingleton=" + isSingleton +
                ", scope=" + scope +
                '}';
    }
}