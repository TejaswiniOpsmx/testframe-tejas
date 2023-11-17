package com.opsmx.oes.spintests.base;

public class APIEndpoints extends BaseClass {

    // Application
    public final static String createApplication = "dashboardservice/v2/application";
    public final static String createApplicationInSpinnaker = "tasks";
    public final static String updateApplicationInSpinnaker = "tasks";
    public final static String deleteApplicationInSpinnaker = "tasks";
    public final static String addNotificationToApplication = "notifications/application/spinnakerAppName";

    // Pipeline
    public final static String createPipelineInSpinnaker = "pipelines";
    public final static String getPipelineConfigs = "applications/spinnakerAppName/pipelineConfigs";
    public final static String executePipeline = "pipelines/v2/appName/pipelineName";
    public final static String updatePipeline = "pipelines/pipelineID";
}
