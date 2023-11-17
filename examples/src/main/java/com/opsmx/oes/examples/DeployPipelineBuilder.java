package com.opsmx.oes.examples;

import  com.opsmx.oes.common.util.ConfigDataProvider;
import com.opsmx.oes.pipelinebuilder.json.Pipeline;
import com.opsmx.oes.pipelinebuilder.json.Stage;
import com.opsmx.oes.pipelinebuilder.json.notifications.EmailNotification;
import com.opsmx.oes.pipelinebuilder.json.notifications.NotificationEvent;
import com.opsmx.oes.pipelinebuilder.json.notifications.SlackNotification;
import com.opsmx.oes.pipelinebuilder.json.stages.model.StageTypes;
import com.opsmx.oes.pipelinebuilder.json.triggers.WebhookTrigger;
import com.opsmx.oes.pipelinebuilder.pipelines.JsonPipelineBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DeployPipelineBuilder extends JsonPipelineBuilder {

    @Override
    public String getUniqueName() {
        return "deployPipeline";
    }

    @Override
    public Pipeline buildPipeline(final String appName, final String pipelineName) {

        final ConfigDataProvider config = new ConfigDataProvider(System.getProperty("user.dir")
                + "/../config.properties", System.getProperty("user.dir")
                + "/../config-overide.properties");
        final String emailNotificationAddress = config.getConfigData("emailNotificationAddress");
        final String slackNotificationAddress = config.getConfigData("slackNotificationAddress");

        final String startedMsg = config.getConfigData("startedMsg");
        final String completedMsg = config.getConfigData("completedMsg");
        final String failureMsg = config.getConfigData("failureMsg");
        final String awaitingManualJudgementMsg = config.getConfigData("awaitingManualJudgementMsg");
        final String continueManualJudgementMsg = config.getConfigData("continueManualJudgementMsg");
        final String stopManualJudgementMsg = config.getConfigData("stopManualJudgementMsg");

        final String webhookSource = config.getConfigData("webhookSource");
        final String k8sAccountName = config.getConfigData("k8sAccountName");
        final String k8sNamespace = config.getConfigData("k8sNamespace");
        final String k8sDeploymentImage = config.getConfigData("k8sDeploymentImage");
        final String k8sDeploymentApp = config.getConfigData("k8sDeploymentApp");
        final String k8sDeploymentImageVersion = config.getConfigData("k8sDeploymentImageVersion");

        Stage deployManifestStage = Stage.builder()
                .type(StageTypes.Kubernetes.DEPLOY_MANIFEST)
                .name("Deploy_manifest")
                .continuePipeline(true)
                .context(generateDeploymentObject(appName, k8sAccountName, k8sNamespace, k8sDeploymentImage,
                        k8sDeploymentApp, k8sDeploymentImageVersion))
                .notifications(List.of(
                        EmailNotification.builder()
                                .message(getMsgObject("stage", "DeployManifest Stage ",
                                        startedMsg, completedMsg, failureMsg))
                                .address(emailNotificationAddress)
                                .build(),
                        SlackNotification.builder()
                                .message(getMsgObject("stage", "DeployManifest Stage ",
                                        startedMsg, completedMsg, failureMsg))
                                .channel(slackNotificationAddress)
                                .build()))
                .build();

        Stage manualJudgementStage = Stage.builder()
                .type(StageTypes.MANUAL_JUDGMENT)
                .name("MANUAL_JUDGMENT_STAGE")
                .continuePipeline(true)
                .parentStageId(deployManifestStage.getId())
                .notifications(List.of(
                        EmailNotification.builder()
                                .message(getMsgObject("manualJudgement", "Manual Judgement Stage ",
                                        awaitingManualJudgementMsg, continueManualJudgementMsg, stopManualJudgementMsg))
                                .address(emailNotificationAddress)
                                .build(),
                        SlackNotification.builder()
                                .message(getMsgObject("manualJudgement", "Manual Judgement Stage ",
                                        awaitingManualJudgementMsg, continueManualJudgementMsg, stopManualJudgementMsg))
                                .channel(slackNotificationAddress)
                                .build()))
                .build();

        Stage deleteManifestStage = Stage.builder()
                .type(StageTypes.Kubernetes.DELETE_MANIFEST)
                .name("Delete (Manifest)")
                .continuePipeline(true)
                .parentStageId(manualJudgementStage.getId())
                .context(generateDeleteManifestObject(appName, k8sAccountName, k8sNamespace))
                .notifications(List.of(
                        EmailNotification.builder()
                                .message(getMsgObject("stage", "DeleteManifest Stage", startedMsg,
                                                                completedMsg, failureMsg))
                                .address(emailNotificationAddress)
                                .build(),
                        SlackNotification.builder()
                                .message(getMsgObject("stage", "DeleteManifest Stage", startedMsg,
                                                                completedMsg, failureMsg))
                                .channel(slackNotificationAddress)
                                .build()))
                .build();

        Pipeline pipeline = Pipeline.builder()
                .name(pipelineName)
                .stages(List.of(deployManifestStage, manualJudgementStage, deleteManifestStage))
                .notifications(List.of(
                        EmailNotification.builder()
                                .message(getMsgObject("pipeline", pipelineName, startedMsg, completedMsg,
                                        failureMsg))
                                .address(emailNotificationAddress)
                                .build(),
                        SlackNotification.builder()
                                .message(getMsgObject("pipeline", pipelineName, startedMsg, completedMsg,
                                        failureMsg))
                                .channel(slackNotificationAddress)
                                .build()))
                .trigger(WebhookTrigger.builder()
                        .enabled(true)
                        .source(webhookSource)
                        .build())
                .build();
        pipeline.setApplication(appName);
        return pipeline;
    }

    private Map<NotificationEvent, String> getMsgObject(final String entityType,
                                                        final String entityName,
                                                        final String startedMsg,
                                                        final String completedMsg,
                                                        final String failureMsg) {
        if(Objects.equals(entityType, "pipeline"))
            return Map.of(
                    NotificationEvent.PIPELINE_STARTING, entityName + startedMsg,
                    NotificationEvent.PIPELINE_COMPLETE, entityName + completedMsg,
                    NotificationEvent.PIPELINE_FAILED, entityName + failureMsg);
        else if(Objects.equals(entityType, "stage"))
            return Map.of(
                    NotificationEvent.STAGE_STARTING, entityName + startedMsg,
                    NotificationEvent.STAGE_COMPLETE, entityName + completedMsg,
                    NotificationEvent.STAGE_FAILED, entityName + failureMsg);
        return Map.of(
                NotificationEvent.MANUAL_JUDGMENT, entityName + startedMsg,
                NotificationEvent.MANUAL_JUDGMENT_CONTINUE, entityName + completedMsg,
                NotificationEvent.MANUAL_JUDGMENT_STOP, entityName + failureMsg);
    }

    private Map<String, Object> generateDeploymentObject(final String appName, final String k8sAccountName,
                                                         final String k8sNamespace, final String k8sDeploymentImage,
                                                         final String k8sDeploymentApp,
                                                         final String k8sDeploymentImageVersion) {
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
        deployManifestStage.put("namespaceOverride", k8sNamespace);

        List<Map<String, Object>> manifests = new ArrayList<>();
        Map<String, Object> deployment = new HashMap<>();
        deployment.put("apiVersion", "apps/v1");
        deployment.put("kind", "Deployment");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", k8sDeploymentImage);

        Map<String, Object> labels = new HashMap<>();
        labels.put("app", k8sDeploymentApp);
        metadata.put("labels", labels);

        deployment.put("metadata", metadata);

        Map<String, Object> spec = new HashMap<>();
        spec.put("replicas", 1);

        Map<String, Object> selector = new HashMap<>();
        Map<String, Object> matchLabels = new HashMap<>();
        matchLabels.put("app", k8sDeploymentApp);
        selector.put("matchLabels", matchLabels);

        spec.put("selector", selector);

        Map<String, Object> template = new HashMap<>();
        Map<String, Object> templateMetadata = new HashMap<>();
        Map<String, Object> templateLabels = new HashMap<>();
        templateLabels.put("app", k8sDeploymentApp);
        templateMetadata.put("labels", templateLabels);

        template.put("metadata", templateMetadata);

        Map<String, Object> templateSpec = new HashMap<>();
        List<Map<String, Object>> containers = new ArrayList<>();
        Map<String, Object> container = new HashMap<>();
        container.put("name", k8sDeploymentApp);
        container.put("image", k8sDeploymentImageVersion);

        List<Map<String, Object>> ports = new ArrayList<>();
        Map<String, Object> port = new HashMap<>();
        port.put("containerPort", 80);
        ports.add(port);

        container.put("ports", ports);
        containers.add(container);
        templateSpec.put("containers", containers);
        template.put("spec", templateSpec);

        spec.put("template", template);
        deployment.put("spec", spec);

        manifests.add(deployment);
        deployManifestStage.put("manifests", manifests);
        return deployManifestStage;
    }

    private Map<String, Object> generateDeleteManifestObject(final String appName, final String k8sAccountName,
                                                             final String k8sNamespace) {
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
        deleteManifestAction.put("manifestName", "Deployment nginx-deployment");

        return deleteManifestAction;
    }
}
