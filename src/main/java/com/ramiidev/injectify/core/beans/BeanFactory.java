package com.ramiidev.injectify.core.beans;

/**
 * La interfaz raíz para acceder al contenedor de beans de la aplicación.
 * Es el contrato básico del cliente con el contenedor de IoC.
 */
public interface BeanFactory {

    /**
     * Obtiene un bean por su nombre.
     * @param name El nombre del bean.
     * @return La instancia del bean.
     */
    Object getBean(String name);

    /**
     * Obtiene un bean único por su tipo.
     * @param requiredType El tipo de clase del bean.
     * @return La instancia del bean.
     * @param <T> El tipo del bean.
     */
    <T> T getBean(Class<T> requiredType);

    /**
     * Obtiene un bean por su nombre y tipo.
     * @param name El nombre del bean.
     * @param requiredType El tipo de clase del bean.
     * @return La instancia del bean.
     * @param <T> El tipo del bean.
     */
    <T> T getBean(String name, Class<T> requiredType);

    String[] getBeanNamesForType(Class<?> type);

    void preInstantiateSingletons();
}
