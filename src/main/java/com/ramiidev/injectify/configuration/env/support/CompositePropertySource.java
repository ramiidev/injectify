package com.ramiidev.injectify.configuration.env.support;

import com.ramiidev.injectify.configuration.env.PropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CompositePropertySource implements PropertySource {
    private final List<PropertySource> sources = new ArrayList<>();

    public void addFirst(PropertySource ps) {
        sources.addFirst(ps);
    }

    public void addLast(PropertySource ps) {
        sources.addLast(ps);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        for (PropertySource ps : sources) {
            String val = ps.getProperty(key, null);
            if (val != null) return val;
        }
        return defaultValue;
    }

    @Override
    public Properties getAllProperties() {
        Properties merged = new Properties();
        // Iterar en reverso para mantener precedencia correcta en el putAll
        for (int i = sources.size() - 1; i >= 0; i--) {
            merged.putAll(sources.get(i).getAllProperties());
        }
        return merged;
    }
}
