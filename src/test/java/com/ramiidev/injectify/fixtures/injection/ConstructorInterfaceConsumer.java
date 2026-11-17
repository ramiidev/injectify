package com.ramiidev.injectify.fixtures.injection;

import com.ramiidev.injectify.fixtures.scope.Singleton;
import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
public class ConstructorInterfaceConsumer {

    private final Singleton singleton;

    @Autowired
    public ConstructorInterfaceConsumer(final Singleton singleton) {
        this.singleton = singleton;
    }

    public Singleton getSingletonService() {
        return singleton;
    }

}
