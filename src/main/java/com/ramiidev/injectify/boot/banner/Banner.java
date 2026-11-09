package com.ramiidev.injectify.boot.banner;

import java.io.PrintStream;

/**
 * Interfaz para imprimir un banner al inicio de la aplicación.
 */
@FunctionalInterface
public interface Banner {

    /**
     * Imprime el banner en el flujo de salida proporcionado.
     * @param out El flujo de salida (generalmente System.out).
     */
    void printBanner(PrintStream out);
}