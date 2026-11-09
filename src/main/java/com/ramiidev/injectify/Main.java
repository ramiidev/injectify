package com.ramiidev.injectify;

import com.ramiidev.injectify.boot.InjectifyApplication;
import com.ramiidev.injectify.boot.autoconfigure.StartInjectifyApplication;

@StartInjectifyApplication
public class Main {
    static void main(String[] args) {
        // Simulamos que el usuario pasó este argumento por consola
        String[] appArgs = new String[]{"--db.type=postgres"};
        InjectifyApplication.run(Main.class, appArgs);

    }
}
