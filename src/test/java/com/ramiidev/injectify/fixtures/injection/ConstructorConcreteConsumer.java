package com.ramiidev.injectify.fixtures.injection;

import com.ramiidev.injectify.fixtures.scope.SingletonImpl;
import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
public class ConstructorConcreteConsumer {

    private final SingletonImpl singletonService;

    @Autowired
    public ConstructorConcreteConsumer(final SingletonImpl singletonService) {
        this.singletonService = singletonService;
    }

    public SingletonImpl getSingletonService() {
        return singletonService;
    }

}
