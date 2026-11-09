package com.ramiidev.injectify.core.diagnostic;

public class PostgresDependencyFailureAnalyzer extends AbstractFailureAnalyzer<ClassNotFoundException> {

    @Override
    protected Class<ClassNotFoundException> getSupportedType() {
        return ClassNotFoundException.class;
    }

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, ClassNotFoundException cause) {
        // La clase abstracta ya hizo el trabajo sucio. Aquí "cause" ya es el ClassNotFoundException.
        if (cause.getMessage() != null && cause.getMessage().contains("org.postgresql.Driver")) {
            return new FailureAnalysis(
                    "Estás intentando usar PostgreSQL, pero no se encontró el Driver JDBC.",
                    "Agrega la dependencia 'org.postgresql:postgresql' a tu proyecto para solucionar esto.",
                    rootFailure
            );
        }
        // Si faltaba otra clase que NO era de Postgres, retornamos null para que otro analizador lo intente.
        return null;
    }
}