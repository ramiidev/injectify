package com.ramiidev.injectify.core;

import com.ramiidev.injectify.core.context.ApplicationContext;
import com.ramiidev.injectify.core.support.TestContextFactory;
import com.ramiidev.injectify.fixtures.scanner.ScannedComponent;
import com.ramiidev.injectify.fixtures.scanner.ScannerApplication;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ScannerContractTest {

    @Test
    void scansComponentsFromStartApplicationPackage() {
        ApplicationContext context = TestContextFactory.create(ScannerApplication.class);

        try {
            assertNotNull(context.getBean(ScannedComponent.class));
        } finally {
            context.destroy();
        }
    }

    @Test
    void generatesAndActivatesScannerCache() throws Exception {
        Path cache = Files.createTempFile("injectify-scan-", ".bin");
        Files.deleteIfExists(cache);
        Map<String, String> generate = Map.of(
                "scan.mode", "GENERATE",
                "scan.file-path", cache.toString()
        );
        Map<String, String> activate = Map.of(
                "scan.mode", "ACTIVATE",
                "scan.file-path", cache.toString()
        );

        ApplicationContext generated = TestContextFactory.create(generate, ScannerApplication.class);
        generated.destroy();

        try {
            ApplicationContext activated = TestContextFactory.create(activate, ScannerApplication.class);
            try {
                assertNotNull(activated.getBean(ScannedComponent.class));
            } finally {
                activated.destroy();
            }
        } finally {
            Files.deleteIfExists(cache);
        }
    }
}
