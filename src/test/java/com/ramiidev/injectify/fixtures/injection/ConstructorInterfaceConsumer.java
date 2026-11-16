package com.ramiidev.injectify.fixtures.injection;

import com.ramiidev.injectify.fixtures.scope.SingletonService;
import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
public class ConstructorInterfaceConsumer {

    private final SingletonService singletonService;

    @Autowired
    public ConstructorInterfaceConsumer(final SingletonService singletonService) {
        this.singletonService = singletonService;
    }

    public SingletonService getSingletonService() {
        return singletonService;
    }

}
