package com.opsmx.oes.common.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigDataProvider {

    Properties properties;

    public ConfigDataProvider(String filePath, String overrideFilePath) {
        properties = new Properties();
        loadPropertiesFromFile(filePath);
        loadPropertiesFromFile(overrideFilePath);
    }

    public <T> T getConfigData(String data) {
        return (T) properties.getProperty(data);
    }

    public void loadPropertiesFromFile(String filePath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            properties.load(fileInputStream);
        } catch (IOException e) {
            System.out.println("Exception is: " + e.getMessage());
        }
    }
}