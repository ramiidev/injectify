package com.ramiidev.injectify.core.diagnostic;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class DialogFailureReporter implements FailureAnalysisReporter {

    @Override
    public void report(FailureAnalysis analysis) {
        Runnable showDialog = () -> {
            String message = analysis.description() + "\n\nAcción sugerida:\n" + analysis.action();
            JOptionPane.showMessageDialog(null, message, "Error Fatal en la Aplicación", JOptionPane.ERROR_MESSAGE);
        };

        if (SwingUtilities.isEventDispatchThread()) {
            showDialog.run();
        } else {
            try {
                // Usamos invokeAndWait porque la app está a punto de morir.
                // Queremos asegurar que el diálogo se vea ANTES del System.exit().
                SwingUtilities.invokeAndWait(showDialog);
            } catch (Exception e) {
                IO.println("[Fallback] No se pudo mostrar el diálogo UI. EDT bloqueado o inalcanzable.");
            }
        }
    }
}
