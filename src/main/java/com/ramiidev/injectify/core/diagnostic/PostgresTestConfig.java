package com.ramiidev.injectify.core.diagnostic;

import com.ramiidev.injectify.shared.annotation.Bean;
import com.ramiidev.injectify.shared.annotation.ConditionalOnProperty;


// Esta configuración SOLO se carga si pones 'db.type=postgres' en tus argumentos
@ConditionalOnProperty(name = "db.type", havingValue = "postgres")
public class PostgresTestConfig {

    @Bean
    public Object simulacionDataSource() throws ClassNotFoundException {
        // AQUÍ ESTÁ EL TRUCO:
        // Intentamos cargar la clase. Como no tienes el driver en el pom.xml,
        // esto lanzará ClassNotFoundException -> BeanInstantiationException -> Crash.
        Class.forName("org.postgresql.Driver");
        return "Conexión Exitosa (Si ves esto, el test falló porque sí tienes el driver)";
    }
}
