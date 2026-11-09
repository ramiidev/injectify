package com.ramiidev.injectify.configuration.binder;

import com.ramiidev.injectify.configuration.env.Environment;

import java.util.HashSet;
import java.util.Set;

public class BinderContext {
    public final Environment env;
    public final String prefix;

    // Claves exactas ya mapeadas (ej: app.name)
    public final Set<String> claimedKeys = new HashSet<>();

    // Prefijos de objetos anidados (ej: app.license)
    // Esto sirve para que el catch-all no toque nada que empiece con esto.
    public final Set<String> nestedNamespaces = new HashSet<>();

    public BinderContext(Environment env, String prefix) {
        this.env = env;
        this.prefix = prefix;
    }
}