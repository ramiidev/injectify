package com.ramiidev.injectify.boot.exception;

import com.ramiidev.injectify.shared.exception.InjectifyException;

/**
 * Excepción lanzada cuando una fase de inicialización (BootstrapPhase)
 * falla críticamente y no puede completar su configuración.
 */
public class BootstrapPhaseException extends InjectifyException {

    private final String phaseName;

    public BootstrapPhaseException(String phaseName, String message, Throwable cause) {
        super(String.format("Fallo en la fase [%s]: %s", phaseName, message), cause);
        this.phaseName = phaseName;
    }

    public String getPhaseName() {
        return phaseName;
    }
}
