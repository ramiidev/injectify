package com.ramiidev.injectify.fixtures.configuration;

import com.ramiidev.injectify.shared.annotation.Bean;
import com.ramiidev.injectify.shared.annotation.stereotype.Configuration;

@Configuration
public class DependentBeanConfiguration {

    @Bean
    public Product product() {
        return new DefaultProduct();
    }

    @Bean
    public DependentBean dependentBean(Product product) {
        return new DependentBean(product);
    }
}
