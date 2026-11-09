package com.ramiidev.injectify.core.convert;

@FunctionalInterface
public interface Converter<S, T> {
    T convert(S source);
}
