package com.ramiidev.injectify.configuration.env.support;

import com.ramiidev.injectify.configuration.env.MutableEnvironment;
import com.ramiidev.injectify.configuration.env.PropertySource;
import com.ramiidev.injectify.core.convert.support.ConversionService;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultEnvironment implements MutableEnvironment {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)\\}");
    private final CompositePropertySource composite = new CompositePropertySource();
    private final ConversionService conversionService;

    public DefaultEnvironment(PropertySource primarySource, ConversionService conversionService) {
        this.composite.addFirst(primarySource);
        this.conversionService = conversionService;
    }

    @Override
    public void addFirstPropertySource(PropertySource propertySource) {
        this.composite.addFirst(propertySource);
    }

    @Override
    public String getProperty(String key) {
        return getProperty(key, (String) null);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String raw = composite.getProperty(key, null);
        return resolvePlaceholders( raw != null ? raw : defaultValue);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        // 1. Obtenemos el valor String ya resuelto (con placeholders procesados)
        String value = getProperty(key);

        // 2. Si es nulo, dejamos que el servicio decida o retornamos null
        if (value == null) return null;

        // 3. Delegamos al ConversionService
        return conversionService.convert(value, targetType);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        // 1. Intentamos obtener el valor
        String value = getProperty(key);

        // 2. Si existe, convertimos
        if (value != null) {
            return conversionService.convert(value, targetType);
        }

        // 3. Si no existe, retornamos el default
        return defaultValue;
    }

    @Override
    public Properties getAllProperties() {
        return composite.getAllProperties();
    }

    @Override
    public String resolvePlaceholders(String text) {
        if (text == null || !text.contains("${")) return text;
        return resolveRecursively(text, new HashSet<>());
    }

    @Override
    public boolean containsProperty(String key) {
        return composite.getProperty(key, null) != null;
    }

    private String resolveRecursively(String text, Set<String> visited) {
        if (text == null) return null;

        Matcher m = PLACEHOLDER.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (m.find()) {
            String content = m.group(1); // Lo que está dentro de ${...} ej: "app.name:Unknown"
            String key = content;
            String defaultValue = null;

            // --- 1. Separar Clave y Valor por Defecto ---
            int separatorIndex = content.indexOf(':');
            if (separatorIndex != -1) {
                key = content.substring(0, separatorIndex);
                defaultValue = content.substring(separatorIndex + 1);
            }

            // --- 2. Detección de Ciclos ---
            if (!visited.add(key)) {
                throw new IllegalArgumentException("Circular placeholder detected: " + key);
            }

            // --- 3. Buscar Valor ---
            String rawValue = composite.getProperty(key, null);

            // Si no existe, usamos el default (si hay)
            if (rawValue == null && defaultValue != null) {
                rawValue = defaultValue;
            }

            // --- 4. Recursión y Reemplazo ---
            String replacement;
            if (rawValue != null) {
                // Resolvemos recursivamente por si el valor o el default tienen más placeholders
                replacement = resolveRecursively(rawValue, visited);
            } else {
                // Si no hay valor ni default, dejamos el placeholder original tal cual
                replacement = m.group(0);
            }

            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            visited.remove(key); // Limpiamos para otras ramas
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
