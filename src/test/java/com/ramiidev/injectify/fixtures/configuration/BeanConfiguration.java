package com.ramiidev.injectify.fixtures.configuration;

import com.ramiidev.injectify.core.beans.definition.BeanScope;
import com.ramiidev.injectify.shared.annotation.Bean;
import com.ramiidev.injectify.shared.annotation.Scope;
import com.ramiidev.injectify.shared.annotation.stereotype.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public Product defaultProduct() {
        return new DefaultProduct();
    }

    @Bean("namedProduct")
    public Product namedProduct() {
        return new NamedProduct();
    }

    @Bean("primaryProcessor")
    public Processor primaryProcessor() {
        return new PrimaryProcessor();
    }

    @Bean("secondaryProcessor")
    public Processor secondaryProcessor() {
        return new SecondaryProcessor();
    }

    @Bean("prototypeProduct")
    @Scope(BeanScope.PROTOTYPE)
    public Product prototypeProduct() {
        return new PrototypeProduct();
    }
}
