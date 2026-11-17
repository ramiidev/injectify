package com.ramiidev.injectify.core;

import com.ramiidev.injectify.core.context.ApplicationContext;
import com.ramiidev.injectify.core.support.TestContextFactory;
import com.ramiidev.injectify.fixtures.injection.ConstructorInterfaceConsumer;
import com.ramiidev.injectify.fixtures.injection.SetterConcreteConsumer;
import com.ramiidev.injectify.fixtures.injection.SetterInterfaceConsumer;
import com.ramiidev.injectify.fixtures.injection.ConstructorConcreteConsumer;
import com.ramiidev.injectify.fixtures.scope.SingletonImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BeanInjectionTest {

    @Test
    @DisplayName("Inject dependency through constructor using concrete type")
    void injectDependencyThroughConstructorUsingConcreteType() {
        ApplicationContext context =
                TestContextFactory.create(
                        SingletonImpl.class,
                        ConstructorConcreteConsumer.class
                );
        try {
            ConstructorConcreteConsumer consumer =
                    context.getBean(ConstructorConcreteConsumer.class);

            assertNotNull(consumer.getSingletonService());
        } finally {
            context.destroy();
        }
    }

    @Test
    @DisplayName("Inject dependency through constructor using interface")
    void injectDependencyThroughConstructorUsingInterface() {
        ApplicationContext context =
                TestContextFactory.create(
                        SingletonImpl.class,
                        ConstructorInterfaceConsumer.class
                );
        try {
            ConstructorInterfaceConsumer consumer =
                    context.getBean(ConstructorInterfaceConsumer.class);

            assertNotNull(consumer.getSingletonService());
        } finally {
            context.destroy();
        }
    }

    @Test
    @DisplayName("Inject dependency through setter using concrete type")
    void injectDependencyThroughSetterUsingConcreteType() {
        ApplicationContext context =
                TestContextFactory.create(
                        SingletonImpl.class,
                        SetterConcreteConsumer.class
                );

        try {
            SetterConcreteConsumer consumer =
                    context.getBean(SetterConcreteConsumer.class);

            assertNotNull(consumer.getSingletonService());
        } finally {
            context.destroy();
        }

    }

    @Test
    @DisplayName("Inject dependency through setter using interface")
    void injectDependencyThroughSetterUsingInterface() {
        ApplicationContext context =
                TestContextFactory.create(
                        SingletonImpl.class,
                        SetterInterfaceConsumer.class
                );

        try {
            SetterInterfaceConsumer consumer =
                    context.getBean(SetterInterfaceConsumer.class);

            assertNotNull(consumer.getSingletonService());
        } finally {
            context.destroy();
        }

    }

}
