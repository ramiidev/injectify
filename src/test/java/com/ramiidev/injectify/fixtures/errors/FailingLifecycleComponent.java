package com.ramiidev.injectify.fixtures.errors;

import com.ramiidev.injectify.shared.annotation.Lazy;
import com.ramiidev.injectify.shared.annotation.PostConstruct;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
@Lazy
public class FailingLifecycleComponent {

    @PostConstruct
    void initialize() {
        throw new IllegalStateException("lifecycle exploded");
    }
}
