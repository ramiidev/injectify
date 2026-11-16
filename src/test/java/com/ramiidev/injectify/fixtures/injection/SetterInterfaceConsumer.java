package com.ramiidev.injectify.fixtures.injection;

import com.ramiidev.injectify.fixtures.scope.SingletonService;
import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
public class SetterInterfaceConsumer {

    private SingletonService singletonService;

    @Autowired
    public void setSingletonService(final SingletonService singletonService) {
        this.singletonService = singletonService;
    }

    public SingletonService getSingletonService() {
        return singletonService;
    }

}
