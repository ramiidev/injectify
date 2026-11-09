package com.ramiidev.injectify.core;

import com.ramiidev.injectify.boot.autoconfigure.InjectifyImportsLoader;
import com.ramiidev.injectify.boot.event.BootstrapEventListener;
import com.ramiidev.injectify.configuration.binder.support.ConfigurationPropertyBinder;
import com.ramiidev.injectify.configuration.env.MutableEnvironment;
import com.ramiidev.injectify.configuration.env.support.DefaultEnvironment;
import com.ramiidev.injectify.configuration.env.support.MapPropertySource;
import com.ramiidev.injectify.core.beans.definition.BeanDefinition;
import com.ramiidev.injectify.core.beans.definition.BeanScope;
import com.ramiidev.injectify.core.beans.support.DefaultBeanFactory;
import com.ramiidev.injectify.core.convert.support.ApplicationConversionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContainerContractsTest {

    @AfterEach
    void clearFactoryCache() {
        InjectifyImportsLoader.clearCache();
    }

    @Test
    void cliPropertySourceHasPrecedenceWhenAddedFirst() {
        MutableEnvironment environment = new DefaultEnvironment(
                new MapPropertySource(Map.of("app.mode", "file")),
                ApplicationConversionService.getSharedInstance()
        );

        environment.addFirstPropertySource(new MapPropertySource(Map.of("app.mode", "cli")));

        assertEquals("cli", environment.getProperty("app.mode"));
    }

    @Test
    void prototypeBeanCreatesANewInstanceForEveryRequest() {
        DefaultBeanFactory factory = newFactory();
        BeanDefinition definition = new BeanDefinition("prototypeBean", PrototypeBean.class);
        definition.setScope(BeanScope.PROTOTYPE);
        factory.registerBeanDefinition("prototypeBean", definition);

        Object first = factory.getBean("prototypeBean");
        Object second = factory.getBean("prototypeBean");

        assertNotSame(first, second);
    }

    @Test
    void factoriesAreLoadedFromCanonicalMetaInfResource() {
        List<BootstrapEventListener> listeners =
                InjectifyImportsLoader.loadFactories(BootstrapEventListener.class);

        assertTrue(listeners.stream().anyMatch(listener ->
                listener.getClass().getName().equals(
                        "com.ramiidev.injectify.boot.listener.LoggingBootstrapListener")));
    }

    private DefaultBeanFactory newFactory() {
        return new DefaultBeanFactory(
                new ConfigurationPropertyBinder(ApplicationConversionService.getSharedInstance()),
                ApplicationConversionService.getSharedInstance()
        );
    }

    public static final class PrototypeBean {
    }
}
