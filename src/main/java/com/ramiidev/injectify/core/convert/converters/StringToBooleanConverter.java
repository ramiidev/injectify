package com.ramiidev.injectify.core.convert.converters;

import com.ramiidev.injectify.core.convert.Converter;

import javax.annotation.Nullable;
import java.util.Set;

public class StringToBooleanConverter implements Converter<String, Boolean> {

    private static final Set<String> TRUE_VALUES = Set.of("true", "on", "yes", "1", "enabled");

    @Override
    @Nullable
    public Boolean convert(String source) {
        if (source == null || source.isBlank()) return null;
        String value = source.trim().toLowerCase();
        return TRUE_VALUES.contains(value);
    }
}
