package com.ramiidev.injectify.boot.autoconfigure.core;

import com.ramiidev.injectify.configuration.annotation.ConfigurationProperties;
import com.ramiidev.injectify.configuration.annotation.PropertyValue;
import com.ramiidev.injectify.core.scanner.ScanMode;

@ConfigurationProperties(value = "scan")
public class ScanProperties {

    @PropertyValue(key = "mode")
    private ScanMode mode = ScanMode.NONE;

    @PropertyValue(key = "file-path")
    private String filePath = "system/bin/injectify-scan.bin";

    public ScanMode getMode() {
        return mode;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "ScanProperties{" +
                "mode=" + mode +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
