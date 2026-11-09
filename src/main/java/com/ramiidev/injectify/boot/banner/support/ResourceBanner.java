package com.ramiidev.injectify.boot.banner.support;

import com.ramiidev.injectify.boot.banner.Banner;
import com.ramiidev.injectify.infrastructure.logging.support.DeferredLogFactory;
import com.ramiidev.injectify.infrastructure.logging.support.DeferredLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Banner que se carga desde un recurso específico (ej. banner.txt).
 */
public class ResourceBanner implements Banner {

    private static final DeferredLogger log = DeferredLogFactory.getLog(ResourceBanner.class);
    private final URL resourceUrl;

    public ResourceBanner(URL resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    @Override
    public void printBanner(PrintStream out) {
        try (InputStream stream = resourceUrl.openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                out.println(line);
            }
            out.println();

        } catch (Exception e) {
            String message = String.format("Error al leer el recurso del banner: %s", resourceUrl);
            log.warn(message, e);
            new InjectifyBanner().printBanner(out);
        }
    }
}
