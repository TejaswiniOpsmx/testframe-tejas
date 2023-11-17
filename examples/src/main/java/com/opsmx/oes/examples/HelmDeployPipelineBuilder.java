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

public class HelmDeployPipelineBuilder extends JsonPipelineBuilder {

    @Override
    public String getUniqueName() {
        return "helmDeployPipeline";
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
        final String shortWaitStagePeriod = config.getConfigData("shortWaitStagePeriod");
        final String pipelineDescription = "This pipeline shows the Kubernetes deployment through Helm chart. " +
                "Bake stage fetch the helm chart from Github repo and deploy stage make uses of produced " +
                "base64 artifact from bake stage and deploys it";

        Stage bakeStage = Stage.builder()
                .type(StageTypes.Kubernetes.BAKE_MANIFEST)
                .name(bakeStageName)
                .continuePipeline(true)
                .context(generateBakeObject(k8sNamespace))
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
                .context(Map.of("waitTime", shortWaitStagePeriod))
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
                .description(pipelineDescription)
                .stages(List.of(bakeStage, deployManifestStage, waitStage, deleteManifestStage))
                .build();
        pipeline.setApplication(appName);
        return pipeline;
    }

    private Map<String, Object> generateBakeObject(final String k8sNamespace) {
        List<Map<String, Object>> expectedArtifacts = new ArrayList<>();
        Map<String, Object> expectedArtifacts1 = new HashMap<>();
        Map<String, Object> defaultArtifact = new HashMap<>();
        defaultArtifact.put("customKind", true);
        defaultArtifact.put("id", "aa792ec7-1c58-478e-adce-dc9a88e9a68a");
        expectedArtifacts1.put("defaultArtifact", defaultArtifact);
        expectedArtifacts1.put("displayName", "helm-artifact");
        expectedArtifacts1.put("id", "26231da3-4ad4-4788-b240-8cc0f32df0bb");
        expectedArtifacts1.put("useDefaultArtifact", false);
        expectedArtifacts1.put("usePriorArtifact", false);

        Map<String, Object> matchArtifact = new HashMap<>();
        matchArtifact.put("artifactAccount", "embedded-artifact");
        matchArtifact.put("customKind", false);
        matchArtifact.put("id", "6ed9cece-feaf-4614-889f-f8c5c90abc2c");
        matchArtifact.put("name", "helm");
        matchArtifact.put("type", "embedded/base64");
        expectedArtifacts1.put("matchArtifact", matchArtifact);
        expectedArtifacts.add(expectedArtifacts1);

        List<Map<String, Object>> inputArtifacts = new ArrayList<>();
        Map<String, Object> inputArtifact1 = new HashMap<>();
        inputArtifact1.put("account", "opsmxdemo_account");
        Map<String, Object> artifact1 = new HashMap<>();
        artifact1.put("artifactAccount", "opsmxdemo_account");
        artifact1.put("id", "27c16dd7-f723-4653-afe1-08e103b612b7");
        artifact1.put("name", "helmchart/hello-world-0.1.0.tgz");
        artifact1.put("reference", "https://api.github.com/repos/opsmx/sample-pipeline-manifest/contents/helmchart/helloworld-0.1.0.tgz");
        artifact1.put("type", "github/file");
        artifact1.put("version", "main");
        inputArtifact1.put("artifact", artifact1);
        inputArtifacts.add(inputArtifact1);

        Map<String, Object> inputArtifact2 = new HashMap<>();
        inputArtifact2.put("account", "opsmxdemo_account");
        Map<String, Object> artifact2 = new HashMap<>();
        artifact2.put("artifactAccount", "opsmxdemo_account");
        artifact2.put("id", "5c2c2d47-c08e-4757-8b5d-0e9e63c525d8");
        artifact2.put("name", "helmchart/hello-world/values.yaml.dev");
        artifact2.put("reference", "https://api.github.com/repos/opsmx/sample-pipeline-manifest/contents/helmchart/hello-world/values.yaml.dev");
        artifact2.put("type", "github/file");
        artifact2.put("version", "main");
        inputArtifact2.put("artifact", artifact2);
        inputArtifacts.add(inputArtifact2);

        Map<String, Object> bakeStage = new HashMap<>();
        bakeStage.put("expectedArtifacts", expectedArtifacts);
        bakeStage.put("inputArtifacts", inputArtifacts);
        bakeStage.put("name", "Bake (Manifest)");
        bakeStage.put("namespace", k8sNamespace);
        bakeStage.put("outputName", "helm");
        bakeStage.put("overrides", new HashMap<>());
        bakeStage.put("rawOverrides", false);
        bakeStage.put("requisiteStageRefIds", new ArrayList<>());
        bakeStage.put("templateRenderer", "HELM2");
        bakeStage.put("type", "bakeManifest");

        return bakeStage;
    }

    private Map<String, Object> generateDeploymentObject(final String appName, final String k8sAccountName,
                                                         final String k8sNamespace) {
        Map<String, Object> deployStage = new HashMap<>();
        deployStage.put("account", k8sAccountName);
        deployStage.put("cloudProvider", "kubernetes");
        deployStage.put("manifestArtifactId", "26231da3-4ad4-4788-b240-8cc0f32df0bb");

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
        deleteManifestStage.put("manifestName", "deployment helm-deployment");
        deleteManifestStage.put("mode", "static");
        deleteManifestStage.put("name", "Delete (Manifest)");

        Map<String, Boolean> options = new HashMap<>();
        options.put("cascading", true);
        deleteManifestStage.put("options", options);

        deleteManifestStage.put("type", "deleteManifest");
        return deleteManifestStage;
    }
}
