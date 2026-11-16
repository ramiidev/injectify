package com.ramiidev.injectify.fixtures.qualifier;

import com.ramiidev.injectify.fixtures.configuration.Processor;
import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.Qualifier;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
public class QualifiedConsumer {
    private final Processor processor;

    @Autowired
    public QualifiedConsumer(@Qualifier("secondaryProcessor") Processor processor) {
        this.processor = processor;
    }

    public Processor getProcessor() {
        return processor;
    }
}
