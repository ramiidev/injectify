package com.ramiidev.injectify.core;

import com.ramiidev.injectify.core.context.ApplicationContext;
import com.ramiidev.injectify.core.support.TestContextFactory;
import com.ramiidev.injectify.fixtures.scope.SingletonService;
import com.ramiidev.injectify.fixtures.scope.SingletonServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BeanResolutionTest {

    @Test
    @DisplayName("Resolve bean using concrete type")
    void resolvesBeanUsingConcreteType() {
        ApplicationContext context = TestContextFactory.create(SingletonServiceImpl.class);

        try {
            SingletonServiceImpl beanConcreteType = context.getBean(SingletonServiceImpl.class);
            assertNotNull(beanConcreteType);
        } finally {
            context.destroy();
        }
    }

    @Test
    @DisplayName("Resolve bean using interface")
    void resolverBeanUsingInterface() {
        ApplicationContext context = TestContextFactory.create(SingletonServiceImpl.class);

        try {
            SingletonService beanInterface = context.getBean(SingletonService.class);
            assertNotNull(beanInterface);
        } finally {
            context.destroy();
        }
    }

}
