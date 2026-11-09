package com.ramiidev.injectify.boot;

import javax.swing.JPanel;

public interface ApplicationLauncher {
    void configureNavigationService(JPanel container);
    void launch();
}
