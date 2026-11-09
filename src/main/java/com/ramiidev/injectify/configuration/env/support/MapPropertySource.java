package com.ramiidev.injectify.configuration.env.support;

import com.ramiidev.injectify.configuration.env.PropertySource;

import java.util.Map;
import java.util.Properties;

/**
 * Implementación basada en un Map.
 */
public class MapPropertySource implements PropertySource {
    private final Map<String, String> source;

    public MapPropertySource(Map<String, String> source) {
        this.source = source;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return source.getOrDefault(key, defaultValue);
    }

    @Override
    public Properties getAllProperties() {
        Properties p = new Properties();
        source.forEach(p::setProperty);
        return p;
    }
}