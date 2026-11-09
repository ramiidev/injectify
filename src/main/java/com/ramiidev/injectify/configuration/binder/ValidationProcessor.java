package com.ramiidev.injectify.configuration.binder;

import com.ramiidev.injectify.configuration.exception.BinderException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

public class ValidationProcessor {
    private Validator validator;

    public ValidationProcessor() {
        try {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            this.validator = factory.getValidator();
        } catch (Exception _) {
            this.validator = null; // Validation API not present or failed
        }
    }

    public void validate(Object bean) {
        if (validator != null) {
            Set<?> violations = validator.validate(bean);
            if (!violations.isEmpty()) {
                throw new BinderException("Validation failed for " + bean.getClass().getSimpleName() + ": " + violations);
            }
        }
    }
}
