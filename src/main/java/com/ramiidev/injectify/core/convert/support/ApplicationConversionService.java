package com.ramiidev.injectify.core.convert.support;

import com.ramiidev.injectify.core.convert.converters.StringToDurationConverter;
import com.ramiidev.injectify.core.convert.converters.StringToEnumConverterFactory;

import java.time.Duration;

public class ApplicationConversionService extends DefaultConversionService {

    private static ApplicationConversionService sharedInstance;

    private ApplicationConversionService() {
        super(); // 1. Carga defaults
        registerApplicationConverters(); // 2. Carga avanzados
    }

    public static ApplicationConversionService getSharedInstance() {
        ApplicationConversionService instance = sharedInstance;
        if (instance == null) {
            synchronized (ApplicationConversionService.class) {
                instance = sharedInstance;
                if (instance == null) {
                    instance = new ApplicationConversionService();
                    sharedInstance = instance;
                }
            }
        }
        return instance;
    }

    private void registerApplicationConverters() {
        addConverter(Duration.class, new StringToDurationConverter());
        addConverterFactory(Enum.class, new StringToEnumConverterFactory());
    }
}