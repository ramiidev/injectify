package com.ramiidev.injectify.core.convert.support;

import com.ramiidev.injectify.core.convert.converters.StringToBooleanConverter;

public class DefaultConversionService extends ConversionService {

    public DefaultConversionService() {
        registerDefaultConverters();
    }

    private void registerDefaultConverters() {
        StringToBooleanConverter stringToBooleanConverter = new StringToBooleanConverter();
        addConverter(Boolean.class, stringToBooleanConverter);
        addConverter(boolean.class, stringToBooleanConverter);
        addConverter(Integer.class, Integer::valueOf);
        addConverter(int.class, Integer::valueOf);
        addConverter(Long.class, Long::valueOf);
        addConverter(long.class, Long::valueOf);
        addConverter(Double.class, Double::valueOf);
        addConverter(double.class, Double::valueOf);
    }
}
