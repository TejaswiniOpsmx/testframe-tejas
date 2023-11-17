package com.opsmx.oes.examples;

import com.opsmx.oes.common.util.ConfigDataProvider;
import com.opsmx.oes.pipelinebuilder.json.application.Application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationBuilder {

    public Application buildApplication(final String appName,
                                        final String appDescription, final String appEmail) {
        return Application.builder()
                .name(appName)
                .description(appDescription)
                .application(appName)
                .email(appEmail)
                .job(generateJobObjectToCreateApp(appName, appEmail, appDescription))
                .build();
    }

    public Application updateApplication(final String appName, final String updatedAppDescription) {
        return Application.builder()
                .name(appName)
                .application(appName)
                .description(updatedAppDescription)
                .job(generateJobObjectToUpdateApp(appName, updatedAppDescription))
                .build();
    }

    public Application deleteApplication(final String appName) {
        return Application.builder()
                .name(appName)
                .description("Deleting Application")
                .application(appName)
                .job(generateJobObjectToDeleteApp(appName))
                .build();
    }

    private List<Map<String, Object>> generateJobObjectToCreateApp(final String appName, final String appEmail,
                                                                   final String appDescription) {

        final ConfigDataProvider config = new ConfigDataProvider(System.getProperty("user.dir")
                + "/../config.properties", System.getProperty("user.dir")
                + "/../config-overide.properties");
        final String rbacStatus = config.getConfigData("rbacStatus");
        final String userName = config.getConfigData("username");

        List<Map<String, Object>> jobList = new ArrayList<>();
        Map<String, Object> jobEntry = new HashMap<>();
        jobEntry.put("type", "createApplication");

        Map<String, Object> application = new HashMap<>();
        application.put("cloudProviders", "kubernetes");
        application.put("instancePort", 80);
        application.put("name", appName);
        application.put("email", appEmail);
        application.put("description", appDescription);
        jobEntry.put("application", application);
        jobEntry.put("user", userName);
        jobList.add(jobEntry);

        if("enabled".equals(rbacStatus)) {
            final List<String> readGroup = fetchGroups(config.getConfigData("readGroup"));
            final List<String> writeGroup = fetchGroups(config.getConfigData("writeGroup"));
            final List<String> executeGroup = fetchGroups(config.getConfigData("executeGroup"));

            Map<String, List<String>> permissions = getStringListMap(readGroup, writeGroup, executeGroup);
            application.put("permissions", permissions);
        }

        return jobList;
    }

    private List<String> fetchGroups(final String groupsInput) {
        String[] groups = groupsInput.split(",");
        for (int i = 0; i < groups.length; i++) {
            groups[i] = groups[i].trim();
        }
        return List.of(groups);
    }

    private Map<String, List<String>> getStringListMap(final List<String> readGroup, final List<String> writeGroup,
                                                              final List<String> executeGroup) {
        Map<String, List<String>> permissions = new HashMap<>();

        List<String> readPermission = new ArrayList<>();
        List<String> writePermission = new ArrayList<>();
        List<String> executePermission = new ArrayList<>();

        for (int i = 0; i < readGroup.size(); i++) {
            readPermission.add(readGroup.get(i));
        }
        for (int i = 0; i < writeGroup.size(); i++) {
            writePermission.add(writeGroup.get(i));
        }
        for (int i = 0; i < executeGroup.size(); i++) {
            executePermission.add(executeGroup.get(i));
        }

        // Add the lists to the map
        permissions.put("READ", readPermission);
        permissions.put("EXECUTE", executePermission);
        permissions.put("WRITE", writePermission);
        return permissions;
    }

    private List<Map<String, Object>> generateJobObjectToUpdateApp(final String appName,
                                                                   final String updatedAppDescription) {

        List<Map<String, Object>> jobList = new ArrayList<>();
        Map<String, Object> jobEntry = new HashMap<>();
        jobEntry.put("type", "updateApplication");

        Map<String, Object> application = new HashMap<>();
        application.put("name", appName);
        application.put("description", updatedAppDescription);
        jobEntry.put("application", application);
        jobList.add(jobEntry);

        return jobList;
    }

    private List<Map<String, Object>> generateJobObjectToDeleteApp(final String appName) {

        List<Map<String, Object>> jobList = new ArrayList<>();
        Map<String, Object> jobEntry = new HashMap<>();
        jobEntry.put("type", "deleteApplication");

        Map<String, Object> application = new HashMap<>();
        application.put("name", appName);
        jobEntry.put("application", application);
        jobList.add(jobEntry);

        return jobList;
    }
}
