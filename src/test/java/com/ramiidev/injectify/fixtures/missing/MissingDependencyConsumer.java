package com.ramiidev.injectify.fixtures.missing;

import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.Lazy;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
@Lazy
public class MissingDependencyConsumer {

    @Autowired
    public MissingDependencyConsumer(MissingDependency dependency) {
    }
}
