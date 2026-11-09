package com.ramiidev.injectify.core.convert.converters;

import com.ramiidev.injectify.core.convert.support.ConversionService;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class CollectionConverter {
    private CollectionConverter() {}
    public static <T> List<T> convertCsv(String raw, Class<T> type, ConversionService cs) {
        String[] parts = raw.split(",");
        List<T> list = new ArrayList<>();
        for (String p : parts) list.add(cs.convert(p.trim(), type));
        return list;
    }
    public static Class<?> getElementType(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        }
        return String.class;
    }
}
