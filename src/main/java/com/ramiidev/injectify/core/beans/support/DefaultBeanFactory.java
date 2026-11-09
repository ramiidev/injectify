package com.ramiidev.injectify.core.beans.support;

import com.ramiidev.injectify.configuration.annotation.ConfigurationProperties;
import com.ramiidev.injectify.configuration.binder.support.ConfigurationPropertyBinder;
import com.ramiidev.injectify.configuration.env.Environment;
import com.ramiidev.injectify.core.aop.InjectifyProxyFactory;
import com.ramiidev.injectify.core.beans.BeanDefinitionRegistry;
import com.ramiidev.injectify.core.beans.BeanFactory;
import com.ramiidev.injectify.core.beans.definition.BeanDefinition;
import com.ramiidev.injectify.core.beans.lifecycle.DisposableBean;
import com.ramiidev.injectify.core.convert.support.ConversionService;
import com.ramiidev.injectify.core.exception.BeanCreationException;
import com.ramiidev.injectify.core.exception.BeanDefinitionOverrideException;
import com.ramiidev.injectify.core.exception.BeanInitializationException;
import com.ramiidev.injectify.core.exception.BeanInstantiationException;
import com.ramiidev.injectify.core.exception.NoSuchBeanDefinitionException;
import com.ramiidev.injectify.core.exception.NoUniqueBeanDefinitionException;
import com.ramiidev.injectify.core.exception.UnsatisfiedDependencyException;
import com.ramiidev.injectify.shared.annotation.Autowired;
import com.ramiidev.injectify.shared.annotation.PostConstruct;
import com.ramiidev.injectify.shared.annotation.Qualifier;
import com.ramiidev.injectify.shared.annotation.Value;
import com.ramiidev.injectify.shared.annotation.stereotype.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DefaultBeanFactory extends DefaultSingletonBeanRegistry
        implements BeanFactory, BeanDefinitionRegistry, DisposableBean {

    private final Logger log = LoggerFactory.getLogger(DefaultBeanFactory.class);

    // 1. LA LISTA MAESTRA (Aquí registras toodo lo que necesita Proxy)
    private static final Set<Class<? extends Annotation>> PROXY_TRIGGER_ANNOTATIONS = Set.of(
            Configuration.class
            // , Transactional.class  <-- Cuando la crees, solo la agregas aquí y listo.
            // , Async.class
    );
    private static final Set<String> EXCLUDED_PACKAGE_PREFIXES = Set.of(
            "javax.swing",
            "java.awt",
            "java.lang"
    );

    // Bandera para proteger el cierre
    private volatile boolean singletonsCurrentlyInDestruction = false;

    // MAPA: Para búsquedas instantáneas O(1) y conteo .size()
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    // COLA: Solo para mantener el orden de inserción O(1)
    private final Queue<String> beanDefinitionNames = new ConcurrentLinkedQueue<>();
    private final ConfigurationPropertyBinder binder;
    private final ConversionService conversionService;

    public DefaultBeanFactory(ConfigurationPropertyBinder binder,
                              ConversionService conversionService) {
        this.binder = binder;
        this.conversionService = conversionService;
    }

    // =================================================================================================================
    // IMPLEMENTACIÓN DE BEAN FACTORY (LA CARA PÚBLICA DEL CONTEXTO)
    // =================================================================================================================

    @Override
    public Object getBean(String beanName) {
        // 1. Intenta obtener un singleton existente (completo o temprano)
        Object existingBean = super.getSingleton(beanName);
        if (existingBean != null) {
            return existingBean;
        }

        // 2. [NUEVO] PROTECCIÓN DE CIERRE
        // Si no existe y estamos cerrando, PROHIBIDO CREARLO.
        if (singletonsCurrentlyInDestruction) {
            throw new BeanCreationException(beanName,
                    "No se puede crear el bean '" + beanName + "' porque el contexto se está cerrando (destroy).");
        }

        // 2. Si no existe, procede con la creación
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        if (beanDefinition == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }

        if (!beanDefinition.isSingleton()) {
            try {
                return createBean(beanName, beanDefinition, false);
            } catch (Exception e) {
                if (e instanceof BeanInstantiationException || e instanceof UnsatisfiedDependencyException) {
                    throw e;
                }
                throw new BeanCreationException(beanName, "Error al crear el bean '" + beanName + "': " + e.getMessage(), e);
            }
        }

        // Usa la versión de getSingleton que maneja la creación y los ciclos
        return getSingleton(beanName, () -> {
            try {
                return createBean(beanName, beanDefinition);
            } catch (Exception e) {
                if (e instanceof BeanInstantiationException || e instanceof UnsatisfiedDependencyException) {
                    throw e;
                }
                throw new BeanCreationException(beanName, "Error al crear el bean '" + beanName + "': " + e.getMessage(), e);
            }
        });
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        String beanName = resolveBeanNameByType(requiredType, new Annotation[0]);
        if (beanName == null) {
            throw new NoSuchBeanDefinitionException("No se encontró ningún bean del tipo '" + requiredType.getName() + "'.");
        }
        return getBean(beanName, requiredType);
    }

    @Override
    public <T> T getBean(String beanName, Class<T> requiredType) {
        Object bean = getBean(beanName);
        if (!requiredType.isInstance(bean)) {
            throw new NoSuchBeanDefinitionException("El bean '" + beanName + "' no es del tipo esperado '" + requiredType.getName() + "'");
        }
        return requiredType.cast(bean);
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        List<String> candidates = findCandidateBeanNames(type, new Annotation[0]);
        return candidates.toArray(new String[0]);
    }

    @Override
    public void preInstantiateSingletons() {
        // Recorremos TODOS los nombres de beans registrados
        String[] names = getBeanDefinitionNames();
        for (String beanName : names) {

            BeanDefinition bd = getBeanDefinition(beanName);

            // LÓGICA MAESTRA:
            // Si es Singleton Y NO es Perezoso... ¡Créalo YA!
            if (bd.isSingleton() && !bd.isLazyInit()) {

                try {
                    // Al hacer getBean, forzamos la ejecución del constructor o método @Bean.
                    // Si el bean tiene un error (como el Class.forName de Postgres),
                    // explotará AQUÍ MISMO.
                    getBean(beanName);
                } catch (Exception ex) {
                    // Lanzamos una excepción específica para que el Main la atrape
                    throw new BeanCreationException(beanName, "Error fatal pre-instanciando bean", ex);
                }
            }
        }
    }

    // =================================================================================================================
    // FASE 2: IMPLEMENTACIÓN DE BEAN DEFINITION REGISTRY (EL REGISTRO DE "PLANOS")
    // =================================================================================================================

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        // 1. Validación de duplicados usando el MAPA para mayor rapidez
        if (this.beanDefinitionMap.containsKey(beanName)) {
            throw new BeanDefinitionOverrideException(
                    "¡Peligro! Ya existe un bean registrado con el nombre '" + beanName + "'. " +
                            "Probablemente tienes dos métodos @Bean con el mismo nombre. " +
                            "Renombra uno o usa @Bean(name='otroNombre')."
            );
        }

        // 2. Insertar en el mapa
        this.beanDefinitionMap.put(beanName, beanDefinition);

        // 3. Insertar en la cola (Rápido y Thread-Safe)
        this.beanDefinitionNames.add(beanName);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return this.beanDefinitionMap.get(beanName);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionNames.toArray(new String[0]);
    }

    @Override
    public int getBeanDefinitionCount() {
        // No usar beanDefinitionNames.size() porque en ConcurrentLinkedQueue es O(N) (lento).
        // Usar siempre el tamaño del mapa.
        return this.beanDefinitionMap.size();
    }

    @Override
    public void destroy() {
        // 1. ACTIVAR BANDERA: "Estamos cerrando, no aceptamos más pedidos"
        // Esto es útil si en el futuro agregas checks en getBean() para rechazar creaciones durante el cierre.
        this.singletonsCurrentlyInDestruction = true;

        log.info("Iniciando destrucción de beans...");

        // 2. CREAR SNAPSHOT (LA COPIA):
        // Copiamos las claves a una lista separada.
        // ¿Por qué? Para iterar sobre una lista fija y segura. Si iteramos sobre el mapa directo
        // y un bean se borra a sí mismo durante su destroy(), podríamos tener efectos secundarios raros.
        List<String> disposableBeanNames = new ArrayList<>(this.singletonObjects.keySet());

        // 3. ITERAR SOBRE LA COPIA
        for (String beanName : disposableBeanNames) {
            Object bean = this.singletonObjects.get(beanName);

            // Si el bean ya fue removido por otra causa, saltamos
            if (bean == null) continue;

            // --- ZONA DE SEGURIDAD ANTI-RECURSIÓN (Tu StackOverflow Fix) ---

            // A. No autodestrucción: Si la factory está registrada como bean, ignorarla.
            if (bean == this) {
                continue;
            }

            // B. No matar al jefe (Contexto/Environment):
            // Si intentamos destruir el Contexto, él nos volverá a llamar a nosotros.
            // Ignoramos cualquier cosa que parezca un contenedor o infraestructura crítica.
            if (bean instanceof BeanFactory ||
                    bean instanceof BeanDefinitionRegistry ||
                    bean.getClass().getName().contains("ApplicationContext") ||
                    bean.getClass().getName().contains("Environment")) {
                continue;
            }

            // -------------------------------------------------------------

            // 4. DESTRUCCIÓN CONTROLADA
            if (bean instanceof DisposableBean disposableBean) {
                try {
                    log.debug("Destruyendo bean: {}", beanName);
                    disposableBean.destroy();
                } catch (Exception e) {
                    log.error("Error al destruir el bean '{}'", beanName, e);
                }
            }
        }

        // 5. LIMPIEZA FINAL
        // Ahora sí, borramos la memoria real.
        this.singletonObjects.clear();
        this.beanDefinitionMap.clear();
        this.singletonsCurrentlyInDestruction = false; // Reset (opcional)
    }

    // =============================================================================================
    // LÓGICA DE CREACIÓN DE BEANS (CORE)
    // =============================================================================================


    protected Object createBean(String beanName, BeanDefinition beanDefinition) throws BeanInitializationException {
        return createBean(beanName, beanDefinition, true);
    }

    protected Object createBean(String beanName, BeanDefinition beanDefinition, boolean exposeEarly)
            throws BeanInitializationException {
        // 1. Instancia el objeto original (Crudo)
        Object beanInstance = instantiateBean(beanDefinition);

        // 2. Exponer referencia temprana (para dependencias circulares)
        // Nota: Aquí se expone el bean crudo. Si tienes dependencias circulares,
        // recibirán el objeto sin proxy (es una limitación aceptable por ahora).
        if (exposeEarly) {
            addSingletonFactory(beanName, () -> beanInstance);
        }

        // 3. Inyección de dependencias (Llenamos los campos del objeto crudo)
        populateBean(beanInstance);

        // 4. Inicialización (@PostConstruct)
        Object initializedBean = initializeBean(beanInstance);

        // --- INTEGRACIÓN CON BYTEBUDDY ---

        // 5. Decisión de Proxy
        // Solo podemos crear subclases si la clase original NO es final.
        if (shouldProxy(initializedBean.getClass())) {
            try {
                return InjectifyProxyFactory.createProxy(initializedBean);
            } catch (Exception e) {
                throw new BeanInitializationException("Error creando proxy para: " + beanName, e);
            }
        }

        // Si es final o falló algo, devolvemos el bean normal
        return initializedBean;
    }

    // [OPTIMIZADO] Lógica unificada
    private boolean shouldProxy(Class<?> beanClass) {
        // A. REGLAS TÉCNICAS (Hard Fails)
        // -----------------------------------------------------------
        if (Modifier.isFinal(beanClass.getModifiers())) return false;

        String name = beanClass.getName();
        if (name.startsWith("java.") || name.startsWith("javax.") ||
                name.startsWith("sun.") || name.startsWith("jdk.")) {
            return false;
        }

        // B. DETECCIÓN POR ANOTACIONES (Unified Check)
        // -----------------------------------------------------------
        // Aquí detectará @Configuration, @Transactional, o lo que sea que esté en la lista.
        return hasProxyAnnotations(beanClass);
    }

    // [HELPER] El escáner genérico
    private boolean hasProxyAnnotations(Class<?> beanClass) {
        // 1. Revisar anotaciones a nivel de CLASE (Ej. @Configuration, @Service con Transactional implícito)
        for (Annotation ann : beanClass.getAnnotations()) {
            if (PROXY_TRIGGER_ANNOTATIONS.contains(ann.annotationType())) {
                return true; // ¡Encontrado! Salimos rápido.
            }
        }

        // 2. Revisar anotaciones a nivel de MÉTODO (Ej. @Transactional en un solo método)
        // Solo gastamos CPU revisando métodos si la clase no tenía nada.
        for (Method method : beanClass.getDeclaredMethods()) {
            for (Annotation ann : method.getAnnotations()) {
                if (PROXY_TRIGGER_ANNOTATIONS.contains(ann.annotationType())) {
                    return true;
                }
            }
        }

        return false;
    }

    protected Object instantiateBean(BeanDefinition beanDefinition) throws BeanInstantiationException {
        try {
            if (beanDefinition.isFromFactoryMethod()) {
                Object factoryBean = getBean(beanDefinition.getFactoryBeanName());
                Method factoryMethod = findFactoryMethod(factoryBean, beanDefinition.getFactoryMethodName());
                Object[] dependencies = resolveParameters(factoryMethod.getParameters());
                return factoryMethod.invoke(factoryBean, dependencies);
            }

            Class<?> beanClass = beanDefinition.getBeanClass();
            if (beanClass.isAnnotationPresent(ConfigurationProperties.class)) {
                Environment environment = getBean(Environment.class);
                return binder.bind(beanClass, environment);
            } else {
                Constructor<?> constructor = findConstructorToUse(beanClass);
                Object[] dependencies = resolveParameters(constructor.getParameters());
                return constructor.newInstance(dependencies);
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new BeanInstantiationException(beanDefinition.getBeanClass(), "Error de reflexión durante la instancia.", e);
        }
    }

    protected void populateBean(Object beanInstance) throws BeanInitializationException {
        try {
            performSetterInjection(beanInstance);
            performFieldInjection(beanInstance);
        } catch (Exception e) {
            String msg = "Error intentando inyectar dependencias en bean de tipo " + beanInstance.getClass().getName();
            throw new BeanInitializationException(msg, e);
        }

    }

    protected Object initializeBean(Object beanInstance) throws BeanInitializationException {
        try {
            invokePostConstruct(beanInstance);
        } catch (Exception e) {
            String msg = "Error intentando ejecutar método @PostConstruct en bean de tipo " + beanInstance.getClass().getName();
            throw new BeanInitializationException(msg, e);
        }
        return beanInstance;
    }

    // =============================================================================================
    // RESOLUCIÓN DE DEPENDENCIAS E INYECCIÓN
    // =============================================================================================
    private Object[] resolveParameters(Parameter[] parameters) {
        if (parameters.length == 0) return new Object[0];
        Object[] dependencies = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            // [MODIFICADO] Usamos getParameterizedType() para no perder <T>
            Type dependencyType = param.getParameterizedType();

            Object dependency = resolveDependency(dependencyType, param.getAnnotations());
            if (dependency == null) {
                throw new UnsatisfiedDependencyException("Desconocido (Parámetro constructor)", getRawType(dependencyType));
            }
            dependencies[i] = dependency;
        }
        return dependencies;
    }

    // [MODIFICADO] Recibe Type, no Class<?>
    private Object resolveDependency(Type type, Annotation[] annotations) {
        // Necesitamos la clase cruda para conversiones simples y checks de @Value
        Class<?> rawType = getRawType(type);

        // 1. Revisar @Value
        Optional<String> valueAnnotation = Arrays.stream(annotations)
                .filter(a -> a.annotationType() == Value.class)
                .map(a -> ((Value) a).value()).findFirst();

        if (valueAnnotation.isPresent()) {
            String valueExpression = valueAnnotation.get();
            Environment env = getBean(Environment.class);
            String resolvedValue = env.resolvePlaceholders(valueExpression);
            return conversionService.convert(resolvedValue, rawType);
        }

        // 2. [MODIFICADO] Revisar @Qualifier (Directo o Meta-anotación)
        String qualifierName = extractQualifierName(annotations);

        if (qualifierName != null) {
            // Si encontramos un nombre (sea por @Qualifier o @Paypal), buscamos ese bean.
            if (containsBeanDefinition(qualifierName) || super.getSingleton(qualifierName) != null) {
                return getBean(qualifierName);
            }
            // Si es requerido y no existe, fallará más adelante o retornará null aquí
            // dependiendo de tu lógica de 'required'.
            return null;
        }

        // 3. Por Tipo (Genérico o Class simple)
        String beanName = resolveBeanNameByType(type, annotations);

        if (beanName != null) {
            return getBean(beanName);
        }

        return null;
    }

    /**
     * Busca un nombre de Qualifier en las anotaciones.
     * Soporta:
     * 1. @Qualifier("nombre") directo.
     * 2. Meta-anotaciones (ej: @Paypal que tiene @Qualifier("paypal") adentro).
     */
    private String extractQualifierName(Annotation[] annotations) {
        for (Annotation ann : annotations) {

            // CASO 1: @Qualifier directo en el campo
            if (ann.annotationType() == Qualifier.class) {
                String value = ((Qualifier) ann).value();
                return value.isEmpty() ? null : value; // Si está vacío directo, es un error de uso o se ignora
            }

            // CASO 2: Meta-Anotación (Tu @HomePage)
            // Ignoramos anotaciones del JDK
            if (ann.annotationType().getName().startsWith("java.lang")) {
                continue;
            }

            Qualifier metaQualifier = ann.annotationType().getAnnotation(Qualifier.class);

            if (metaQualifier != null) {
                // A. ¿La meta-anotación definió un nombre explícito?
                // Ej: @Qualifier("customName") public @interface HomePage
                String explicitValue = metaQualifier.value();

                if (!explicitValue.isEmpty()) {
                    return explicitValue;
                }

                // B. [MAGIA] Si está vacío, usamos el nombre de la anotación
                // Ej: @Qualifier public @interface HomePage -> Buscamos "homePage"
                String annotationName = ann.annotationType().getSimpleName();

                // Convertimos "HomePage" a "homePage" (camelCase)
                return Character.toLowerCase(annotationName.charAt(0)) + annotationName.substring(1);
            }
        }
        return null;
    }

    /**
     * Método central que busca un nombre de bean basado en su tipo.
     * Maneja la búsqueda, la unicidad y el desempate por nombre.
     *
     * @return El nombre del bean o null si no se encuentra.
     * @throws NoUniqueBeanDefinitionException si hay ambigüedad no resuelta.
     */
    private String resolveBeanNameByType(Type requiredType, Annotation[] annotations) {
        List<String> candidateNames = findCandidateBeanNames(requiredType, annotations);
        int totalCandidates = candidateNames.size();

        if (totalCandidates == 1) {
            return candidateNames.get(0);
        }

        if (totalCandidates > 1) {
            throw new NoUniqueBeanDefinitionException(getRawType(requiredType), totalCandidates);
        }

        return null;
    }

    /**
     * Estrategia alternativa (no usada):
     * Intenta resolver ambigüedad por el nombre por defecto del tipo.
     */
    @SuppressWarnings("unused")
    private String resolveByDefaultName(Class<?> requiredType, Set<String> candidateNames) {
        String defaultName = Character.toLowerCase(requiredType.getSimpleName().charAt(0)) +
                requiredType.getSimpleName().substring(1);

        return candidateNames.contains(defaultName) ? defaultName : null;
    }

    // =============================================================================================
    // MÉTODOS HELPERS (Reflexión y Utilidades)
    // =============================================================================================
    /**
     * Busca los nombres de beans que coincidan con el tipo Y las anotaciones requeridas.
     */
    private List<String> findCandidateBeanNames(Type requiredType, Annotation[] requiredAnnotations) {
        Set<String> candidateNames = new HashSet<>();
        Class<?> rawRequiredType = getRawType(requiredType);

        // =========================================================================
        // A. CACHÉ DE SINGLETONS (Priorizamos la Realidad del Objeto)
        // =========================================================================
        super.singletonObjects.forEach((name, beanInstance) -> {
            // 1. ¿El objeto vivo es del tipo que buscamos?
            if (rawRequiredType.isInstance(beanInstance)) {

                BeanDefinition bd = beanDefinitionMap.get(name);
                boolean isCandidate = true;

                // 2. Si es un Genérico complejo (List<String>), validamos extra con la definición
                // (Porque isInstance no puede ver la diferencia entre List<String> y List<Integer>)
                if (requiredType instanceof ParameterizedType) {
                    if (bd != null && !checkTypeMatch(requiredType, bd)) {
                        isCandidate = false;
                    }
                }

                // 3. Si pasó el tipo, validamos Qualifiers (@HomePage, etc.)
                if (isCandidate) {
                    // Si no tiene definición (bean manual), pasa si no piden qualifiers
                    if (bd == null) {
                        if (requiredAnnotations.length == 0) candidateNames.add(name);
                    }
                    // Si tiene definición, solo revisamos Qualifiers (saltándonos el check de tipo porque ya pasó isInstance)
                    else if (checkQualifierMatch(requiredAnnotations, bd)) {
                        candidateNames.add(name);
                    }
                }
            }
        });

        // =========================================================================
        // B. CACHÉ TEMPRANA (Early Singletons) - Misma lógica
        // =========================================================================
        super.earlySingletonObjects.forEach((name, beanInstance) -> {
            if (rawRequiredType.isInstance(beanInstance)) {
                BeanDefinition bd = beanDefinitionMap.get(name);
                if (bd != null) {
                    boolean genericOk = !(requiredType instanceof ParameterizedType) || checkTypeMatch(requiredType, bd);
                    if (genericOk && checkQualifierMatch(requiredAnnotations, bd)) {
                        candidateNames.add(name);
                    }
                }
            }
        });

        // =========================================================================
        // C. BEAN DEFINITIONS (Beans no creados - Lógica Estricta)
        // =========================================================================
        beanDefinitionMap.forEach((name, bd) -> {
            boolean alreadyFound = super.singletonObjects.containsKey(name) ||
                    super.earlySingletonObjects.containsKey(name);

            if (!alreadyFound) {
                // Aquí NO hay objeto, así que dependemos 100% de la definición.
                if (isAutowireCandidate(requiredType, bd, requiredAnnotations)) {
                    candidateNames.add(name);
                }
            }
        });

        return new ArrayList<>(candidateNames);
    }

    /**
     * Validador Maestro: Verifica si un Bean cumple con TODOS los requisitos (Tipo y Qualifiers).
     */
    private boolean isAutowireCandidate(Type requiredType, BeanDefinition bd, Annotation[] requiredAnnotations) {

        // 1. Verificar Tipo (Generics & Hierarchy)
        if (!checkTypeMatch(requiredType, bd)) {
            return false;
        }

        // 2. Verificar Qualifiers (Annotations & Attributes)
        if (!checkQualifierMatch(requiredAnnotations, bd)) {
            return false;
        }

        // ¡Es un candidato válido!
        return true;
    }

    private boolean checkTypeMatch(Type requiredType, BeanDefinition bd) {
        Type beanType = bd.getResolvableType(); // El tipo que el bean dice ser

        // CASO A: Genérico vs Genérico (List<String> vs List<Integer>)
        if (requiredType instanceof ParameterizedType reqPT && beanType instanceof ParameterizedType beanPT) {
            Class<?> reqRaw = getRawType(reqPT);
            Class<?> beanRaw = getRawType(beanPT);

            if (!reqRaw.isAssignableFrom(beanRaw)) return false;
            // Comparación estricta de los tipos internos <T>
            return Arrays.equals(reqPT.getActualTypeArguments(), beanPT.getActualTypeArguments());
        }

        // CASO B: Genérico vs Clase (Repository<User> vs UserRepository)
        if (requiredType instanceof ParameterizedType reqPT && beanType instanceof Class<?> beanClass) {
            return checkGenericTypeMatch(reqPT, beanClass);
        }

        // CASO C: Tipos Crudos (Service vs ServiceImpl)
        Class<?> reqRaw = getRawType(requiredType);
        Class<?> beanRaw = getRawType(beanType);

        return reqRaw.isAssignableFrom(beanRaw);
    }

    private boolean checkQualifierMatch(Annotation[] requiredAnnotations, BeanDefinition bd) {
        if (requiredAnnotations == null || requiredAnnotations.length == 0) {
            return true;
        }

        for (Annotation reqAnn : requiredAnnotations) {
            if (isQualifier(reqAnn.annotationType())) {

                // -----------------------------------------------------------
                // 1. PLAN A: MATCH POR NOMBRE (La regla que te faltaba)
                // -----------------------------------------------------------
                // Obtenemos el nombre que exige la anotación (ej: @HomePage -> "homePage")
                String requiredName = extractQualifierNameFromSingleAnnotation(reqAnn);

                // Si el nombre del Qualifier coincide con el nombre del Bean... ¡APROBADO!
                // Esto permite que @HomePage inyecte el bean @Bean("homePage")
                if (requiredName != null && requiredName.equals(bd.getBeanName())) {
                    continue; // Pasamos a la siguiente validación
                }

                // -----------------------------------------------------------
                // 2. PLAN B: MATCH POR ANOTACIÓN/ATRIBUTOS (Estricto)
                // -----------------------------------------------------------
                // Si el nombre no coincidió, buscamos si tiene la etiqueta física
                // y si sus atributos coinciden (ej. genre="Horror")
                Annotation beanAnn = findAnnotationOnBean(bd, reqAnn.annotationType());

                if (beanAnn == null || !areAnnotationAttributesEqual(reqAnn, beanAnn)) {
                    return false; // Ni nombre ni atributos coinciden -> FALLO
                }
            }
        }
        return true;
    }

    /**
     * Verifica si una clase (beanClass) implementa una interfaz genérica específica (requiredType)
     * con los argumentos correctos.
     * Ej: ¿UserRepository implementa Repository<Long, User>?
     */
    /** El "Sabueso": Recorre la herencia buscando el genérico correcto */
    private boolean checkGenericTypeMatch(ParameterizedType requiredType, Class<?> beanClass) {
        Class<?> requiredRaw = getRawType(requiredType);

        if (!requiredRaw.isAssignableFrom(beanClass)) return false;

        // 1. Buscar en Interfaces
        for (Type genericInterface : beanClass.getGenericInterfaces()) {
            if (isTypeMatch(requiredType, genericInterface)) return true;
            // (Opcional) Recursividad en interfaces padres si fuera necesario
        }

        // 2. Buscar en Superclase
        Type genericSuper = beanClass.getGenericSuperclass();
        if (genericSuper != null) {
            if (isTypeMatch(requiredType, genericSuper)) return true;
            if (genericSuper instanceof ParameterizedType pt) {
                return checkGenericTypeMatch(requiredType, getRawType(pt));
            } else if (genericSuper instanceof Class<?> cls) {
                return checkGenericTypeMatch(requiredType, cls);
            }
        }
        return false;
    }

    private String extractQualifierNameFromSingleAnnotation(Annotation ann) {
        // A. Es @Qualifier directo
        if (ann.annotationType() == Qualifier.class) {
            String value = ((Qualifier) ann).value();
            return value.isEmpty() ? null : value;
        }

        // B. Es Meta-Anotación (ej. @HomePage)
        Qualifier metaQualifier = ann.annotationType().getAnnotation(Qualifier.class);
        if (metaQualifier != null) {
            String explicitValue = metaQualifier.value();
            if (!explicitValue.isEmpty()) {
                return explicitValue;
            }
            // Convención: @HomePage -> "homePage"
            String simpleName = ann.annotationType().getSimpleName();
            return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
        }
        return null;
    }

    /** Compara dos ParameterizedTypes */
    private boolean isTypeMatch(ParameterizedType required, Type candidate) {
        if (candidate instanceof ParameterizedType candidatePT) {
            if (!getRawType(required).equals(getRawType(candidatePT))) return false;
            return Arrays.equals(required.getActualTypeArguments(), candidatePT.getActualTypeArguments());
        }
        return false;
    }

    /** 3. Busca una anotación específica en el Bean (sea Clase o Método) */
    private Annotation findAnnotationOnBean(BeanDefinition bd, Class<? extends Annotation> annotationType) {
        // A. Si es un @Component (Clase)
        if (bd.getBeanClass() != null && !bd.isFromFactoryMethod()) {
            // Buscamos en la clase y sus padres/interfaces si es heredada
            return bd.getBeanClass().getAnnotation(annotationType);
        }

        // B. Si es un @Bean (Método Factory)
        if (bd.isFromFactoryMethod()) {
            Method method = getFactoryMethodFromBD(bd);
            if (method != null) {
                return method.getAnnotation(annotationType);
            }
        }
        return null;
    }

    /** 4. Compara dos anotaciones atributo por atributo (Deep Equals) */
    private boolean areAnnotationAttributesEqual(Annotation required, Annotation candidate) {
        if (!required.annotationType().equals(candidate.annotationType())) {
            return false;
        }
        // Recorremos métodos: value(), genre(), format(), etc.
        for (Method method : required.annotationType().getDeclaredMethods()) {
            try {
                Object reqValue = method.invoke(required);
                Object candValue = method.invoke(candidate);
                if (!Objects.equals(reqValue, candValue)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /** 5. Detecta si una anotación es un Qualifier */
    private boolean isQualifier(Class<? extends Annotation> annotationType) {
        return annotationType.isAnnotationPresent(Qualifier.class) || annotationType.equals(Qualifier.class);
    }

    /** 2. Recupera el método @Bean original desde la definición */
    private Method getFactoryMethodFromBD(BeanDefinition bd) {
        if (!bd.isFromFactoryMethod()) return null;

        try {
            Object factoryBean = getBean(bd.getFactoryBeanName());
            // findFactoryMethod ya lo tienes implementado (maneja proxies)
            return findFactoryMethod(factoryBean, bd.getFactoryMethodName());
        } catch (Exception e) {
            log.warn("No se pudo inspeccionar el método factory para el bean: " + bd.getBeanName());
            return null;
        }
    }

    /**
     * Extrae la clase base de un Type (para manejar ParameterizedType).
     */
    private Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType pt) {
            return (Class<?>) pt.getRawType();
        }
        throw new IllegalArgumentException("Tipo no soportado: " + type);
    }

    private void performSetterInjection(Object instance) throws IllegalAccessException, InvocationTargetException {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Autowired.class) && method.getParameterCount() == 1) {

                // 1. Obtenemos la anotación para ver si es requerido
                Autowired autowired = method.getAnnotation(Autowired.class);
                boolean isRequired = autowired.required();

                Parameter parameter = method.getParameters()[0];

                try {
                    // 2. Intentamos resolver la dependencia
                    Object dependency = resolveDependency(parameter.getType(), parameter.getAnnotations());

                    // 3. Si la encontramos, inyectamos
                    if (dependency != null) {
                        method.invoke(instance, dependency);
                    } else if (isRequired) {
                        // 4. Si no se encontró (por tipo) y es requerido -> Error
                        throw new UnsatisfiedDependencyException(
                                "Dependencia requerida no encontrada para el setter: " + method.getName(),
                                parameter.getType());
                    }

                } catch (NoSuchBeanDefinitionException e) {
                    // 5. CAPTURAMOS LA EXCEPCIÓN DEL QUALIFIER
                    // Si resolveDependency falló buscando un @Qualifier específico:
                    if (isRequired) {
                        throw e; // Si es obligatorio, dejamos que explote.
                    }
                    // Si es opcional (required=false), simplemente ignoramos el error y no inyectamos nada.
                    log.debug("Dependencia opcional no encontrada: {}", e.getMessage());
                }
            }
        }
    }

    @SuppressWarnings("java:S3011")
    private void performFieldInjection(Object instance) throws IllegalAccessException {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Value.class)) {
                // Lógica de Value se mantiene igual
                Value valueAnn = field.getAnnotation(Value.class);
                Environment env = getBean(Environment.class);
                String resolvedValue = env.resolvePlaceholders(valueAnn.value());
                Object convertedValue = conversionService.convert(resolvedValue, field.getType());

                field.setAccessible(true);
                field.set(instance, convertedValue);
            } else if (field.isAnnotationPresent(Autowired.class)) {
                // [NUEVO] Soporte de Autowired en campos con genéricos
                field.setAccessible(true);
                Autowired autowired = field.getAnnotation(Autowired.class);

                try {
                    // [MODIFICADO] Usamos getGenericType
                    Object dependency = resolveDependency(field.getGenericType(), field.getAnnotations());

                    if (dependency != null) {
                        field.set(instance, dependency);
                    } else if (autowired.required()) {
                        throw new UnsatisfiedDependencyException("Campo requerido no encontrado: " + field.getName(), field.getType());
                    }
                } catch (NoSuchBeanDefinitionException e) {
                    if (autowired.required()) throw e;
                }
            }
        }
    }

    @SuppressWarnings("java:S3011")
    private void invokePostConstruct(Object instance) throws InaccessibleObjectException, IllegalAccessException, InvocationTargetException {
        Class<?> currentClass = instance.getClass();

        while (currentClass != null && currentClass != Object.class) {
            // Detiene la búsqueda si entramos en los paquetes de Swing o Java.
            String packageName = currentClass.getPackageName();
            boolean excluded = EXCLUDED_PACKAGE_PREFIXES.stream()
                    .anyMatch(packageName::startsWith);

            if (excluded) {
                break;
            }

            for (Method method : currentClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(PostConstruct.class)) {
                    if (method.getParameterCount() != 0) {
                        throw new BeanInitializationException("El método @PostConstruct '" + method.getName() + "' en la clase '" + currentClass.getName() + "' no debe tener parámetros.");
                    }
                    method.setAccessible(true);
                    method.invoke(instance);
                    return; // Ejecuta solo el primer método @PostConstruct que encuentre.
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    /**
     * Busca el constructor a utilizar para la inyección de dependencias.
     * Prioriza un único constructor anotado con @Inject. Si no existe,
     * busca el constructor por defecto. Lanza una excepción si hay ambigüedad
     * (múltiples constructores @Inject).
     */
    private Constructor<?> findConstructorToUse(Class<?> componentClass) {
        List<Constructor<?>> injectableConstructors = new ArrayList<>();
        for (Constructor<?> constructor : componentClass.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                injectableConstructors.add(constructor);
            }
        }

        int totalInjectableConstructors = injectableConstructors.size();
        if (totalInjectableConstructors > 1) {
            // Error: Ambigüedad, se encontraron múltiples constructores con @Inject
            throw new BeanInstantiationException(componentClass,
                    "Ambigüedad: Se encontraron " + injectableConstructors.size()
                            + " constructores anotados con @Autowired. Solo uno es permitido.");
        } else if (totalInjectableConstructors == 1) {
            // Caso ideal: Se encontró un único constructor @Inject
            return injectableConstructors.getFirst();
        } else {
            // Caso por defecto: No hay @Inject, busca el constructor sin argumentos
            try {
                return componentClass.getDeclaredConstructor();
            } catch (NoSuchMethodException _) {
                throw new BeanInstantiationException(componentClass, "No se encontró un constructor apropiado: No hay constructor por defecto ni uno anotado con @Autowired.");
            }
        }
    }

    // [MODIFICADO] Ahora es capaz de encontrar el método original aunque factoryBean sea un Proxy
    private Method findFactoryMethod(Object factoryBean, String methodName) throws BeanInstantiationException {
        Class<?> targetClass = factoryBean.getClass();

        // [NUEVO] Si es un Proxy (ByteBuddy/CGLIB), subimos a la clase padre.
        // Esto es CRUCIAL: El proxy no tiene las anotaciones @Qualifier en los parámetros,
        // pero la clase padre (tu código) SÍ.
        if (isProxyClass(targetClass)) {
            targetClass = targetClass.getSuperclass();
        }

        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new BeanInstantiationException(factoryBean.getClass(), "No se encontró el método factory '" + methodName + "'");
    }

    // [NUEVO] Helper para detectar proxies generados
    private boolean isProxyClass(Class<?> clazz) {
        String name = clazz.getName();
        return name.contains("$ByteBuddy") || name.contains("$$") || name.contains("CGLIB");
    }
}
