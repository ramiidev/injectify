package com.ramiidev.injectify.core;

import com.ramiidev.injectify.core.context.ApplicationContext;
import com.ramiidev.injectify.core.support.TestContextFactory;
import com.ramiidev.injectify.fixtures.configuration.DefaultProduct;
import com.ramiidev.injectify.fixtures.configuration.DependentBean;
import com.ramiidev.injectify.fixtures.configuration.DependentBeanConfiguration;
import com.ramiidev.injectify.fixtures.configuration.BeanConfiguration;
import com.ramiidev.injectify.fixtures.configuration.SecondaryProcessor;
import com.ramiidev.injectify.fixtures.qualifier.MetaQualifiedConsumer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class AdvancedBeanContractTest {

    @Test
    void resolvesMetaQualifier() {
        ApplicationContext context =
                TestContextFactory.create(BeanConfiguration.class, MetaQualifiedConsumer.class);

        try {
            MetaQualifiedConsumer consumer = context.getBean(MetaQualifiedConsumer.class);
            assertInstanceOf(SecondaryProcessor.class, consumer.getProcessor());
        } finally {
            context.destroy();
        }
    }

    @Test
    void resolvesDependencyBetweenBeanMethods() {
        ApplicationContext context = TestContextFactory.create(DependentBeanConfiguration.class);

        try {
            DependentBean dependent = context.getBean(DependentBean.class);
            assertInstanceOf(DefaultProduct.class, dependent.getProduct());
            assertSame(dependent.getProduct(), context.getBean("product"));
        } finally {
            context.destroy();
        }
    }

    @Test
    void exposesEarlyReferencesForSetterCycleAndProxiesConfiguration() {
        ApplicationContext context = TestContextFactory.create(
                com.ramiidev.injectify.fixtures.cycle.CycleFirst.class,
                com.ramiidev.injectify.fixtures.cycle.CycleSecond.class,
                com.ramiidev.injectify.fixtures.cycle.ProxyConfiguration.class
        );

        try {
            com.ramiidev.injectify.fixtures.cycle.CycleFirst first =
                    context.getBean(com.ramiidev.injectify.fixtures.cycle.CycleFirst.class);
            com.ramiidev.injectify.fixtures.cycle.CycleSecond second =
                    context.getBean(com.ramiidev.injectify.fixtures.cycle.CycleSecond.class);
            Object configuration =
                    context.getBean(com.ramiidev.injectify.fixtures.cycle.ProxyConfiguration.class);

            assertSame(second, first.getSecond());
            assertSame(first, second.getFirst());
            assertNotSame(com.ramiidev.injectify.fixtures.cycle.ProxyConfiguration.class,
                    configuration.getClass());
        } finally {
            context.destroy();
        }
    }
}
