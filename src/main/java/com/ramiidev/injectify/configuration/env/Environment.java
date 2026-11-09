package com.ramiidev.injectify.configuration.env;

import java.util.Properties;

/**
 * Representa el entorno de configuración de la aplicación.
 * Proporciona un acceso unificado a las propiedades desde
 * diversas fuentes (archivos, variables de sistema, etc.).
 */
public interface Environment{

    /**
     * Obtiene una propiedad por su clave.
     * @param key La clave de la propiedad.
     * @return El valor de la propiedad o null si no existe.
     */
    String getProperty(String key);

    /**
     * Obtiene una propiedad por su clave, con un valor por defecto.
     * @param key La clave de la propiedad.
     * @param defaultValue El valor a devolver si la clave no se encuentra.
     * @return El valor de la propiedad o el valor por defecto.
     */
    String getProperty(String key, String defaultValue);


    <T> T getProperty(String key, Class<T> targetType);
    <T> T getProperty(String key, Class<T> targetType, T defaultValue);

    /**
     * Devuelve todas las propiedades de una fuente principal.
     * @return Un objeto Properties.
     */
    Properties getAllProperties();

    /**
     * Resuelve los placeholders del formato ${...} en el texto dado,
     * reemplazándolos con los valores de las propiedades correspondientes.
     * @param text El texto que contiene los placeholders.
     * @return El texto con los valores resueltos.
     */
    String resolvePlaceholders(String text);

    boolean containsProperty(String key);
}
