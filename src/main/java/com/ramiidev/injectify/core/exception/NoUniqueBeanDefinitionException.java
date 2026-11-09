package com.ramiidev.injectify.core.exception;

public class NoUniqueBeanDefinitionException extends BeansException {
    public NoUniqueBeanDefinitionException(Class<?> type, int found) {
        super("Ambigüedad: Se encontraron " + found + " beans del tipo " + type.getName());
    }
}
