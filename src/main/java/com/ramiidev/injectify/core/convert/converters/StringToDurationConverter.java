package com.ramiidev.injectify.core.convert.converters;

import com.ramiidev.injectify.core.convert.Converter;

import java.time.Duration;
import java.util.Locale;

// Implementa tu interfaz Converter
public class StringToDurationConverter implements Converter<String, Duration> {

    @Override
    public Duration convert(String source) {
        if (source == null || source.isBlank()) return null;

        String v = source.trim().toLowerCase(Locale.ROOT);
        try {
            if (v.endsWith("ms")) return Duration.ofMillis(parseLong(v, "ms"));
            if (v.endsWith("s")) return Duration.ofSeconds(parseLong(v, "s"));
            if (v.endsWith("m")) return Duration.ofMinutes(parseLong(v, "m"));
            if (v.endsWith("h")) return Duration.ofHours(parseLong(v, "h"));
            if (v.endsWith("d")) return Duration.ofDays(parseLong(v, "d"));

            return Duration.parse(source);
        } catch (Exception ex) {
            throw new IllegalArgumentException("No se puede convertir '" + source + "' a Duration", ex);
        }
    }

    private long parseLong(String value, String unit) {
        return Long.parseLong(value.replace(unit, ""));
    }
}
