package com.ramiidev.injectify.configuration.binder.support;

import com.ramiidev.injectify.configuration.annotation.ConfigurationProperties;
import com.ramiidev.injectify.configuration.annotation.NestedConfigurationProperty;
import com.ramiidev.injectify.configuration.annotation.PropertyValue;
import com.ramiidev.injectify.configuration.binder.BinderContext;
import com.ramiidev.injectify.configuration.env.Environment;
import com.ramiidev.injectify.configuration.exception.BinderException;
import com.ramiidev.injectify.core.convert.support.ConversionService;
import com.ramiidev.injectify.shared.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class JavaBeanBinder {
    private final ConversionService conversionService;

    public JavaBeanBinder(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public <T> T bind(Class<T> type, Environment env) {
        if (!type.isAnnotationPresent(ConfigurationProperties.class)) {
            throw new BinderException("Falta @ConfigurationProperties en " + type.getName());
        }

        ConfigurationProperties cp = type.getAnnotation(ConfigurationProperties.class);
        String prefix = buildPrefix(cp);

        T instance = ReflectionUtils.instantiate(type);
        BinderContext ctx = new BinderContext(env, prefix);

        try {
            bindFields(instance, ctx);
            return instance;
        } catch (Exception e) {
            throw new BinderException("Error binding " + type.getName(), e);
        }
    }

    private String buildPrefix(ConfigurationProperties cp) {
        String p = cp.prefix().trim();
        String v = cp.value().trim();
        if (!p.isEmpty() && !v.isEmpty()) return p + "." + v;
        return !p.isEmpty() ? p : v;
    }

    private void bindFields(Object bean, BinderContext ctx) throws IllegalAccessException {
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        List<Field> catchAllFields = new ArrayList<>();

        // --- PASADA 1: Campos Específicos y Nested ---
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);

            PropertyValue pv = field.getAnnotation(PropertyValue.class);

            // DETECCIÓN ROBUSTA DE CATCH-ALL
            // Verificamos si key es ".*" o "*"
            if (pv != null && (".*".equals(pv.key()) || "*".equals(pv.key()))) {
                catchAllFields.add(field);
                continue; // Lo procesamos en la Pasada 2
            }

            // 1. Nested
            if (field.isAnnotationPresent(NestedConfigurationProperty.class)) {
                bindNested(bean, field, ctx);
                continue;
            }

            // 2. Simple Properties
            String keyName = getKeyName(field);
            String[] candidates = getCandidates(keyName);

            for (String candidate : candidates) {
                String fullKey = ctx.prefix.isEmpty() ? candidate : ctx.prefix + "." + candidate;
                String val = ctx.env.getProperty(fullKey);

                if (val != null) {
                    Object converted = convertValue(field, val);
                    ReflectionUtils.setField(bean, field, converted);
                    ctx.claimedKeys.add(fullKey);
                    break;
                }
            }
        }

        // --- PASADA 2: Catch-All ---
        for (Field field : catchAllFields) {
            bindCatchAllProperties(bean, field, ctx);
        }
    }

    private void bindNested(Object parent, Field field, BinderContext parentCtx) throws IllegalAccessException {
        Class<?> childType = field.getType();
        String childSegment;
        ConfigurationProperties cp = childType.getAnnotation(ConfigurationProperties.class);

        if (cp != null && !cp.value().isEmpty()) {
            childSegment = cp.value();
        } else {
            childSegment = field.getName().replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
        }

        String childPrefix = parentCtx.prefix.isEmpty() ? childSegment : parentCtx.prefix + "." + childSegment;

        // Bloqueamos el namespace del hijo
        parentCtx.nestedNamespaces.add(childPrefix + ".");

        Object child = ReflectionUtils.instantiate(childType);
        BinderContext childCtx = new BinderContext(parentCtx.env, childPrefix);

        bindFields(child, childCtx);
        ReflectionUtils.setField(parent, field, child);
    }

    private void bindCatchAllProperties(Object bean, Field field, BinderContext ctx) {
        if (field.getType() != Properties.class && !Map.class.isAssignableFrom(field.getType())) {
            return;
        }

        // 1. INSTANCIAMOS SIEMPRE (Esto arregla tu NullPointerException)
        Properties props = new Properties();

        Properties allProps = ctx.env.getAllProperties();
        String searchPrefix = ctx.prefix + ".";

        for (String key : allProps.stringPropertyNames()) {
            // A. Debe empezar con el prefijo
            if (!key.startsWith(searchPrefix)) continue;

            // B. No debe estar reclamada
            if (ctx.claimedKeys.contains(key)) continue;

            // C. No debe ser de un hijo
            boolean isNested = false;
            for (String ns : ctx.nestedNamespaces) {
                if (key.startsWith(ns)) {
                    isNested = true;
                    break;
                }
            }
            if (isNested) continue;

            // Es válida
            String relativeKey = key.substring(searchPrefix.length());
            props.setProperty(relativeKey, allProps.getProperty(key));
        }

        // 2. INYECTAMOS SIEMPRE (Aunque esté vacío)
        ReflectionUtils.setField(bean, field, props);
    }

    private Object convertValue(Field field, String val) {
        if (Collection.class.isAssignableFrom(field.getType())) {
            Class<?> elemType = ReflectionUtils.getGenericType(field.getGenericType());
            return convertToCollection(val, elemType);
        }

        return conversionService.convert(val, field.getType());
    }

    private <T> List<T> convertToCollection(String rawValue, Class<T> elementType) {
        if (rawValue == null || rawValue.isBlank()) {
            return Collections.emptyList();
        }

        String[] parts = rawValue.split(",");
        List<T> list = new ArrayList<>(parts.length);

        for (String part : parts) {
            T converted = conversionService.convert(part.trim(), elementType);
            list.add(converted);
        }

        return list;
    }

    private String getKeyName(Field f) {
        PropertyValue pv = f.getAnnotation(PropertyValue.class);
        // Si no tiene key definida, usamos el nombre del campo
        return (pv != null && !pv.key().isEmpty() && !".*".equals(pv.key())) ? pv.key() : f.getName();
    }

    private String[] getCandidates(String key) {
        String kebab = key.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
        if (kebab.equals(key)) return new String[]{key};
        return new String[]{key, kebab};
    }
}