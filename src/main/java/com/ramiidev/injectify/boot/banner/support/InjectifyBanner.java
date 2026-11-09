package com.ramiidev.injectify.boot.banner.support;

import com.ramiidev.injectify.boot.banner.Banner;

import java.io.PrintStream;

/**
 * Implementación por defecto del Banner de Injectify.
 */
public class InjectifyBanner implements Banner {

    private static final String[] BANNER = {
            "   _____            _             _             ",
            "  / ____|          (_)           (_)            ",
            " | (___ __      __ _ _ __   __ _ _ _ __   ___   ",
            "  \\___ \\\\ \\ /\\ / /| | '_ \\ / _` | | '_ \\ / _ \\  ",
            "  ____) |\\ V  V / | | | | | (_| | | | | |  __/  ",
            " |_____/  \\_/\\_/  |_|_| |_|\\__, |_|_| |_|\\___|  ",
            "                            __/ |               ",
            "                           |___/                ",
            " :: Injectify Framework ::             (v1.0.0)  "
    };

    @Override
    public void printBanner(PrintStream out) {
        for (String line : BANNER) {
            out.println(line);
        }
        out.println();
    }
}
