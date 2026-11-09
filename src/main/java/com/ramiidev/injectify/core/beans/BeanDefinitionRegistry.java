package com.ramiidev.injectify.core.beans;

import com.ramiidev.injectify.core.beans.definition.BeanDefinition;

/**
 * Interfaz para los registros que contienen definiciones de beans (BeanDefinition).
 */
public interface BeanDefinitionRegistry {

    /**
     * Registra una nueva definición de bean en el registro.
     * @param beanName El nombre del bean.
     * @param beanDefinition El objeto BeanDefinition.
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);

    void registerSingleton(String beanName, Object singletonObject);

    /**
     * Obtiene la definición de un bean por su nombre.
     * @param beanName El nombre del bean.
     * @return El BeanDefinition.
     */
    BeanDefinition getBeanDefinition(String beanName);

    /**
     * Comprueba si una definición de bean con ese nombre ya está registrada.
     * @param beanName El nombre del bean.
     * @return true si ya existe.
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * Devuelve los nombres de todas las definiciones de beans registradas.
     * @return Un array de strings con los nombres de los beans.
     */
    String[] getBeanDefinitionNames();

    int getBeanDefinitionCount();
}
