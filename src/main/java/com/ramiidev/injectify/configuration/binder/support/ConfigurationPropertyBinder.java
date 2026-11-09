package com.ramiidev.injectify.configuration.binder.support;

import com.ramiidev.injectify.configuration.env.Environment;
import com.ramiidev.injectify.core.convert.support.ConversionService;

public class ConfigurationPropertyBinder {
        // Instanciamos el servicio de conversión una sola vez
        private final JavaBeanBinder binder;

        public ConfigurationPropertyBinder(ConversionService conversionService) {
            binder = new JavaBeanBinder(conversionService);
        }

        /**
         * Crea una instancia de la clase dada y le inyecta las propiedades del entorno.
         * * @param type La clase del objeto de configuración (ej: LogProperties.class)
         *
         * @param type La clase del objeto de configuración.
         * @param env El entorno de donde sacar los valores
         * @return La instancia llena y validada
         */
        public <T> T bind(Class<T> type, Environment env) {
            return binder.bind(type, env);
        }

    }