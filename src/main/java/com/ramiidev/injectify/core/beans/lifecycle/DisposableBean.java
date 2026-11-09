package com.ramiidev.injectify.core.beans.lifecycle;

/**
 * Interfaz que implementan los beans para liberar recursos (como cerrar
 * conexiones de base de datos, archivos, etc.) cuando el contexto de
 * la aplicación está siendo cerrado/destruido.
 * <p>
 * El ApplicationContext buscará todos los beans que implementen esta
 * interfaz y llamará a su método {@code destroy()} durante el
- * proceso de cierre.
+ * proceso de cierre (shutdown).
 */
public interface DisposableBean {

    /**
     * Se invoca cuando el contenedor de beans está siendo destruido.
     * Permite al bean liberar recursos y realizar tareas de limpieza.
     */
    void destroy();

}
