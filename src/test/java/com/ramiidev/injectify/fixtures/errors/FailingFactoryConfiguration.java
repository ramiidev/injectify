package com.ramiidev.injectify.fixtures.errors;

import com.ramiidev.injectify.shared.annotation.Bean;
import com.ramiidev.injectify.shared.annotation.Lazy;
import com.ramiidev.injectify.shared.annotation.stereotype.Configuration;

@Configuration
public class FailingFactoryConfiguration {

    @Bean
    @Lazy
    public FailureProduct failingProduct() {
        throw new IllegalStateException("factory exploded");
    }

    public static final class FailureProduct {
    }
}
