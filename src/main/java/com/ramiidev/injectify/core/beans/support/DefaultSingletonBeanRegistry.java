package com.ramiidev.injectify.core.beans.support;

import com.ramiidev.injectify.core.beans.SingletonBeanRegistry;
import com.ramiidev.injectify.core.exception.BeanCreationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Implementación base para la gestión de beans singleton.
 * <p>
 * Esta clase proporciona la infraestructura fundamental para el registro y recuperación de beans singleton.
 * Su característica más importante es la implementación de un sistema de <strong>Caché de Tres Niveles</strong>
 * diseñado para resolver dependencias circulares (A necesita a B, y B necesita a A) sin bloquear la aplicación.
 * </p>
 * <p>
 * Los tres niveles de caché son:
 * <ol>
 * <li><strong>Nivel 1 (singletonObjects):</strong> Contiene beans completos, totalmente inicializados y listos para usar.</li>
 * <li><strong>Nivel 2 (earlySingletonObjects):</strong> Contiene beans que han sido instanciados pero aún no poblados o inicializados (referencias tempranas).</li>
 * <li><strong>Nivel 3 (singletonFactories):</strong> Contiene fábricas (ObjectFactory) capaces de crear la referencia temprana de un bean si es necesario.</li>
 * </ol>
 * </p>
 */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

    /**
     * Caché de objetos singleton finales. Aquí se almacenan los beans que ya completaron
     * su ciclo de creación
     * <p>
     * Map<String, Object> donde:
     * <ul>
     * <li>La clave de tipo {@link String} representa el nombre lógico del bean.</li>
     * <li>El valor de tipo {@link Object} es la instancia final del singleton ya inicializado.</li>
     * </ul>
     * </p>
     */
    protected final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /**
     * Caché de objetos singleton tempranos. Se utiliza para exponer una referencia a un bean antes
     * de que esté completamente listo, permitiendo resolver dependencias circulares.
     * <p>
     * Map<String, Object> donde:
     * <ul>
     * <li>La clave de tipo {@link String} representa el nombre lógico del bean.</li>
     * <li>El valor de tipo {@link Object} es la instancia temprana del singleton.</li>
     * </ul>
     * </p>
     */
    protected final Map<String, Object> earlySingletonObjects = HashMap.newHashMap(16);

    /**
     * Caché de fábricas de singletons. Aquí se almacenan las fábricas capaces de crear
     * la instancia temprana de un bean durante la resolución de dependencias cíclicas.
     * <p>
     * Map<String, Object> donde:
     * <ul>
     * <li>La clave de tipo {@link String} representa el nombre lógico del bean.</li>
     * <li>El valor de tipo {@link Supplier}&lt;{@link Object}&gt;
     *     es la fábrica que genera la instancia temprana del singleton.</li>
     * </ul>
     */
    protected final Map<String, Supplier<Object>> singletonFactories = HashMap.newHashMap(16);

    /**
     * Conjunto de nombres de beans que están actualmente en proceso de creación.
     * Se utiliza para detectar dependencias circulares.
     * Cada elemento del conjunto es un {@link String} que representa el nombre lógico del bean.
     */
    protected final Set<String> singletonsCurrentlyInCreation =
            Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    /**
     * Implementación estándar de registro.
     * <p>
     * Este método es <strong>Thread-Safe</strong>. Sincroniza en el mapa de singletons
     * para asegurar que el estado del caché sea consistente al añadir manualmente un objeto.
     * También se encarga de eliminar cualquier referencia temprana del bean si existía.
     * </p>
     */
    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        addSingleton(beanName, singletonObject);
    }

    /**
     * Devuelve el objeto singleton registrado o en proceso de creación.
     * <p>
     * Esta implementación llama internamente a {@link #getSingleton(String, boolean)} pasando
     * {@code true} como segundo argumento. Esto significa que este método <strong>soporta
     * la resolución de dependencias circulares</strong>, ya que está dispuesto a devolver
     * una referencia a un bean que aún se está construyendo (referencia temprana).
     * </p>
     */
    @Override
    public Object getSingleton(String beanName) {
        return getSingleton(beanName, true);
    }

    /**
     * Comprueba la existencia del singleton en el caché principal.
     * <p>
     * Verifica exclusivamente en el mapa {@code singletonObjects} (Nivel 1).
     * No considera beans que estén actualmente en construcción o en fábricas.
     * </p>
     */
    @Override
    public boolean containsSingleton(String beanName) {
        return this.singletonObjects.containsKey(beanName);
    }

    /**
     * El método central para obtener o crear un bean singleton.
     * Orquesta el uso de los 3 niveles de caché para garantizar que el bean se cree una sola vez
     * y que las dependencias circulares se resuelvan correctamente.
     *
     * @param beanName El nombre del bean a obtener o crear.
     * @param singletonFactory La fábrica (lambda) que contiene la lógica para crear la instancia del bean si no existe.
     * @return La instancia del bean singleton.
     * @throws BeanCreationException Si ocurre un error durante la creación del bean.
     */
    public Object getSingleton(String beanName, Supplier<Object> singletonFactory) {
        // 1. Intenta obtener del caché de Nivel 1 (Ruta rápida)
        Object singletonObject = this.singletonObjects.get(beanName);

        if (singletonObject == null) {
            // Bloqueo para asegurar creación única en entorno concurrente
            synchronized (this.singletonObjects) {
                // Doble comprobación (Double-check locking)
                singletonObject = this.singletonObjects.get(beanName);

                if (singletonObject == null) {
                    // 2. Marcar el bean como "En Creación" para detectar ciclos futuros
                    beforeSingletonCreation(beanName);

                    try {
                        // 3. Registrar la fábrica en el Nivel 3.
                        // Esto permite que si este bean se necesita a sí mismo (cíclicamente) durante su creación,
                        // se pueda obtener una referencia temprana.
                        addSingletonFactory(beanName, singletonFactory);

                        // 4. Ejecutar la creación real.
                        // Llamamos a getSingleton(beanName, true) que buscará en los niveles.
                        // Si hay un ciclo, encontrará la fábrica que acabamos de registrar.
                        singletonObject = getSingleton(beanName, true);

                        if (singletonObject == null) {
                            throw new BeanCreationException(beanName, "La fábrica interna devolvió null, lo cual es inesperado.");
                        }
                    } catch (BeanCreationException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        throw new BeanCreationException(beanName, "Fallo instanciando singleton.", ex);
                    } finally {
                        // 5. Limpiar la marca de "En Creación" independientemente del resultado
                        afterSingletonCreation(beanName);
                    }

                    // 6. Promover a Nivel 1 (Completado).
                    // El bean ya está listo, se guarda en singletonObjects y se limpia de los cachés temporales.
                    addSingleton(beanName, singletonObject);
                }
            }
        }
        return singletonObject;
    }

    /**
     * Método interno para buscar un bean en los tres niveles de caché.
     * Es el corazón de la resolución de ciclos de dependencia.
     *
     * @param beanName El nombre del bean.
     * @param allowEarlyReference Si se permite devolver una referencia temprana (del Nivel 2 o creada desde Nivel 3).
     * @return La instancia del objeto o null si no se encuentra.
     */
    // SUPPRESS: SonarQube sugiere usar computeIfAbsent, pero esto rompería la lógica
    // de recursividad y el manejo explícito de los 3 niveles de caché necesario aquí.
    @SuppressWarnings("squid:S3824")
    protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        // 1. Busca en el caché final (Nivel 1) - El caso más común
        Object singletonObject = this.singletonObjects.get(beanName);

        // 2. Si no se encuentra y el bean está marcado como "en creación" (ciclo detectado)
        // allowEarlyReference originalmente se validaba aqui.
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            // Se agrego el synchronized para evitar problemas de concurrencia
            synchronized (this.singletonObjects) {
                // Busca en el caché temprano (Nivel 2) - Referencias ya expuestas
                singletonObject = this.earlySingletonObjects.get(beanName);

                if (singletonObject == null && allowEarlyReference) {
                    // 3. Busca en el caché de fábricas (Nivel 3)
                    Supplier<Object> singletonFactory = this.singletonFactories.get(beanName);
                    if (singletonFactory != null) {
                        // EJECUTA la fábrica para obtener la referencia temprana (instancia sin dependencias)
                        singletonObject = singletonFactory.get();

                        // Mueve de Nivel 3 (Fábrica) a Nivel 2 (Objeto Temprano)
                        // Esto asegura que si hay múltiples dependencias circulares al mismo bean,
                        // todas reciban la MISMA instancia temprana, no nuevas instancias.
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        this.singletonFactories.remove(beanName);
                    }
                }
            }
        }
        return singletonObject;
    }

    /**
     * Añade una fábrica al caché de Nivel 3 si el bean no existe en el Nivel 1.
     * Esto expone la posibilidad de crear una referencia temprana del bean.
     *
     * @param beanName El nombre del bean.
     * @param singletonFactory La fábrica para crear la instancia.
     */
    protected void addSingletonFactory(String beanName, Supplier<Object> singletonFactory) {
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonFactories.put(beanName, singletonFactory);
                this.earlySingletonObjects.remove(beanName);
            }
        }
    }

    /**
     * Registra un bean completamente formado en el caché de Nivel 1.
     * Elimina cualquier rastro del bean en los cachés temporales (Nivel 2 y 3).
     *
     * @param beanName El nombre del bean.
     * @param singletonObject La instancia finalizada del bean.
     */
    protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, singletonObject);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
        }
    }

    /**
     * Marca un bean como "actualmente en creación".
     *
     * @param beanName El nombre del bean.
     * @throws IllegalStateException Si el bean ya estaba marcado como en creación (ciclo no resoluble o error lógico).
     */
    protected void beforeSingletonCreation(String beanName) {
        if (!this.singletonsCurrentlyInCreation.add(beanName)) {
            throw new IllegalStateException("El bean '" + beanName + "' ya está marcado como en proceso de creación. " +
                    "Posible ciclo de dependencias irresoluble.");
        }
    }

    /**
     * Elimina la marca de "actualmente en creación" de un bean.
     *
     * @param beanName El nombre del bean.
     */
    protected void afterSingletonCreation(String beanName) {
        if (!this.singletonsCurrentlyInCreation.remove(beanName)) {
            throw new IllegalStateException("El bean '" + beanName + "' no estaba marcado como en creación.");
        }
    }

    /**
     * Verifica si un bean está actualmente en proceso de creación.
     *
     * @param beanName El nombre del bean.
     * @return true si el bean se está creando actualmente.
     */
    public boolean isSingletonCurrentlyInCreation(String beanName) {
        return this.singletonsCurrentlyInCreation.contains(beanName);
    }
}
