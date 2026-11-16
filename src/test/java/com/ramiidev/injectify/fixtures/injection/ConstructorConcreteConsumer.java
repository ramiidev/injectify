package com.ramiidev.injectify.fixtures.injection;

import com.ramiidev.injectify.fixtures.scope.SingletonServiceImpl;
import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
public class ConstructorConcreteConsumer {

    private final SingletonServiceImpl singletonService;

    @Autowired
    public ConstructorConcreteConsumer(final SingletonServiceImpl singletonService) {
        this.singletonService = singletonService;
    }

    public SingletonServiceImpl getSingletonService() {
        return singletonService;
    }

}
