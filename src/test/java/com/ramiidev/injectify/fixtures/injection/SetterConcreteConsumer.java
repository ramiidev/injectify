package com.ramiidev.injectify.fixtures.injection;

import com.ramiidev.injectify.fixtures.scope.SingletonImpl;
import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
public class SetterConcreteConsumer {

    private SingletonImpl singletonService;

    @Autowired
    public void setSingletonService(final SingletonImpl singletonService) {
        this.singletonService = singletonService;
    }

    public SingletonImpl getSingletonService() {
        return singletonService;
    }

}
