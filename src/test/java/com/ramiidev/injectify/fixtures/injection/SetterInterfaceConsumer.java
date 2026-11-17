package com.ramiidev.injectify.fixtures.injection;

import com.ramiidev.injectify.fixtures.scope.Singleton;
import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
public class SetterInterfaceConsumer {

    private Singleton singleton;

    @Autowired
    public void setSingletonService(final Singleton singleton) {
        this.singleton = singleton;
    }

    public Singleton getSingletonService() {
        return singleton;
    }

}
