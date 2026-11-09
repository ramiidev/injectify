package com.ramiidev.injectify.configuration.env;

public interface MutableEnvironment extends Environment {
    void addFirstPropertySource(PropertySource ps);
}
