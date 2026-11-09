package com.ramiidev.injectify.infrastructure.logging.support;

import com.ramiidev.injectify.infrastructure.logging.LogSettings;
import com.ramiidev.injectify.infrastructure.logging.LoggingSystem;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import java.util.Objects;
/**
 * Implementación de {@link LoggingSystem} que configura Apache Log4j2 programáticamente.
 * <p>
 * Esta clase actúa como el orquestador de la configuración de logs. Utiliza los valores
 * definidos en el objeto de valor {@link LogSettings} (extraídos del entorno) en una
 * estructura de configuración válida de Log4j2 (Appenders, Layouts, Policies y Root Logger).
 * </p>
 * <p>
 * El uso de esta clase elimina la necesidad de depender de archivos de configuración
 * externos como {@code log4j2.xml} o {@code log4j2.json}, permitiendo una configuración
 * dinámica y centralizada.
 * </p>
 *
 * @see LoggingSystem
 * @see LogSettings
 */
public final class Log4J2LoggingSystem implements LoggingSystem {

    private static final String CONFIG_NAME = "InjectifyProgrammaticConfig";
    private static final String CONSOLE_APPENDER = "Stdout";
    private static final String FILE_APPENDER = "File";
    private static final String LAYOUT_TYPE = "PatternLayout";

    // Constantes para la política de rotación de archivos
    private static final String ROTATION_SIZE = "10MB";
    private static final String MAX_BACKUP_FILES = "5";
    private static final String GZ_FILE_SUFFIX = "-%d{MM-dd-yy}.log.gz";

    /**
     * Inicializa el sistema de logging Log4j2 aplicando la configuración proporcionada.
     * <p>
     * Este método realiza las siguientes acciones:
     * <ol>
     * <li>Verifica si la configuración es válida y si los logs están habilitados.</li>
     * <li>Si están deshabilitados, establece el nivel raíz en {@link Level#OFF}.</li>
     * <li>Construye los Appenders de Consola y Archivo (si aplica).</li>
     * <li>Configura el Logger Raíz (Root Logger) y vincula los appenders.</li>
     * <li>Aplica la configuración en caliente mediante {@link Configurator#reconfigure}.</li>
     * </ol>
     *
     * @param settings Objeto inmutable que contiene los parámetros de configuración
     * (niveles, patrones, rutas de archivo, etc.).
     */
    @Override
    public void initialize(LogSettings settings) {
        if (Objects.isNull(settings) || !settings.enabled()) {
            Configurator.setRootLevel(Level.OFF);
            return;
        }

        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setConfigurationName(CONFIG_NAME);

        configureConsole(builder, settings);
        configureFile(builder, settings);
        configureRootLogger(builder, settings);

        Configurator.reconfigure(builder.build());
    }

    /**
     * Configura y añade el Appender de Consola (System.out) al builder.
     *
     * @param builder  El constructor de la configuración de Log4j2 en proceso.
     * @param settings La configuración que contiene el patrón de formato para la consola.
     */
    private void configureConsole(ConfigurationBuilder<BuiltConfiguration> builder, LogSettings settings) {
        AppenderComponentBuilder appender = builder.newAppender(CONSOLE_APPENDER, "Console")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)
                .add(createLayout(builder, settings.consolePattern()));

        builder.add(appender);
    }

    /**
     * Configura y añade el Appender de Archivo con rotación (RollingFile) al builder,
     * únicamente si la escritura en archivo está habilitada en la configuración.
     * <p>
     * Incluye políticas de rotación por tamaño y estrategias de compresión (.gz).
     * </p>
     *
     * @param builder  El constructor de la configuración de Log4j2 en proceso.
     * @param settings La configuración que contiene la ruta, patrón y estado de habilitación del archivo.
     */
    private void configureFile(ConfigurationBuilder<BuiltConfiguration> builder, LogSettings settings) {
        if (!settings.fileEnabled()) {
            return;
        }

        String filePath = settings.filePath();
        String rotationPattern = filePath + GZ_FILE_SUFFIX;

        AppenderComponentBuilder appender = builder.newAppender(FILE_APPENDER, "RollingFile")
                .addAttribute("fileName", filePath)
                .addAttribute("filePattern", rotationPattern)
                .add(createLayout(builder, settings.filePattern()))
                .addComponent(createTriggeringPolicy(builder))
                .addComponent(createRolloverStrategy(builder));

        builder.add(appender);
    }

    /**
     * Configura el Root Logger (Logger Raíz), establece su nivel de severidad y le
     * asocia los appenders configurados previamente.
     *
     * @param builder  El constructor de la configuración de Log4j2.
     * @param settings La configuración que define el nivel raíz (DEBUG, INFO, etc.) y si se usa archivo.
     */
    private void configureRootLogger(ConfigurationBuilder<BuiltConfiguration> builder, LogSettings settings) {
        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(settings.rootLevel());

        // La consola siempre se añade por defecto
        rootLogger.add(builder.newAppenderRef(CONSOLE_APPENDER));

        // El archivo solo se añade si estaba habilitado
        if (settings.fileEnabled()) {
            rootLogger.add(builder.newAppenderRef(FILE_APPENDER));
        }

        builder.add(rootLogger);
    }

    /**
     * Crea un componente de Layout (formato visual del log) basado en un patrón.
     *
     * @param builder El constructor de configuración.
     * @param pattern El patrón de texto (ej: "%d [%t] %-5level: %msg%n").
     * @return Un {@link LayoutComponentBuilder} listo para ser añadido a un Appender.
     */
    private LayoutComponentBuilder createLayout(ConfigurationBuilder<BuiltConfiguration> builder, String pattern) {
        return builder.newLayout(LAYOUT_TYPE)
                .addAttribute("pattern", pattern);
    }

    /**
     * Crea la política de disparo (Triggering Policy) que determina cuándo rotar el archivo.
     * Actualmente configurado para rotar basado en tamaño (SizeBasedTriggeringPolicy).
     *
     * @param builder El constructor de configuración.
     * @return Un {@link ComponentBuilder} configurado con el tamaño máximo (ej: 10MB).
     */
    private ComponentBuilder<?> createTriggeringPolicy(ConfigurationBuilder<BuiltConfiguration> builder) {
        return builder.newComponent("Policies")
                .addComponent(builder.newComponent("SizeBasedTriggeringPolicy")
                        .addAttribute("size", ROTATION_SIZE));
    }

    /**
     * Crea la estrategia de rotación (Rollover Strategy) que determina cuántos archivos
     * de respaldo mantener y cómo nombrarlos.
     *
     * @param builder El constructor de configuración.
     * @return Un {@link ComponentBuilder} configurado con el máximo de archivos de backup.
     */
    private ComponentBuilder<?> createRolloverStrategy(ConfigurationBuilder<BuiltConfiguration> builder) {
        return builder.newComponent("DefaultRolloverStrategy")
                .addAttribute("max", MAX_BACKUP_FILES);
    }
}