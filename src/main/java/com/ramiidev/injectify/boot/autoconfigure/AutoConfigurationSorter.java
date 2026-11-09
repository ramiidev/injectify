package com.ramiidev.injectify.boot.autoconfigure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Comparador para ordenar clases de configuración basado en @AutoConfigureOrder.
 */
public class AutoConfigurationSorter {

    /**
     * Ordena las clases respetando @AutoConfigureOrder, @AutoConfigureAfter y @AutoConfigureBefore.
     */
    public List<Class<?>> getInPriorityOrder(Collection<Class<?>> classes) {
        // 1. Convertir a lista y hacer un ordenamiento numérico inicial
        // Esto sirve como base para cuando no hay relaciones Before/After.
        List<Class<?>> sortedClasses = new ArrayList<>(classes);
        sortedClasses.sort(Comparator.comparingInt(this::getOrder));

        // 2. Aplicar ordenamiento topológico (Before/After)
        return sortByAnnotation(sortedClasses);
    }

    private int getOrder(Class<?> clazz) {
        if (clazz.isAnnotationPresent(AutoConfigureOrder.class)) {
            return clazz.getAnnotation(AutoConfigureOrder.class).value();
        }
        return AutoConfigureOrder.DEFAULT_ORDER;
    }

    // --- Lógica del Grafo ---

    private List<Class<?>> sortByAnnotation(List<Class<?>> classes) {
        Set<Class<?>> visited = new HashSet<>();
        List<Class<?>> result = new ArrayList<>();

        // Mapeo rápido para verificar existencia
        Set<Class<?>> availableClasses = new HashSet<>(classes);

        for (Class<?> clazz : classes) {
            processNode(clazz, availableClasses, visited, result);
        }

        return result;
    }

    /**
     * Algoritmo recursivo (DFS) para resolver dependencias.
     */
    private void processNode(Class<?> current, Set<Class<?>> availableClasses,
                             Set<Class<?>> visited, List<Class<?>> result) {

        if (visited.contains(current)) {
            return; // Ya procesado
        }

        // 1. Detectar dependencias "After" (Yo voy después de X -> Procesar X primero)
        if (current.isAnnotationPresent(AutoConfigureAfter.class)) {
            for (Class<?> dependency : current.getAnnotation(AutoConfigureAfter.class).value()) {
                if (availableClasses.contains(dependency)) {
                    processNode(dependency, availableClasses, visited, result);
                }
            }
        }

        // 2. Detectar dependencias inversas "Before" (X va antes de mi -> es lo mismo que After)
        // Esto es más complejo: Tengo que buscar quiénes dijeron "@AutoConfigureBefore(Yo)"
        // y procesarlos a ellos primero.
        for (Class<?> candidate : availableClasses) {
            if (candidate.isAnnotationPresent(AutoConfigureBefore.class)) {
                Class<?>[] targets = candidate.getAnnotation(AutoConfigureBefore.class).value();
                for (Class<?> target : targets) {
                    if (target.equals(current)) {
                        // El candidato dijo "Voy antes que Current", así que procesamos candidato
                        processNode(candidate, availableClasses, visited, result);
                    }
                }
            }
        }

        // 3. Registrarse a sí mismo y marcar como visitado
        visited.add(current);
        result.add(current);
    }
}