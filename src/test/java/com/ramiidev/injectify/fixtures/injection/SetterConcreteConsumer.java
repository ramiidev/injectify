package com.ramiidev.injectify.fixtures.injection;

import com.ramiidev.injectify.fixtures.scope.SingletonServiceImpl;
import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
public class SetterConcreteConsumer {

    private SingletonServiceImpl singletonService;

    @Autowired
    public void setSingletonService(final SingletonServiceImpl singletonService) {
        this.singletonService = singletonService;
    }

    public SingletonServiceImpl getSingletonService() {
        return singletonService;
    }

}
