package com.ramiidev.injectify.core.scanner;

import com.ramiidev.injectify.shared.annotation.stereotype.Component;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.net.URL;
import java.util.Set;

public class ClassPathScanner {

    public Set<Class<?>> scan(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        return reflections.getTypesAnnotatedWith(Component.class);
    }

    public Set<Class<?>> scan(URL projectClasspathRoot) {
        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(projectClasspathRoot));
        return reflections.getTypesAnnotatedWith(Component.class);
    }
}
