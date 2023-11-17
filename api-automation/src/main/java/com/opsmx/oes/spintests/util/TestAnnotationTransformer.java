package com.opsmx.oes.spintests.util;

import com.opsmx.oes.common.util.ConfigDataProvider;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class TestAnnotationTransformer implements IAnnotationTransformer {

    private final ConfigDataProvider config = new ConfigDataProvider(System.getProperty("user.dir")
            + "/../config.properties", System.getProperty("user.dir")
            + "/../config-overide.properties");

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor,
                          Method testMethod) {
        String methodName = testMethod.getName();

        // Load the configuration value from config.properties file
        boolean isTestCaseEnabled = loadTestCaseEnabledStatus(methodName);

        // Set the enabled attribute based on the configuration value
        annotation.setEnabled(isTestCaseEnabled);
    }

    private boolean loadTestCaseEnabledStatus(final String methodName) {
        return "enabled".equals(config.getConfigData(methodName));
    }
}

