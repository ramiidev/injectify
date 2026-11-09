package com.ramiidev.injectify.core.context.support;

import com.ramiidev.injectify.configuration.binder.support.ConfigurationPropertyBinder;
import com.ramiidev.injectify.configuration.env.Environment;
import com.ramiidev.injectify.core.beans.definition.BeanDefinition;
import com.ramiidev.injectify.core.beans.support.DefaultBeanFactory;
import com.ramiidev.injectify.core.context.ApplicationContext;
import com.ramiidev.injectify.core.convert.support.ConversionService;
import com.ramiidev.injectify.shared.annotation.Bean;
import com.ramiidev.injectify.shared.annotation.ConditionalOnClass;
import com.ramiidev.injectify.shared.annotation.ConditionalOnMissingBean;
import com.ramiidev.injectify.shared.annotation.ConditionalOnProperty;
import com.ramiidev.injectify.shared.annotation.Lazy;
import com.ramiidev.injectify.shared.annotation.Scope;
import com.ramiidev.injectify.shared.annotation.stereotype.Configuration;
import com.ramiidev.injectify.shared.utils.AnnotationUtils;

import javax.annotation.CheckForNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;

/**
 * Clase base para implementaciones de ApplicationContext.
 * Implementa la lógica común del ciclo de vida de los beans usando BeanDefinitions.
 */
public abstract class AbstractApplicationContext implements ApplicationContext {

    protected final ConfigurationPropertyBinder propertyBinder;
    protected final DefaultBeanFactory beanFactory;
    protected final Environment environment;

    protected AbstractApplicationContext(Environment enviroment, ConversionService conversionService) {
        this.environment = enviroment;
        this.propertyBinder = new ConfigurationPropertyBinder(conversionService);
        this.beanFactory = new DefaultBeanFactory(propertyBinder, conversionService);
    }

    @Override
    public Object getBean(String name) {
        return beanFactory.getBean(name);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        return beanFactory.getBean(requiredType);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        return beanFactory.getBean(name, requiredType);
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanFactory.getBeanDefinition(beanName);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanFactory.containsBeanDefinition(beanName);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return beanFactory.getBeanDefinitionNames();
    }

    @Override
    public int getBeanDefinitionCount() {
        return beanFactory.getBeanDefinitionCount();
    }

    @Override
    public void destroy() {
        beanFactory.destroy();
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        return beanFactory.getBeanNamesForType(type);
    }

    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        beanFactory.registerSingleton(beanName, singletonObject);
    }

    @Override
    public void preInstantiateSingletons() {
        beanFactory.preInstantiateSingletons();
    }

    /**
     * Escanea clases y crea una BeanDefinition para cada componente.
     */
    protected void prepareBeanDefinitions(Set<Class<?>> componentClasses) {
        for (Class<?> componentClass : componentClasses) {
            if (componentClass.isAnnotation()) continue;

            // 1. [NUEVO] VALIDACIÓN DE CONDICIONALES A NIVEL DE CLASE
            // Verificamos si la CLASE entera debe saltarse (ej. @ConditionalOnMissingBean en la clase)
            // Pasamos 'componentClass' como elemento anotado y como tipo.
            if (shouldSkipBeanRegistration(componentClass, componentClass)) {
                // Si la condición falla, saltamos la clase Y sus métodos @Bean internos
                continue;
            }

            String beanName = getBeanNameForClass(componentClass);

            // Lógica para componentes de clase normal
            if (!containsBeanDefinition(beanName)) {
                // [NOTA] Para clases directas, el Type y el Class suelen ser lo mismo,
                // salvo que hereden de un genérico, pero aquí registramos la clase en sí.
                BeanDefinition definition = new BeanDefinition(beanName, componentClass);

                Scope scopeAnnotation = findScopeAnnotation(componentClass);
                if (scopeAnnotation != null) {
                    definition.setScope(scopeAnnotation.value());
                }

                Boolean lazyValue = findLazyConfiguration(componentClass);
                // Se encontró @Lazy (directo o en meta-anotación)
                // Comportamiento por defecto (Eager)
                definition.setLazyInit(Objects.requireNonNullElse(lazyValue, false));

                registerBeanDefinition(beanName, definition);
            }

            // Procesar métodos @Bean dentro de clases @Configuration
            if (componentClass.isAnnotationPresent(Configuration.class)) {
                registerConfigurationMethods(componentClass, beanName);
            }
        }
    }

    /**
     * Busca la configuración @Lazy con prioridad:
     * 1. Anotación directa en el elemento (@Lazy en la clase/método).
     * 2. Meta-anotación (@Lazy dentro de @Repository, @Service, etc).
     * * @return true/false si encuentra configuración, o null si no hay nada definido.
     */
    @CheckForNull
    private Boolean findLazyConfiguration(AnnotatedElement element) {
        // 1. PRIORIDAD MÁXIMA: Anotación Directa
        // Si el usuario escribió @Lazy(false) explícitamente, esto debe ganar
        // sobre lo que diga @Repository.
        if (element.isAnnotationPresent(Lazy.class)) {
            return element.getAnnotation(Lazy.class).value();
        }

        // 2. PRIORIDAD SECUNDARIA: Meta-Anotaciones
        // Buscamos dentro de las anotaciones que tiene el elemento (ej. dentro de @Repository)
        for (Annotation annotation : element.getAnnotations()) {
            // Saltamos anotaciones de Java para no perder tiempo
            if (annotation.annotationType().getName().startsWith("java.lang")) continue;
            if (annotation.annotationType().isAnnotationPresent(Lazy.class)) {
                return annotation.annotationType().getAnnotation(Lazy.class).value();
            }
        }

        // 3. No se encontró nada
        return null;
    }

    private void registerConfigurationMethods(Class<?> componentClass, String configBeanName) {
        for (Method method : componentClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Bean.class)) {

                // Validamos condicionales (@ConditionalOnMissingBean, etc.)
                if (shouldSkipBeanRegistration(method, method.getReturnType())) {
                    continue;
                }

                String factoryMethodBeanName = getBeanNameForMethod(method);

                // [CLAVE] CAPTURA DE GENÉRICOS EN MÉTODOS FACTORY
                // Aquí es donde distinguimos WindowStageManager<Main> de WindowStageManager<Login>
                // getGenericReturnType() nos devuelve el tipo completo con sus parámetros <T>.
                Type genericReturnType = method.getGenericReturnType();
                Class<?> rawReturnType = method.getReturnType();

                // Usamos el nuevo constructor de BeanDefinitionF
                BeanDefinition methodDef = new BeanDefinition(
                        factoryMethodBeanName,
                        rawReturnType,     // Clase cruda para instanciar (si fuera necesario)
                        genericReturnType  // Tipo completo para la resolución de inyección
                );

                methodDef.setFactoryBeanName(configBeanName);
                methodDef.setFactoryMethodName(method.getName());

                Scope scopeAnnotation = findScopeAnnotation(method);
                if (scopeAnnotation != null) {
                    methodDef.setScope(scopeAnnotation.value());
                }

                // 1. Buscamos @Lazy en el método (Prioridad Alta)
                Boolean methodLazy = findLazyConfiguration(method);
                if (methodLazy != null) {
                    // EL MÉTODO TIENE OPINIÓN (sea true o false).
                    // Obedecemos al método ciegamente.
                    methodDef.setLazyInit(methodLazy);
                }
                else {
                    // EL MÉTODO DIO NULL (No tiene opinión).
                    // Solo ahora preguntamos a la clase.
                    Boolean classLazy = findLazyConfiguration(componentClass);
                    methodDef.setLazyInit(Objects.requireNonNullElse(classLazy, false));
                }

                registerBeanDefinition(factoryMethodBeanName, methodDef);
            }
        }
    }

    /**
     * Evalúa si el registro del bean debe omitirse debido a @ConditionalOnMissingBean.
     *
     * @param element  El elemento anotado (Clase o Método).
     * @param beanType El tipo del bean que se produciría.
     * @return true si se debe omitir el registro (porque la condición falló), false si se debe proceder.
     */
    // 1. Actualiza este método maestro para incluir la nueva validación
    private boolean shouldSkipBeanRegistration(AnnotatedElement element, Class<?> beanType) {

        // A. Chequeo de @ConditionalOnMissingBean (Ya lo tenías)
        if (element.isAnnotationPresent(ConditionalOnMissingBean.class) &&
                shouldSkipOnMissingBean(element.getAnnotation(ConditionalOnMissingBean.class), beanType)) {
            return true;
        }


        // B. [NUEVO] Chequeo de @ConditionalOnClass
        if (element.isAnnotationPresent(ConditionalOnClass.class)
                && shouldSkipOnClass(element.getAnnotation(ConditionalOnClass.class))) {
            return true; // La clase requerida no existe -> SALTAR
        }

        // [NUEVO] Chequeo de Propiedades
        if (element.isAnnotationPresent(ConditionalOnProperty.class)
                && shouldSkipOnProperty(element.getAnnotation(ConditionalOnProperty.class))) {
                return true; // Saltar si la propiedad no coincide
            }


        return false;
    }

    // 2. Extrae la lógica vieja a un método privado (Limpieza de código)
    private boolean shouldSkipOnMissingBean(ConditionalOnMissingBean condition, Class<?> beanType) {

        // 1. Chequeo por NOMBRE
        for (String name : condition.name()) {
            if (containsBeanDefinition(name) || beanFactory.containsSingleton(name)) {
                return true;
            }
        }

        // 2. Chequeo por TIPO (clase)
        Class<?>[] types = (condition.value().length == 0 && condition.name().length == 0)
                ? new Class<?>[]{beanType} : condition.value();

        // Para cada tipo, verificamos si ya existe un bean definido
        // (esto puede ser optimizado con un índice interno si se usa mucho)
        for (Class<?> type : types) {
            if (containsBeanDefinitionByType(type)) {
                return true;
            }
        }
        return false;
    }

    // 2. La lógica de evaluación
    private boolean shouldSkipOnProperty(ConditionalOnProperty condition) {
        String key = condition.name();
        String expectedValue = condition.havingValue();
        boolean matchIfMissing = condition.matchIfMissing();

        // A. Verificamos si la propiedad existe en el Environment
        if (!environment.containsProperty(key)) {
            // Si NO existe, solo pasamos si matchIfMissing es true
            return !matchIfMissing;
        }

        // B. Si la propiedad existe, verificamos el valor
        // Si expectedValue está vacío, significa que el usuario solo quería saber si existía la key
        if (!expectedValue.isEmpty()) {
            String actualValue = environment.getProperty(key);
            // Si el valor real NO es igual al esperado -> SALTAR
            return !expectedValue.equals(actualValue);
        }

        // Si llegamos aquí: La propiedad existe y (el valor coincide O no importaba el valor)
        return false; // NO saltar (Cargar el bean)
    }

    // 3. [NUEVO] La lógica para verificar si existe la clase en el ClassPath
    private boolean shouldSkipOnClass(ConditionalOnClass condition) {
        // A. Verificar por Nombres (String) - Lo más común para librerías opcionales
        for (String className : condition.name()) {
            try {
                // Intentamos cargar la clase dinámicamente
                Class.forName(className, false, this.getClass().getClassLoader());
            } catch (ClassNotFoundException e) {
                // ¡AJÁ! La librería no está importada en el proyecto.
                return true; // Debemos saltar el registro
            }
        }

        // B. Verificar por Clase (value)
        // Esto es redundante si el código compila, pero útil por consistencia.
        // Si llegamos aquí, las clases en 'value' existen (si no, no compilaría).

        return false; //  Existe, NO saltar.
    }


    /**
     * Busca en todas las definiciones registradas si hay alguna que coincida con el tipo.
     */
    private boolean containsBeanDefinitionByType(Class<?> typeToCheck) {
        String[] definedNames = getBeanDefinitionNames();
        for (String name : definedNames) {
            BeanDefinition def = getBeanDefinition(name);
            // Verificamos si la clase del bean es asignable al tipo que buscamos
            if (typeToCheck.isAssignableFrom(def.getBeanClass())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Calcula el nombre para una clase escaneada (@Component, @Service, etc).
     * Prioridad:
     * 1. Valor explícito en @Component("nombre")
     * 2. Default: Nombre de la clase en camelCase
     */
    protected String getBeanNameForClass(Class<?> componentClass) {
        // 1. Usar nuestra nueva utilidad inteligente
        String customName = AnnotationUtils.findComponentBeanName(componentClass);

        if (customName != null && !customName.isBlank()) {
            return customName;
        }

        // 3. Generar nombre por defecto (camelCase)
        String name = componentClass.getSimpleName();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private String getBeanNameForMethod(Method method) {
        Bean beanAnnotation = method.getAnnotation(Bean.class);
        if (beanAnnotation != null && !beanAnnotation.value().isBlank()) {
            return beanAnnotation.value();
        }

        // 3. Default: Nombre del método
        return method.getName();
    }

    /**
     * Busca una anotación @Scope en una clase o método, incluyendo meta-anotaciones.
     * La anotación directa siempre tiene prioridad sobre la meta-anotación.
     */
    private Scope findScopeAnnotation(AnnotatedElement element) {
        // 1. Busca la anotación directamente en el elemento (clase o método)
        if (element.isAnnotationPresent(Scope.class)) {
            return element.getAnnotation(Scope.class);
        }

        // 2. Si no, busca en las anotaciones que el elemento tiene (meta-anotaciones)
        for (Annotation annotation : element.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Scope.class)) {
                return annotation.annotationType().getAnnotation(Scope.class);
            }
        }

        return null;
    }
}

