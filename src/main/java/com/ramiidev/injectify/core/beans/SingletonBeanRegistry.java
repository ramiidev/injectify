package com.ramiidev.injectify.core.beans;

/**
 * Interfaz que define un registro para beans compartidos.
 * <p>
 * Permite registrar y recuperar instancias de objetos que deben existir como
 * una única instancia dentro del contenedor de IoC. Esta interfaz es fundamental
 * para la gestión del ciclo de vida y el almacenamiento de los componentes
 * principales de la aplicación.
 * </p>
 */
public interface SingletonBeanRegistry {

    /**
     * Registra un objeto singleton existente en el registro del contenedor.
     * <p>
     * El objeto dado debe estar completamente inicializado; el registro no realizará
     * ninguna inyección de dependencias ni callbacks de inicialización.
     * </p>
     *
     * @param beanName        El nombre del bean.
     * @param singletonObject La instancia del objeto singleton existente.
     */
    void registerSingleton(String beanName, Object singletonObject);

    /**
     * Devuelve la instancia del objeto singleton registrado bajo el nombre dado.
     * <p>
     * Este método permite el acceso a beans definidos manualmente o aquellos
     * que ya han sido creados por el contenedor.
     * </p>
     *
     * @param beanName El nombre del bean a buscar.
     * @return La instancia del objeto singleton, o {@code null} si no se encuentra.
     */
    Object getSingleton(String beanName);

    /**
     * Comprueba si este registro contiene una instancia singleton con el nombre dado.
     * <p>
     * Solo verifica instancias ya creadas; no verifica definiciones de beans
     * que aún no han sido instanciadas.
     * </p>
     *
     * @param beanName El nombre del bean a comprobar.
     * @return {@code true} si el singleton está registrado; de lo contrario {@code false}.
     */
    boolean containsSingleton(String beanName);
}