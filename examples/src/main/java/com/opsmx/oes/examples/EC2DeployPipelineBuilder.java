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

public class EC2DeployPipelineBuilder extends JsonPipelineBuilder {

    @Override
    public String getUniqueName() {
        return "ec2DeployPipeline";
    }

    final ConfigDataProvider config = new ConfigDataProvider(System.getProperty("user.dir")
            + "/../config.properties", System.getProperty("user.dir")
            + "/../config-overide.properties");

    @Override
    public Pipeline buildPipeline(final String appName, final String pipelineName) {

        final String k8sAccountName = config.getConfigData("k8sAccountName");

        Stage bakeStage = Stage.builder()
                .type(StageTypes.Kubernetes.BAKE_MANIFEST)
                .name("Bake Stage")
                .continuePipeline(true)
                .context(generateBakeObject())
                .build();

        Stage deployStage = Stage.builder()
                .type(StageTypes.Kubernetes.DEPLOY_MANIFEST)
                .name("Deploy Stage")
                .continuePipeline(true)
                .parentStageId(bakeStage.getId())
                .context(generateDeploymentObject(appName))
                .build();

        Stage destroyServerGroupStage = Stage.builder()
                .type(StageTypes.Kubernetes.DELETE_MANIFEST)
                .name("Destroy Stage")
                .continuePipeline(true)
                .parentStageId(deployStage.getId())
                .context(generateDestroyServerGroupObject(appName))
                .build();

        Pipeline pipeline = Pipeline.builder()
                .name(pipelineName)
                .stages(List.of(bakeStage, deployStage, destroyServerGroupStage))
                .build();
        pipeline.setApplication(appName);
        return pipeline;
    }

    private Map<String, Object> generateBakeObject() {
        final String baseAmi = config.getConfigData("baseAmi");
        final String baseLabel = config.getConfigData("baseLabel");
        final String baseName = config.getConfigData("baseName");
        final String baseOs = config.getConfigData("baseOs");
        final String cloudProviderType = config.getConfigData("cloudProviderType");
        final String bakeName = config.getConfigData("name");
        final String packageName = config.getConfigData("package");
        final String rebakeStatus = config.getConfigData("rebakeStatus");
        final String awsRegion = config.getConfigData("awsRegion");
        final String skipRegionDetection = config.getConfigData("skipRegionDetection");
        final String storeType = config.getConfigData("storeType");
        final String type = config.getConfigData("type");
        final String user = config.getConfigData("user");
        final String vmType = config.getConfigData("vmType");

        Map<String, Object> bakeMap = new HashMap<>();
        bakeMap.put("baseAmi", baseAmi);
        bakeMap.put("baseLabel", baseLabel);
        bakeMap.put("baseName", baseName);
        bakeMap.put("baseOs", baseOs);
        bakeMap.put("cloudProviderType", cloudProviderType);
        bakeMap.put("extendedAttributes", new HashMap<>());
        bakeMap.put("name", bakeName);
        bakeMap.put("package", packageName);
        bakeMap.put("rebake", rebakeStatus);

        List<String> regions = new ArrayList<>();
        regions.add(awsRegion);
        bakeMap.put("regions", regions);

        bakeMap.put("skipRegionDetection", skipRegionDetection);
        bakeMap.put("storeType", storeType);
        bakeMap.put("type", type);
        bakeMap.put("user", user);
        bakeMap.put("vmType", vmType);

        return bakeMap;
    }

    private Map<String, Object> generateDeploymentObject(final String appName) {
        final String associatePublicIpAddressStatus = config.getConfigData("associatePublicIpAddressStatus");
        final String us_east_1a = config.getConfigData("us-east-1a");
        final String us_east_1b = config.getConfigData("us-east-1b");
        final String us_east_1c = config.getConfigData("us-east-1c");
        final String us_east_1d = config.getConfigData("us-east-1d");
        final String us_east_1e = config.getConfigData("us-east-1e");
        final String us_east_1f = config.getConfigData("us-east-1f");
        final String cloudProvider = config.getConfigData("cloudProvider");
        final String lbName = config.getConfigData("lbName");
        final String instanceType = config.getConfigData("instanceType");
        final String detail = config.getConfigData("detail");
        final String stack = config.getConfigData("stack");
        final String ec2Account = config.getConfigData("ec2Account");
        final String clusterName = appName + "-" + stack + "-" + detail;

        Map<String, Object> deployMap = new HashMap<>();

        List<Map<String, Object>> clusters = new ArrayList<>();
        Map<String, Object> cluster = new HashMap<>();
        cluster.put("account", ec2Account);
        cluster.put("application", appName);
        cluster.put("associatePublicIpAddress", associatePublicIpAddressStatus);

        Map<String, List<String>> availabilityZones = new HashMap<>();
        List<String> usEast1Zones = new ArrayList<>();
        usEast1Zones.add(us_east_1a);
        usEast1Zones.add(us_east_1b);
        usEast1Zones.add(us_east_1c);
        usEast1Zones.add(us_east_1d);
        usEast1Zones.add(us_east_1e);
        usEast1Zones.add(us_east_1f);
        availabilityZones.put("us-east-1", usEast1Zones);
        cluster.put("availabilityZones", availabilityZones);

        Map<String, Integer> capacity = new HashMap<>();
        capacity.put("desired", 1);
        capacity.put("max", 1);
        capacity.put("min", 1);
        cluster.put("capacity", capacity);

        cluster.put("cloudProvider", cloudProvider);
        cluster.put("cooldown", 10);
        cluster.put("copySourceCustomBlockDeviceMappings", false);
        cluster.put("ebsOptimized", false);
        cluster.put("enabledMetrics", new ArrayList<>());
        cluster.put("freeFormDetails", "stack");
        cluster.put("healthCheckGracePeriod", 600);
        cluster.put("healthCheckType", "EC2");
        cluster.put("iamRole", "BaseIAMRole");
        cluster.put("instanceMonitoring", false);
        cluster.put("instanceType", instanceType);
        cluster.put("interestingHealthProviderNames", new ArrayList<String>() {{
            add("Amazon");
        }});
        cluster.put("keyPair", "EC2Keys");

        List<String> loadBalancers = new ArrayList<>();
        loadBalancers.add(lbName);
        cluster.put("loadBalancers", loadBalancers);

        Map<String, String> moniker = new HashMap<>();
        moniker.put("app", appName);
        moniker.put("detail", detail);
        moniker.put("stack", stack);
        cluster.put("moniker", moniker);

        cluster.put("provider", "aws");

        List<String> securityGroups = new ArrayList<>();
        securityGroups.add("sg-0bdfef8aa65c5618a");
        cluster.put("securityGroups", securityGroups);

        cluster.put("spotPrice", "");
        cluster.put("stack", stack);
        cluster.put("strategy", "highlander");
        cluster.put("subnetType", "external (spinEC2vpc)");
        cluster.put("suspendedProcesses", new ArrayList<>());
        cluster.put("tags", new HashMap<>());
        cluster.put("targetGroups", new ArrayList<>());
        cluster.put("targetHealthyDeployPercentage", 100);

        List<String> terminationPolicies = new ArrayList<>();
        terminationPolicies.add("Default");
        cluster.put("terminationPolicies", terminationPolicies);
        cluster.put("useAmiBlockDeviceMappings", false);

        clusters.add(cluster);

        deployMap.put("clusters", clusters);
        deployMap.put("name", "DeployDev");
        deployMap.put("type", "deploy");
        return deployMap;
    }

    private Map<String, Object> generateDestroyServerGroupObject(final String appName) {
        final String cloudProvider = config.getConfigData("cloudProvider");
        final String cloudProviderType = config.getConfigData("cloudProviderType");
        final String us_east_1 = config.getConfigData("us-east-1");
        final String ec2_destroyStage_target = config.getConfigData("ec2_destroyStage_target");
        final String ec2_destroyStage_type = config.getConfigData("ec2_destroyStage_type");
        final String detail = config.getConfigData("detail");
        final String stack = config.getConfigData("stack");
        final String ec2Account = config.getConfigData("ec2Account");
        final String clusterName = appName + "-" + stack + "-" + detail;

        Map<String, Object> destroyServerGroupMap = new HashMap<>();
        destroyServerGroupMap.put("cloudProvider", cloudProvider);
        destroyServerGroupMap.put("cloudProviderType", cloudProviderType);
        destroyServerGroupMap.put("cluster", clusterName);
        destroyServerGroupMap.put("credentials", ec2Account);

        Map<String, Object> moniker = new HashMap<>();
        moniker.put("app", appName);
        moniker.put("cluster", clusterName);
        moniker.put("detail", detail);
        moniker.put("stack", stack);
        destroyServerGroupMap.put("moniker", moniker);

        destroyServerGroupMap.put("name", "Destroy Server Group");

        List<String> regions = new ArrayList<>();
        regions.add(us_east_1);
        destroyServerGroupMap.put("regions", regions);

        destroyServerGroupMap.put("target", ec2_destroyStage_target);
        destroyServerGroupMap.put("type", ec2_destroyStage_type);

        return destroyServerGroupMap;
    }
}