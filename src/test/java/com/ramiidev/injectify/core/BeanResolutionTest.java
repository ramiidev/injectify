package com.ramiidev.injectify.core;

import com.ramiidev.injectify.core.context.ApplicationContext;
import com.ramiidev.injectify.core.support.TestContextFactory;
import com.ramiidev.injectify.fixtures.scope.Singleton;
import com.ramiidev.injectify.fixtures.scope.SingletonImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BeanResolutionTest {

    @Test
    @DisplayName("Resolve bean using concrete type")
    void resolvesBeanUsingConcreteType() {
        ApplicationContext context = TestContextFactory.create(SingletonImpl.class);

        try {
            SingletonImpl beanConcreteType = context.getBean(SingletonImpl.class);
            assertNotNull(beanConcreteType);
        } finally {
            context.destroy();
        }
    }

    @Test
    @DisplayName("Resolve bean using interface")
    void resolverBeanUsingInterface() {
        ApplicationContext context = TestContextFactory.create(SingletonImpl.class);

        try {
            Singleton beanInterface = context.getBean(Singleton.class);
            assertNotNull(beanInterface);
        } finally {
            context.destroy();
        }
    }

}
