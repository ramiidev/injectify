package com.ramiidev.injectify.core;

import com.ramiidev.injectify.core.context.ApplicationContext;
import com.ramiidev.injectify.core.support.TestContextFactory;
import com.ramiidev.injectify.fixtures.scope.PrototypeService;
import com.ramiidev.injectify.fixtures.scope.PrototypeServiceImpl;
import com.ramiidev.injectify.fixtures.scope.SingletonService;
import com.ramiidev.injectify.fixtures.scope.SingletonServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class BeanScopeTest {

    @Test
    @DisplayName("Return same instance for singleton scope")
    void returnSameInstanceForSingleton() {
        ApplicationContext context = TestContextFactory.create(SingletonServiceImpl.class);

        try {
            SingletonService firstInstance = context.getBean(SingletonServiceImpl.class);
            SingletonService secondInstance = context.getBean(SingletonServiceImpl.class);

            assertSame(firstInstance, secondInstance);
        } finally {
            context.destroy();
        }
    }

    @Test
    @DisplayName("Return different instances for prototype scope")
    void returnDifferentInstanceForPrototype() {
        ApplicationContext context = TestContextFactory.create(PrototypeServiceImpl.class);

        try {
            PrototypeService firstInstance = context.getBean(PrototypeServiceImpl.class);
            PrototypeService secondInstance = context.getBean(PrototypeServiceImpl.class);

            assertNotEquals(firstInstance, secondInstance);
        } finally {
            context.destroy();
        }

    }
}
