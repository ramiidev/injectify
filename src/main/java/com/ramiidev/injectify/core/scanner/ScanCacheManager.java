package com.ramiidev.injectify.core.scanner;

import com.ramiidev.injectify.core.exception.ScanCacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ScanCacheManager {

    private static final Logger logger = LoggerFactory.getLogger(ScanCacheManager.class);

    /**
     * Guarda los nombres de las clases en un archivo, creando directorios si es necesario.
     * Escribe en el SISTEMA DE ARCHIVOS (Raíz del proyecto).
     */
    public void saveCache(Set<Class<?>> classes, String filePath) {
        Set<String> classNames = classes.stream()
                .map(Class::getName)
                .collect(Collectors.toSet());

        // 1. Asegurar que los directorios existen (system/bin/, etc.)
        File targetFile = new File(filePath);
        File parentDir = targetFile.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                logger.warn("No se pudieron crear los directorios para: {}", parentDir.getAbsolutePath());
            }
        }

        // 2. Escribir el archivo
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(targetFile))) {
            oos.writeObject(classNames);
            logger.info("Caché generado exitosamente en DISCO: {}", targetFile.getAbsolutePath());
        } catch (IOException e) {
            throw new ScanCacheException("Error escribiendo caché en: " + filePath, e);
        }
    }

    /**
     * Carga las clases usando una estrategia híbrida:
     * 1. Intenta cargar desde CLASSPATH (Resources/JAR) -> Producción.
     * 2. Si falla, intenta cargar desde SISTEMA DE ARCHIVOS -> Desarrollo/Local.
     */
    @SuppressWarnings("unchecked")
    public Set<Class<?>> loadCache(String filePath) {
        logger.debug("Iniciando carga de caché para ruta: {}", filePath);

        Set<String> classNames = null;

        // ESTRATEGIA A: Intentar desde Classpath (Resources)
        // Esto funciona si moviste el archivo a src/main/resources o está dentro del JAR.
        try (InputStream cpIn = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath)) {
            if (cpIn != null) {
                logger.info("Caché encontrado en CLASSPATH (Resources).");
                try (ObjectInputStream ois = new ObjectInputStream(cpIn)) {
                    classNames = (Set<String>) ois.readObject();
                }
            }
        } catch (Exception e) {
            logger.warn("Error leyendo del classpath, intentando sistema de archivos...", e);
        }

        // ESTRATEGIA B: Intentar desde Sistema de Archivos (Raíz del proyecto)
        // Esto funciona si acabas de generar el archivo en 'system/bin/...' y no está en el JAR.
        if (classNames == null) {
            File file = new File(filePath);
            if (file.exists()) {
                logger.info("Caché encontrado en SISTEMA DE ARCHIVOS local: {}", file.getAbsolutePath());
                try (InputStream fsIn = new FileInputStream(file);
                     ObjectInputStream ois = new ObjectInputStream(fsIn)) {
                    classNames = (Set<String>) ois.readObject();
                } catch (Exception e) {
                    throw new ScanCacheException("Archivo encontrado en disco pero corrupto/ilegible: " + filePath, e);
                }
            }
        }

        // VALIDACIÓN FINAL
        if (classNames == null) {
            throw new ScanCacheException(
                    "Modo ACTIVATE falló. El archivo '" + filePath + "' no existe en Resources (JAR) ni en el Sistema de Archivos."
            );
        }

        // RECONSTRUCCIÓN DE CLASES
        Set<Class<?>> classes = new HashSet<>();
        try {
            for (String name : classNames) {
                classes.add(Class.forName(name));
            }
        } catch (ClassNotFoundException e) {
            throw new ScanCacheException("El caché contiene referencias a clases que ya no existen.", e);
        }

        return classes;
    }
}
