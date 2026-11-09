package com.ramiidev.injectify.core.context.support;

import com.ramiidev.injectify.boot.autoconfigure.AutoConfigurationSorter;
import com.ramiidev.injectify.boot.autoconfigure.EnableAutoConfiguration;
import com.ramiidev.injectify.boot.autoconfigure.StartInjectifyApplication;
import com.ramiidev.injectify.boot.autoconfigure.InjectifyImportsLoader;
import com.ramiidev.injectify.boot.autoconfigure.core.ScanProperties;
import com.ramiidev.injectify.configuration.binder.support.ConfigurationPropertyBinder;
import com.ramiidev.injectify.configuration.env.Environment;
import com.ramiidev.injectify.core.context.ApplicationContext;
import com.ramiidev.injectify.core.convert.support.ConversionService;
import com.ramiidev.injectify.core.scanner.ClassPathScanner;
import com.ramiidev.injectify.core.scanner.ScanCacheManager;
import com.ramiidev.injectify.core.scanner.ScanMode;
import com.ramiidev.injectify.shared.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del contexto de aplicación que carga componentes
 * a partir de una clase de configuración anotada con @StartApplicationSwing.
 * Extiende la clase base InjectifyApplicationContext para añadir la lógica
 * de escaneo basada en anotaciones.
 */
public class AnnotationConfigAbstractApplicationContext extends AbstractApplicationContext {

    private final Logger log = LoggerFactory.getLogger(AnnotationConfigAbstractApplicationContext.class);
    private final ClassPathScanner scanner = new ClassPathScanner();
    private final ScanCacheManager cacheManager = new ScanCacheManager();
    private final Set<Class<?>> componentSources = new HashSet<>();
    private final ConfigurationPropertyBinder configurationPropertyBinder;

    /**
     * Crea un nuevo contexto a partir de clases fuente (MainApp, Tests, Configs).
     */
    public AnnotationConfigAbstractApplicationContext(Environment environment,
                                                      ConversionService conversionService,
                                                      Class<?>... componentClasses) {
        super(environment, conversionService);

        if (componentClasses == null || componentClasses.length == 0) {
            throw new IllegalArgumentException("Debes proporcionar al menos una clase fuente para iniciar el contexto.");
        }

        this.configurationPropertyBinder = new ConfigurationPropertyBinder(conversionService);
        this.componentSources.addAll(Arrays.asList(componentClasses));

        // Registramos beans de infraestructura
        this.registerSingleton(getBeanNameForClass(Environment.class), environment);
        this.registerSingleton(getBeanNameForClass(ApplicationContext.class), this);

        log.info("Contexto inicializado con {} fuentes de configuración.", componentClasses.length);
    }

    @Override
    public void refresh() {
        // 1. FILTRADO DE CLASES DE USUARIO
        Set<Class<?>> userComponentClasses = resolveComponentClasses()
                .stream()
                .collect(Collectors.toUnmodifiableSet());

        // 2. REGISTRO DE COMPONENTES DEL USUARIO (FASE 1)
        super.prepareBeanDefinitions(userComponentClasses);

        // FASE 2: AUTO-CONFIGURACIÓN AVANZADA
        boolean autoConfigEnabled = componentSources.stream()
                .anyMatch(this::isAutoConfigurationEnabled);

        if (autoConfigEnabled) {
            // A. Carga cruda
            Set<Class<?>> rawConfigClasses = InjectifyImportsLoader.loadFactoryClasses(EnableAutoConfiguration.class);
            // B. ORDENAMIENTO INTELIGENTE (Topological Sort)
            AutoConfigurationSorter sorter = new AutoConfigurationSorter();
            List<Class<?>> sortedConfigClasses = sorter.getInPriorityOrder(rawConfigClasses);

            // LOG PARA DEBUG (Verificar el orden)
            sortedConfigClasses.forEach(c -> log.debug("AutoConfig Order: {}", c.getSimpleName()));

            // C. Registro
            super.prepareBeanDefinitions(new LinkedHashSet<>(sortedConfigClasses));
        }

        log.info("Contexto de aplicación refrescado correctamente.");
        log.debug("Se registraron {} definiciones de beans.", getBeanDefinitionCount());

        // 4. (Opcional) Si quieres Lazy Loading, no pongas nada más.
        // Si quieres Eager Loading del Core, llamarías a finishBeanFactoryInitialization() aquí.
        finishBeanFactoryInitialization();
    }

    @Override
    public void registerSingleton(Class<?> type, Object singletonObject) {
        this.registerSingleton(getBeanNameForClass(type), singletonObject);
    }

    protected void finishBeanFactoryInitialization() {
        super.preInstantiateSingletons();
    }

    /**
     * Verifica si la auto-configuración está habilitada en la clase fuente.
     * Busca la anotación @EnableAutoConfiguration directa o indirectamente.
     */
    private boolean isAutoConfigurationEnabled(Class<?> source) {
        // 1. Chequeo Directo:
        // ¿La clase tiene @EnableAutoConfiguration pegado explícitamente?
        if (source.isAnnotationPresent(EnableAutoConfiguration.class)) {
            return true;
        }

        // 2. Chequeo de Meta-Anotaciones (Indirecto):
        // Recorre todas las anotaciones que tiene la clase (ej: @StartInjectifyApplication)
        for (Annotation annotation : source.getAnnotations()) {
            // Revisa si esa anotación tiene a su vez @EnableAutoConfiguration
            // Esto permite crear anotaciones compuestas.
            if (annotation.annotationType().isAnnotationPresent(EnableAutoConfiguration.class)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Resuelve las clases de componentes, ya sea escaneando o cargando del caché.
     */
    private Set<Class<?>> resolveComponentClasses() {
        // 1. Leer configuración de escaneo desde el Environment
        // (Se asume que ScanProperties es una clase @ConfigurationProperties("injectify.scan"))
        ScanProperties scanProperties = configurationPropertyBinder.bind(ScanProperties.class, environment);
        ScanMode mode = scanProperties.getMode();
        String cacheFile = scanProperties.getFilePath();

        // --- MODO ACTIVATE: Carga rápida desde caché ---
        if (mode == ScanMode.ACTIVATE) {
            log.info("Modo ACTIVATE: Cargando clases desde caché binario en '{}'...", cacheFile);
            return cacheManager.loadCache(cacheFile);
        }

        // --- MODO DEFAULT/GENERATE: Escaneo normal ---
        Set<Class<?>> detectedClasses = new HashSet<>();

        for (Class<?> source : componentSources) {
            if (source.isAnnotationPresent(StartInjectifyApplication.class)) {
                log.debug("Escaneando paquete desde fuente principal: {}", source.getName());
                detectedClasses.addAll(scanFromSource(source));
            } else {
                // Si es una clase de configuración suelta (sin @Start...), la agregamos directamente
                detectedClasses.add(source);
            }
        }

        // --- MODO GENERATE: Guardar el resultado para la próxima vez ---
        if (mode == ScanMode.GENERATE) {
            log.info("Modo GENERATE: Guardando caché de escaneo en '{}'...", cacheFile);
            cacheManager.saveCache(detectedClasses, cacheFile);
        }

        return detectedClasses;
    }

    private Set<Class<?>> scanFromSource(Class<?> source) {
        StartInjectifyApplication annotation = source.getAnnotation(StartInjectifyApplication.class);
        String specificPackage = annotation.value();

        if (specificPackage != null && !specificPackage.trim().isEmpty()) {
            return scanner.scan(specificPackage);
        } else {
            try {
                URL rootUrl = PathUtils.calculateProjectRootUrl(source);
                return scanner.scan(rootUrl);
            } catch (Exception e) {
                throw new RuntimeException("No se pudo determinar la ruta de escaneo raíz para " + source.getName(), e);
            }
        }
    }
}