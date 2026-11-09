package com.ramiidev.injectify.core.diagnostic;

public abstract class AbstractFailureAnalyzer<T extends Throwable> implements FailureAnalyzer {

    @Override
    public FailureAnalysis analyze(Throwable failure) {
        // 1. Extraemos el tipo de excepción que esta clase hija quiere manejar
        Class<T> type = getSupportedType();

        // 2. Buscamos si ESA excepción existe en algún nivel del stacktrace
        T cause = findCause(failure, type);

        // 3. Si la encontramos, le pasamos el error limpio a la clase hija
        if (cause != null) {
            return analyze(failure, cause);
        }

        // 4. Si no está en el stacktrace, retornamos null (este analizador pasa el turno)
        return null;
    }

    /**
     * El método que las subclases implementarán con su lógica específica.
     * Ya reciben la causa exacta que estaban buscando.
     */
    protected abstract FailureAnalysis analyze(Throwable rootFailure, T cause);

    /**
     * Define qué tipo de excepción maneja este analizador.
     */
    protected abstract Class<T> getSupportedType();

    // La "Plomería": El bucle que nadie quiere escribir dos veces
    @SuppressWarnings("unchecked")
    private T findCause(Throwable failure, Class<T> type) {
        Throwable current = failure;
        while (current != null) {
            if (type.isInstance(current)) {
                return (T) current; // ¡Encontrado!
            }
            current = current.getCause(); // Bajamos un nivel en la "muñeca rusa"
        }
        return null; // No se encontró en toda la cadena
    }
}
