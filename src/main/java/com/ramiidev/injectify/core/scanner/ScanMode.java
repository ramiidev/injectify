package com.ramiidev.injectify.core.scanner;

public enum ScanMode {
    NONE,       // Comportamiento normal (Escanea siempre)
    GENERATE,   // Escanea y guarda el resultado en un archivo .bin
    ACTIVATE    // NO escanea, carga del archivo .bin (Falla si no existe)
}
