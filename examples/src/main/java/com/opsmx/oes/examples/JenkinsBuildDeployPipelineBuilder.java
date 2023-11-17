package com.opsmx.oes.examples;

import com.opsmx.oes.common.util.ConfigDataProvider;
import com.opsmx.oes.pipelinebuilder.json.Pipeline;
import com.opsmx.oes.pipelinebuilder.json.Stage;
import com.opsmx.oes.pipelinebuilder.json.stages.model.StageTypes;
import com.opsmx.oes.pipelinebuilder.pipelines.JsonPipelineBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JenkinsBuildDeployPipelineBuilder extends JsonPipelineBuilder {

    @Override
    public String getUniqueName() {
        return "jenkinsBuildDeployPipeline";
    }

    @Override
    public Pipeline buildPipeline(final String appName, final String pipelineName) {

        final ConfigDataProvider config = new ConfigDataProvider(System.getProperty("user.dir")
            + "/../config.properties", System.getProperty("user.dir")
            + "/../config-overide.properties");
        final String k8sAccountName = config.getConfigData("k8sAccountName");
        final String k8sNamespace = config.getConfigData("k8sNamespace");
        final String jenkinsJob = config.getConfigData("job");
        final String jenkinsMaster = config.getConfigData("master");
        final String propertyFile = config.getConfigData("propertyFile");
        final String jenkinsBuildStageName = config.getConfigData("jenkinsBuildStageName");

        final String jenkinsBuildAppName = config.getConfigData("jenkinsBuildAppName");
        final String jenkinsBuildDeleteAppName = config.getConfigData("jenkinsBuildDeleteAppName");
        final String jenkinsBuildImage = config.getConfigData("jenkinsBuildImage");

        Stage jenkinsBuildStage = Stage.builder()
                .type(StageTypes.Jenkins.JENKINS)
                .name(jenkinsBuildStageName)
                .continuePipeline(false)
                .jenkinsJob(jenkinsJob)
                .jenkinsMaster(jenkinsMaster)
                .jenkinsPropertyFile(propertyFile)
                .build();

        Stage deployManifestStage = Stage.builder()
                .type(StageTypes.Kubernetes.DEPLOY_MANIFEST)
                .name("Deploy_manifest")
                .continuePipeline(true)
                .parentStageId(jenkinsBuildStage.getId())
                .context(generateDeploymentObject(appName, k8sAccountName, k8sNamespace, jenkinsBuildAppName,
                        jenkinsBuildImage))
                .build();

        Stage deleteManifestStage = Stage.builder()
                .type(StageTypes.Kubernetes.DELETE_MANIFEST)
                .name("Delete (Manifest)")
                .continuePipeline(true)
                .parentStageId(deployManifestStage.getId())
                .context(generateDeleteManifestObject(appName, k8sAccountName, k8sNamespace, jenkinsBuildDeleteAppName))
                .build();

        Pipeline pipeline = Pipeline.builder()
                .name(pipelineName)
                .stages(List.of(jenkinsBuildStage, deployManifestStage, deleteManifestStage))
                .build();
        pipeline.setApplication(appName);
        return pipeline;
    }

    private Map<String, Object> generateDeploymentObject(final String appName, final String k8sAccountName,
                                                         final String k8sNamespace, final String jenkinsBuildAppName,
                                                         final String jenkinsBuildImage) {
        Map<String, Object> deployManifestStage = new HashMap<>();
        deployManifestStage.put("type", "deployManifest");
        deployManifestStage.put("source", "text");
        deployManifestStage.put("skipExpressionEvaluation", false);

        Map<String, Object> trafficManagement = new HashMap<>();
        trafficManagement.put("enabled", false);

        Map<String, Object> trafficOptions = new HashMap<>();
        trafficOptions.put("namespace", null);
        trafficOptions.put("services", new ArrayList<String>());
        trafficOptions.put("enableTraffic", false);
        trafficOptions.put("strategy", null);

        trafficManagement.put("options", trafficOptions);
        deployManifestStage.put("trafficManagement", trafficManagement);

        deployManifestStage.put("cloudProvider", "kubernetes");

        Map<String, Object> moniker = new HashMap<>();
        moniker.put("app", appName);
        deployManifestStage.put("moniker", moniker);

        deployManifestStage.put("account", k8sAccountName);
        deployManifestStage.put("namespace", k8sNamespace);

        List<Map<String, Object>> manifests = new ArrayList<>();
        Map<String, Object> deployment = new HashMap<>();
        deployment.put("apiVersion", "apps/v1");
        deployment.put("kind", "Deployment");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", jenkinsBuildAppName);

        deployment.put("metadata", metadata);

        Map<String, Object> spec = new HashMap<>();
        spec.put("replicas", 1);

        Map<String, Object> selector = new HashMap<>();
        Map<String, Object> matchLabels = new HashMap<>();
        matchLabels.put("app", jenkinsBuildAppName);
        selector.put("matchLabels", matchLabels);

        spec.put("selector", selector);

        Map<String, Object> template = new HashMap<>();
        Map<String, Object> templateMetadata = new HashMap<>();
        Map<String, Object> templateLabels = new HashMap<>();
        templateLabels.put("app", jenkinsBuildAppName);
        templateMetadata.put("labels", templateLabels);

        template.put("metadata", templateMetadata);

        Map<String, Object> templateSpec = new HashMap<>();
        List<Map<String, Object>> containers = new ArrayList<>();
        Map<String, Object> container = new HashMap<>();
        container.put("name", jenkinsBuildAppName);
        container.put("imagePullPolicy", "Always");
        container.put("image", jenkinsBuildImage);

        List<Map<String, Object>> envList = new ArrayList<>();
        Map<String, Object> envVar1 = new HashMap<>();
        envVar1.put("name", "DD_AGENT_HOST");
        Map<String, Object> valueFrom1 = new HashMap<>();
        Map<String, Object> fieldRef1 = new HashMap<>();
        fieldRef1.put("apiVersion", "v1");
        fieldRef1.put("fieldPath", "status.hostIP");
        valueFrom1.put("fieldRef", fieldRef1);
        envVar1.put("valueFrom", valueFrom1);
        envList.add(envVar1);
        Map<String, Object> envVar2 = new HashMap<>();
        envVar2.put("name", "SERVICE_NAME");
        envVar2.put("value", jenkinsBuildAppName);
        envList.add(envVar2);
        container.put("env", envList);

        List<Map<String, Object>> ports = new ArrayList<>();
        Map<String, Object> port = new HashMap<>();
        port.put("containerPort", 8088);
        port.put("name", "http");
        port.put("protocol", "TCP");
        ports.add(port);

        container.put("ports", ports);
        containers.add(container);
        templateSpec.put("containers", containers);
        templateSpec.put("restartPolicy", "Always");
        template.put("spec", templateSpec);

        spec.put("template", template);
        deployment.put("spec", spec);

        manifests.add(deployment);
        deployManifestStage.put("manifests", manifests);
        return deployManifestStage;
    }

    private Map<String, Object> generateDeleteManifestObject(final String appName, final String k8sAccountName,
                                                             final String k8sNamespace,
                                                             final String jenkinsBuildDeleteAppName) {
        Map<String, Object> deleteManifestAction = new HashMap<>();
        deleteManifestAction.put("kind", "Deployment");
        deleteManifestAction.put("mode", "static");
        deleteManifestAction.put("app", appName);
        deleteManifestAction.put("cloudProvider", "kubernetes");

        Map<String, Object> options = new HashMap<>();
        options.put("gracePeriodSeconds", null);
        options.put("cascading", true);
        deleteManifestAction.put("options", options);

        deleteManifestAction.put("location", k8sNamespace);
        deleteManifestAction.put("account", k8sAccountName);
        deleteManifestAction.put("manifestName", jenkinsBuildDeleteAppName);

        return deleteManifestAction;
    }
}
