package com.ramiidev.injectify.core.support;

import com.ramiidev.injectify.configuration.env.Environment;
import com.ramiidev.injectify.configuration.env.support.DefaultEnvironment;
import com.ramiidev.injectify.configuration.env.support.MapPropertySource;
import com.ramiidev.injectify.core.context.ApplicationContext;
import com.ramiidev.injectify.core.context.support.AnnotationConfigAbstractApplicationContext;
import com.ramiidev.injectify.core.convert.support.ApplicationConversionService;

import java.util.Map;

public class TestContextFactory {
    private TestContextFactory() {
    }

    public static ApplicationContext create(Class<?>... componentsClasses) {
        return create(Map.of(), componentsClasses);
    }

    public static ApplicationContext create(Map<String, String> properties,
                                            Class<?>... componentsClasses) {
        ApplicationConversionService conversionService =
                ApplicationConversionService.getSharedInstance();

        Environment environment = new DefaultEnvironment(
                new MapPropertySource(properties),
                conversionService
        );

        ApplicationContext context =
                new AnnotationConfigAbstractApplicationContext(
                        environment,
                        conversionService,
                        componentsClasses
                );

        context.refresh();

        return context;
    }
}
