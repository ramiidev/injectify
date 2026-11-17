package com.ramiidev.injectify.fixtures.configuration;

public final class DependentBean {
    private final Product product;

    public DependentBean(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
}
