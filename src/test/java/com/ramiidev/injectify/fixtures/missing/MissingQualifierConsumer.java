package com.ramiidev.injectify.fixtures.missing;

import com.ramiidev.injectify.fixtures.configuration.Processor;
import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.Lazy;
import com.ramiidev.injectify.shared.annotation.Qualifier;
import com.ramiidev.injectify.shared.annotation.stereotype.Component;

@Component
@Lazy
public class MissingQualifierConsumer {

    @Autowired
    public MissingQualifierConsumer(
            @Qualifier("doesNotExist") Processor processor) {
    }
}
