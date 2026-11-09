package com.ramiidev.injectify.shared.utils;

import java.io.File;
import java.net.URL;

/**
 * Clase de utilidad para operaciones relacionadas con rutas del classpath.
 */
public final class PathUtils {

    /**
     * Constructor privado para evitar la instanciación de la clase de utilidad.
     */
    private PathUtils() {}

    /**
     * Calcula la URL raíz de las clases de un proyecto a partir de una clase fuente.
     * @param source La clase a partir de la cual se determinará la raíz del proyecto.
     * @return La URL del directorio raíz.
     * @throws Exception Si no se puede encontrar el recurso o la ruta.
     */
    public static URL calculateProjectRootUrl(Class<?> source) throws Exception {
        String className = source.getName();
        String classAsPath = className.replace('.', '/') + ".class";
        URL classUrl = source.getClassLoader().getResource(classAsPath);
        if (classUrl == null) {
            throw new RuntimeException("No se pudo encontrar el recurso de la clase principal. Asegúrate de que el proyecto esté compilado.");
        }
        String classPath = classUrl.getPath();
        String basePath = classPath.substring(0, classPath.length() - classAsPath.length());
        return new File(basePath).toURI().toURL();
    }
}