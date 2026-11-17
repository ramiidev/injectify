package com.ramiidev.injectify.fixtures.cycle;

import com.ramiidev.injectify.fixtures.configuration.Product;
import com.ramiidev.injectify.fixtures.configuration.DefaultProduct;
import com.ramiidev.injectify.shared.annotation.Bean;
import com.ramiidev.injectify.shared.annotation.stereotype.Configuration;

@Configuration
public class ProxyConfiguration {

    @Bean
    public Product product() {
        return new DefaultProduct();
    }
}
