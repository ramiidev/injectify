package com.ramiidev.injectify.fixtures.lifecycle;

import com.ramiidev.injectify.core.beans.lifecycle.DisposableBean;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DisposableComponent implements DisposableBean {
    private static final AtomicInteger DESTROY_COUNT = new AtomicInteger();

    public static void resetDestroyCount() {
        DESTROY_COUNT.set(0);
    }

    public static int getDestroyCount() {
        return DESTROY_COUNT.get();
    }

    @Override
    public void destroy() {
        DESTROY_COUNT.incrementAndGet();
    }
}
