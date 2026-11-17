package com.ramiidev.injectify.core;

import com.ramiidev.injectify.core.context.ApplicationContext;
import com.ramiidev.injectify.core.support.TestContextFactory;
import com.ramiidev.injectify.fixtures.scope.Prototype;
import com.ramiidev.injectify.fixtures.scope.PrototypeImpl;
import com.ramiidev.injectify.fixtures.scope.Singleton;
import com.ramiidev.injectify.fixtures.scope.SingletonImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class BeanScopeTest {

    @Test
    @DisplayName("Return same instance for singleton scope")
    void returnSameInstanceForSingleton() {
        ApplicationContext context = TestContextFactory.create(SingletonImpl.class);

        try {
            Singleton firstInstance = context.getBean(SingletonImpl.class);
            Singleton secondInstance = context.getBean(SingletonImpl.class);

            assertSame(firstInstance, secondInstance);
        } finally {
            context.destroy();
        }
    }

    @Test
    @DisplayName("Return different instances for prototype scope")
    void returnDifferentInstanceForPrototype() {
        ApplicationContext context = TestContextFactory.create(PrototypeImpl.class);

        try {
            Prototype firstInstance = context.getBean(PrototypeImpl.class);
            Prototype secondInstance = context.getBean(PrototypeImpl.class);

            assertNotEquals(firstInstance, secondInstance);
        } finally {
            context.destroy();
        }

    }
}
