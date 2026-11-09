package com.ramiidev.injectify.configuration.env.support;

import com.ramiidev.injectify.configuration.env.PropertySource;
import com.ramiidev.injectify.configuration.exception.ConfigurationNotFoundException;
import com.ramiidev.injectify.infrastructure.logging.support.DeferredLogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Carga las propiedades desde application.properties y, opcionalmente,
 * desde application-{profile}.properties, permitiendo sobreescritura por entorno.
 */
public class ApplicationPropertySource implements PropertySource {

    private static final String BASE_FILE = "application.properties";
    private static final String PROFILE_KEY = "injectify.application.profiles.active";

    private final Properties properties = new Properties();

    public ApplicationPropertySource() throws ConfigurationNotFoundException {
        loadProperties(BASE_FILE, true);

        String activeProfile = System.getProperty(PROFILE_KEY);
        if (activeProfile == null) activeProfile = System.getenv(PROFILE_KEY);
        if (activeProfile == null) activeProfile = properties.getProperty(PROFILE_KEY);
        if (activeProfile != null && !activeProfile.isBlank()) {
            String message = String.format("Perfil activo:'%s'", activeProfile);
            DeferredLogFactory.getLog(ApplicationPropertySource.class).info(message);
            loadProperties("application-" + activeProfile + ".properties", false);
        }
    }

    private void loadProperties(String filename, boolean required) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (input == null) {
                if (required)
                    throw new ConfigurationNotFoundException("No se encontró " + filename);
                return;
            }
            Properties temp = new Properties();
            temp.load(input);
            properties.putAll(temp);
        } catch (IOException ex) {
            throw new ConfigurationNotFoundException("Error leyendo " + filename, ex);
        }
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    public Properties getAllProperties() {
        return properties;
    }
}