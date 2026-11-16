package com.ramiidev.injectify.core;

import com.ramiidev.injectify.core.context.ApplicationContext;
import com.ramiidev.injectify.core.exception.BeanCreationException;
import com.ramiidev.injectify.core.exception.NoUniqueBeanDefinitionException;
import com.ramiidev.injectify.core.exception.UnsatisfiedDependencyException;
import com.ramiidev.injectify.core.support.TestContextFactory;
import com.ramiidev.injectify.fixtures.configuration.BeanConfiguration;
import com.ramiidev.injectify.fixtures.configuration.DefaultProduct;
import com.ramiidev.injectify.fixtures.configuration.NamedProduct;
import com.ramiidev.injectify.fixtures.configuration.Product;
import com.ramiidev.injectify.fixtures.configuration.Processor;
import com.ramiidev.injectify.fixtures.configuration.SecondaryProcessor;
import com.ramiidev.injectify.fixtures.lifecycle.DisposableComponent;
import com.ramiidev.injectify.fixtures.missing.MissingDependencyConsumer;
import com.ramiidev.injectify.fixtures.qualifier.QualifiedConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeanContractTest {

    @AfterEach
    void resetDestroyCounter() {
        DisposableComponent.resetDestroyCount();
    }

    @Test
    @DisplayName("Registers @Bean methods using default and explicit names")
    void registersBeanMethodsUsingDefaultAndExplicitNames() {
        ApplicationContext context = TestContextFactory.create(BeanConfiguration.class);

        try {
            Product defaultProduct = context.getBean("defaultProduct", Product.class);
            Product namedProduct = context.getBean("namedProduct", Product.class);

            assertInstanceOf(DefaultProduct.class, defaultProduct);
            assertInstanceOf(NamedProduct.class, namedProduct);
            assertEquals(3, context.getBeanNamesForType(Product.class).length);
        } finally {
            context.destroy();
        }
    }

    @Test
    @DisplayName("Applies prototype scope to @Bean methods")
    void appliesPrototypeScopeToBeanMethods() {
        ApplicationContext context = TestContextFactory.create(BeanConfiguration.class);

        try {
            Product first = context.getBean("prototypeProduct", Product.class);
            Product second = context.getBean("prototypeProduct", Product.class);

            assertNotSame(first, second);
        } finally {
            context.destroy();
        }
    }

    @Test
    @DisplayName("Resolves a dependency using an explicit qualifier")
    void resolvesDependencyUsingExplicitQualifier() {
        ApplicationContext context =
                TestContextFactory.create(BeanConfiguration.class, QualifiedConsumer.class);

        try {
            QualifiedConsumer consumer = context.getBean(QualifiedConsumer.class);

            assertInstanceOf(SecondaryProcessor.class, consumer.getProcessor());
        } finally {
            context.destroy();
        }
    }

    @Test
    @DisplayName("Rejects an unqualified dependency with multiple candidates")
    void rejectsAmbiguousDependency() {
        ApplicationContext context = TestContextFactory.create(BeanConfiguration.class);

        try {
            assertThrows(
                    NoUniqueBeanDefinitionException.class,
                    () -> context.getBean(Processor.class)
            );
        } finally {
            context.destroy();
        }
    }

    @Test
    @DisplayName("Reports a missing required dependency")
    void reportsMissingRequiredDependency() {
        ApplicationContext context = TestContextFactory.create(MissingDependencyConsumer.class);

        try {
            BeanCreationException exception = assertThrows(
                    BeanCreationException.class,
                    () -> context.getBean(MissingDependencyConsumer.class)
            );

            assertInstanceOf(UnsatisfiedDependencyException.class, exception.getCause());
        } finally {
            context.destroy();
        }
    }

    @Test
    @DisplayName("Destroys instantiated disposable singletons")
    void destroysInstantiatedDisposableSingletons() {
        ApplicationContext context = TestContextFactory.create(DisposableComponent.class);
        DisposableComponent bean = context.getBean(DisposableComponent.class);

        try {
            assertSame(bean, context.getBean(DisposableComponent.class));
        } finally {
            context.destroy();
        }

        assertEquals(1, DisposableComponent.getDestroyCount());
    }
}
