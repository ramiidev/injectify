package com.ramiidev.injectify.infrastructure.logging;

import com.ramiidev.injectify.configuration.env.Environment;
import org.apache.logging.log4j.Level;

import java.util.Objects;

/**
 * Value Object inmutable que contiene la configuración purificada de los logs.
 * Separa la lectura/validación (aquí) de la ejecución (Configurator).
 */
public record LogSettings(
        boolean enabled,
        Level rootLevel,
        String consolePattern,
        boolean fileEnabled,
        String filePath,
        String filePattern
) {

    private static final String KEY_ENABLED = "injectify.log.enabled";
    private static final String KEY_LEVEL = "injectify.log.level";
    private static final String KEY_PATTERN = "injectify.log.pattern";
    private static final String KEY_FILE_ENABLED = "injectify.log.file.enabled";
    private static final String KEY_FILE_PATH = "injectify.log.file.path";
    private static final String KEY_FILE_PATTERN = "injectify.log.file.pattern";

    private static final String DEFAULT_PATTERN = "%style{%d{yyyy-MM-dd HH:mm:ss}} %highlight{%-5level} %style{%-40.40c{1.}}{cyan} - %msg%n";
    private static final String DEFAULT_FILE_PATTERN = "%d{yyyy-MM-dd HH:mm:ss} %-5level %-40.40c{1.} - %msg%n";
    private static final String DEFAULT_FILE_PATH = "logs/application.log";

    /**
     * Factory method que extrae y valida la configuración del entorno.
     */
    public static LogSettings from(Environment env) {
        boolean enabled = env.getProperty(KEY_ENABLED, Boolean.class, true);
        String  consolePattern = env.getProperty(KEY_PATTERN, String.class, DEFAULT_PATTERN);

        Level level;
        try {
            String levelStr = env.getProperty(KEY_LEVEL, String.class, Level.INFO.toString());
            level = Level.valueOf(levelStr.toUpperCase());
        } catch (Exception _) {
            level = Level.INFO;
        }


        String filePath = env.getProperty(KEY_FILE_PATH, String.class, DEFAULT_FILE_PATH);
        String filePattern = env.getProperty(KEY_FILE_PATTERN, String.class, DEFAULT_FILE_PATTERN);
        boolean fileExplicitlyEnabled = env.getProperty(KEY_FILE_ENABLED, Boolean.class, false);
        boolean fileEnabled = fileExplicitlyEnabled && Objects.nonNull(filePath) && !filePath.isBlank();

        return new LogSettings(
                enabled,
                level,
                consolePattern,
                fileEnabled,
                filePath,
                filePattern
        );
    }
}
