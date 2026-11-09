package com.ramiidev.injectify.infrastructure.logging;

import org.slf4j.Logger;

// Enum con patrón estrategia para delegar al método correcto de SLF4J
public enum LogLevel {

    TRACE {
        @Override
        public void log(Logger log, String msg, Throwable t) { // <--- Agregar public
            if (t == null) log.trace(msg);
            else log.trace(msg, t);
        }
    },
    DEBUG {
        @Override
        public void log(Logger log, String msg, Throwable t) { // <--- Agregar public
            if (t == null) log.debug(msg);
            else log.debug(msg, t);
        }
    },
    INFO {
        @Override
        public void log(Logger log, String msg, Throwable t) { // <--- Agregar public
            if (t == null) log.info(msg);
            else log.info(msg, t);
        }
    },
    WARN {
        @Override
        public void log(Logger log, String msg, Throwable t) { // <--- Agregar public
            if (t == null) log.warn(msg);
            else log.warn(msg, t);
        }
    },
    ERROR {
        @Override
        public void log(Logger log, String msg, Throwable t) { // <--- Agregar public
            if (t == null) log.error(msg);
            else log.error(msg, t);
        }
    };

    // La definición del método debe ser pública
    public abstract void log(Logger log, String msg, Throwable t);
}
