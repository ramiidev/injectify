package com.ramiidev.injectify.configuration.env;

import java.util.Properties;

/**
 * Define un contrato para cualquier componente que actúe como una
 * fuente de propiedades de configuración para la aplicación.
 */
public interface PropertySource {

    /**
     * Devuelve una propiedad específica.
     * @param key La clave de la propiedad.
     * @param defaultValue El valor a devolver si la clave no se encuentra.
     * @return El valor de la propiedad.
     */
    String getProperty(String key, String defaultValue);

    /**
     * Devuelve todas las propiedades cargadas.
     * @return Un objeto {@link Properties} con toda la configuración.
     */
    Properties getAllProperties();
}
