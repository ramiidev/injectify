package com.ramiidev.injectify.boot.banner.support;

import com.ramiidev.injectify.boot.banner.Banner;

import java.io.PrintStream;
import java.net.URL;

/**
 * Clase encargada de localizar e imprimir el banner adecuado.
 */
public class BannerPrinter {

    private static final String BANNER_LOCATION = "banner.txt";

    /**
     * Imprime el banner en System.out.
     */
    @SuppressWarnings("java:S106")
    public void print() {
        print(System.out);
    }

    /**
     * Imprime el banner en el flujo especificado.
     */
    public void print(PrintStream out) {
        Banner banner = getBanner();
        banner.printBanner(out);
    }

    private Banner getBanner() {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(BANNER_LOCATION);

        if (resource != null) {
            return new ResourceBanner(resource);
        }

        return new InjectifyBanner();
    }
}
