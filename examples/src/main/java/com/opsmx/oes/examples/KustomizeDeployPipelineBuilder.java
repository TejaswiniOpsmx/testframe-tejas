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

public class KustomizeDeployPipelineBuilder extends JsonPipelineBuilder {

    @Override
    public String getUniqueName() {
        return "kustomizeDeployPipeline";
    }

    @Override
    public Pipeline buildPipeline(final String appName, final String pipelineName) {

        final ConfigDataProvider config = new ConfigDataProvider(System.getProperty("user.dir")
            + "/../config.properties", System.getProperty("user.dir")
            + "/../config-overide.properties");
        final String k8sAccountName = config.getConfigData("k8sAccountName");
        final String k8sNamespace = config.getConfigData("k8sNamespace");
        final String bakeStageName = config.getConfigData("bakeStageName");
        final String deployManifestStageName = config.getConfigData("deployManifestStageName");
        final String deleteManifestStageName = config.getConfigData("deleteManifestStageName");
        final String waitStageName = config.getConfigData("waitStageName");
        final String waitStagePeriod = config.getConfigData("waitStagePeriod");

        Stage bakeStage = Stage.builder()
                .type(StageTypes.Kubernetes.BAKE_MANIFEST)
                .name(bakeStageName)
                .continuePipeline(true)
                .context(generateBakeObject())
                .build();

        Stage deployManifestStage = Stage.builder()
                .type(StageTypes.Kubernetes.DEPLOY_MANIFEST)
                .name(deployManifestStageName)
                .continuePipeline(true)
                .parentStageId(bakeStage.getId())
                .context(generateDeploymentObject(appName, k8sAccountName, k8sNamespace))
                .build();

        Stage waitStage = Stage.builder()
                .type(StageTypes.WAIT)
                .name(waitStageName)
                .continuePipeline(true)
                .parentStageId(deployManifestStage.getId())
                .context(Map.of("waitTime", waitStagePeriod))
                .build();

        Stage deleteManifestStage = Stage.builder()
                .type(StageTypes.Kubernetes.DELETE_MANIFEST)
                .name(deleteManifestStageName)
                .continuePipeline(true)
                .parentStageId(waitStage.getId())
                .context(generateDeleteManifestObject(appName, k8sAccountName, k8sNamespace))
                .build();

        Pipeline pipeline = Pipeline.builder()
                .name(pipelineName)
                .stages(List.of(bakeStage, deployManifestStage, waitStage, deleteManifestStage))
                .build();
        pipeline.setApplication(appName);
        return pipeline;
    }

    private Map<String, Object> generateBakeObject() {
        List<Map<String, Object>> expectedArtifacts = new ArrayList<>();
        Map<String, Object> expectedArtifact1 = new HashMap<>();
        expectedArtifact1.put("defaultArtifact", Map.of("customKind", true,
                "id", "0fb7aefd-2cc3-4c92-af36-60521b458a81"));
        expectedArtifact1.put("displayName", "helloworld");
        expectedArtifact1.put("id", "eef22150-5ae6-42d1-aa2d-81c894017fe9");

        Map<String, Object> matchArtifact = new HashMap<>();
        matchArtifact.put("artifactAccount", "embedded-artifact");
        matchArtifact.put("customKind", false);
        matchArtifact.put("id", "586396c4-fcee-4bf8-a758-6ff61c7542e3");
        matchArtifact.put("type", "embedded/base64");

        expectedArtifact1.put("matchArtifact", matchArtifact);
        expectedArtifact1.put("useDefaultArtifact", false);
        expectedArtifact1.put("usePriorArtifact", false);
        expectedArtifacts.add(expectedArtifact1);

        Map<String, Object> inputArtifact = new HashMap<>();
        inputArtifact.put("account", "opsmx_repo");

        Map<String, Object> artifact = new HashMap<>();
        artifact.put("artifactAccount", "opsmx_repo");
        artifact.put("customKind", true);
        artifact.put("id", "72fbda2b-2a4a-43a4-92a8-d8fe1bdd3983");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("subPath", "kustomize/helloworld/base/");

        artifact.put("metadata", metadata);
        artifact.put("reference", "https://github.com/OpsMx/sample-pipeline-manifest");
        artifact.put("type", "git/repo");
        artifact.put("version", "main");

        inputArtifact.put("artifact", artifact);

        Map<String, Object> bakeStage = new HashMap<>();
        bakeStage.put("expectedArtifacts", expectedArtifacts);
        bakeStage.put("inputArtifact", inputArtifact);

        bakeStage.put("kustomizeFilePath", "kustomize/helloworld/base/kustomization.yaml");
        bakeStage.put("name", "Bake (Manifest)");
        bakeStage.put("overrides", new HashMap<>());
        bakeStage.put("templateRenderer", "KUSTOMIZE");
        bakeStage.put("type", "bakeManifest");
        return bakeStage;
    }

    private Map<String, Object> generateDeploymentObject(final String appName, final String k8sAccountName,
                                                         final String k8sNamespace) {
        Map<String, Object> deployStage = new HashMap<>();
        deployStage.put("account", k8sAccountName);
        deployStage.put("cloudProvider", "kubernetes");
        deployStage.put("manifestArtifactId", "eef22150-5ae6-42d1-aa2d-81c894017fe9");

        Map<String, String> moniker = new HashMap<>();
        moniker.put("app", appName);
        deployStage.put("moniker", moniker);

        deployStage.put("name", "Deploy (Manifest)");
        deployStage.put("namespaceOverride", k8sNamespace);

        deployStage.put("skipExpressionEvaluation", false);
        deployStage.put("source", "artifact");

        Map<String, Object> trafficManagement = new HashMap<>();
        trafficManagement.put("enabled", false);

        Map<String, Object> options = new HashMap<>();
        options.put("enableTraffic", false);
        trafficManagement.put("options", options);

        deployStage.put("trafficManagement", trafficManagement);
        deployStage.put("type", "deployManifest");
        return deployStage;

    }

    private Map<String, Object> generateDeleteManifestObject(final String appName, final String k8sAccountName,
                                                             final String k8sNamespace) {
        Map<String, Object> deleteManifestStage = new HashMap<>();
        deleteManifestStage.put("account", k8sAccountName);
        deleteManifestStage.put("app", appName);
        deleteManifestStage.put("cloudProvider", "kubernetes");
        deleteManifestStage.put("location", k8sNamespace);
        deleteManifestStage.put("manifestName", "deployment the-deployment");
        deleteManifestStage.put("mode", "static");
        deleteManifestStage.put("name", "Delete (Manifest)");

        Map<String, Boolean> options = new HashMap<>();
        options.put("cascading", true);
        deleteManifestStage.put("options", options);

        deleteManifestStage.put("type", "deleteManifest");
        return deleteManifestStage;
    }
}

