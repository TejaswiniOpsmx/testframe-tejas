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

public class AwsEcsPipelineBuilder extends JsonPipelineBuilder {

    @Override
    public String getUniqueName() {
        return "deployPipeline";
    }

    @Override
    public Pipeline buildPipeline(final String appName, final String pipelineName) {

        final ConfigDataProvider config = new ConfigDataProvider(System.getProperty("user.dir")
                + "/../config.properties", System.getProperty("user.dir")
                + "/../config-overide.properties");
        final String webhookSource = config.getConfigData("webhookSource");

        Stage findImageFromTags = Stage.builder()
                .type(StageTypes.AWS.fIND_IMAGE_FROM_TAGS)
                .name("Find Image From Tags")
                .context(generateFindImageFromTagsObject())
                .build();

        Stage deploy = Stage.builder()
                .type(StageTypes.AWS.DEPLOY)
                .name("Deploy")
                .parentStageId(findImageFromTags.getId())
                .context(generateDeployObject(appName))
                .build();

        Stage destroy = Stage.builder()
                .type(StageTypes.AWS.DESTROY_SERVER_GROUP)
                .name("Destroy Server Group")
                .parentStageId(deploy.getId())
                .context(generateDestroyObject(appName))
                .build();

        Pipeline pipeline = Pipeline.builder()
                .name(pipelineName)
                .stages(List.of(findImageFromTags, deploy, destroy))
                .build();
        pipeline.setApplication(appName);
        return pipeline;
    }

    private Map<String, Object> generateFindImageFromTagsObject() {
        Map<String, Object> FindImageFromTagsObject = new HashMap<>();
        FindImageFromTagsObject.put("cloudProvider", "ecs");
        FindImageFromTagsObject.put("cloudProviderType", "ecs");
        FindImageFromTagsObject.put("imageLabelOrSha", "732813442182.dkr.ecr.us-east-1.amazonaws.com/restapp-ecrdeploy:ecr-restapp-64");

        return FindImageFromTagsObject;
    }

    private Map<String, Object> generateDeployObject(final String appName) {
        Map<String, Object> deployObject = new HashMap<>();
        List<Map<String, Object>> clusters = new ArrayList<>();
        Map<String, Object> cluster = new HashMap<>();
        cluster.put("application", appName);
        cluster.put("account", "ecs-rol-spinnaker-managed-role");
        cluster.put("associatePublicIpAddress", true);

        Map<String, Object>  availabilityZone = new HashMap<>();
        List<String> zones = new ArrayList<>();
        zones.add("us-east-1a");
        zones.add("us-east-1b");
        zones.add("us-east-1c");
        zones.add("us-east-1d");
        zones.add("us-east-1e");
        zones.add("us-east-1f");
        availabilityZone.put("us-east-1",zones);
        cluster.put("availabilityZones", availabilityZone);

        Map<String, Object>  capacityVaules = new HashMap<>();
        capacityVaules.put("desired",1);
        capacityVaules.put("max",1);
        capacityVaules.put("min",1);
        cluster.put("capacity", capacityVaules);

        List<Map<String, Object>> capacityProviderStrateges = new ArrayList<>();
        cluster.put("capacityProviderStrategy",capacityProviderStrateges);
        cluster.put("cloudProvider","ecs");
        cluster.put("computeUnits",512);
        cluster.put("copySourceScalingPoliciesAndActions",true);
        cluster.put("dockerImageCredentialsSecret","None (No registry credentials)");

        Map<String, Object> dockerLabel = new HashMap<>();
        cluster.put("dockerLabels",dockerLabel);
        cluster.put("ecsClusterName","opsmx-ecs-cluster");

        Map<String, Object> environmentVariable = new HashMap<>();
        cluster.put("environmentVariables",environmentVariable);
        cluster.put("healthCheckGracePeriodSeconds","");
        cluster.put("healthCheckType","EC2");
        cluster.put("iamRole","ROL-Spinnaker-Managed-Role");

        Map<String, Object>  imageDescriptionObject = new HashMap<>();
        imageDescriptionObject.put("fromContext",true);
        imageDescriptionObject.put("imageId","732813442182.dkr.ecr.us-east-1.amazonaws.com/restapp-ecrdeploy:ecr-restapp-64");
        imageDescriptionObject.put("imageLabelOrSha","732813442182.dkr.ecr.us-east-1.amazonaws.com/restapp-ecrdeploy:ecr-restapp-64");
        imageDescriptionObject.put("stageId","3");
        cluster.put("imageDescription",imageDescriptionObject);
        cluster.put("launchType","FARGATE");

        List loadBalancers = new ArrayList();
        cluster.put("loadBalancers",loadBalancers);

        Map<String, Object> moniker = new HashMap<>();
        moniker.put("app", appName);
        cluster.put("moniker", moniker);
        cluster.put("networkMode", "awsvpc");
        List placementConstraints = new ArrayList();
        cluster.put("placementConstraints", placementConstraints);
        cluster.put("placementStrategyName", "");

        List placementStrategySequence = new ArrayList();
        cluster.put("placementStrategySequence", placementStrategySequence);
        cluster.put("preferSourceCapacity", true);
        cluster.put("provider", "ecs");
        cluster.put("reservedMemory", 1024);

        List<String> securityGroupNames = new ArrayList<>();
        securityGroupNames.add("spinEC2-New");
        cluster.put("securityGroupNames", securityGroupNames);
        List serviceDiscoveryAssociations = new ArrayList();
        cluster.put("serviceDiscoveryAssociations", serviceDiscoveryAssociations);
        cluster.put("strategy", "highlander");
        cluster.put("subnetType", "");

        List<String> subnetTypes = new ArrayList<>();
        subnetTypes.add("external (spinEC2vpc)");
        cluster.put("subnetTypes", subnetTypes);

        Map<String, Object>  tags = new HashMap<>();
        cluster.put("tags", tags);
        cluster.put("targetGroup", "");
        List targetGroupMappings = new ArrayList();
        cluster.put("targetGroupMappings", targetGroupMappings);
        Map<String, Object>  taskDefinitionArtifact = new HashMap<>();
        cluster.put("taskDefinitionArtifact", taskDefinitionArtifact);
        cluster.put("useSourceCapacity", true);
        cluster.put("useTaskDefinitionArtifact", false);
        List vpcLoadBalancers = new ArrayList();
        cluster.put("vpcLoadBalancers", vpcLoadBalancers);
        clusters.add(cluster);
        deployObject.put("clusters",clusters);

        return deployObject;
    }

    private Map<String, Object> generateDestroyObject(final String appName) {
        Map<String, Object> destroyServerGroup = new HashMap<>();
        destroyServerGroup.put("cloudProvider", "ecs");
        destroyServerGroup.put("cloudProviderType", "ecs");
        destroyServerGroup.put("cluster", appName);
        destroyServerGroup.put("credentials", "ecs-rol-spinnaker-managed-role");
        destroyServerGroup.put("isNew", true);
        Map<String, Object> moniker = new HashMap<>();
        moniker.put("app", appName);
        moniker.put("cluster", appName);
        moniker.put("sequence", null);
        destroyServerGroup.put("moniker", moniker);

        List<String> zones = new ArrayList<>();
        zones.add("us-east-1");
        destroyServerGroup.put("regions", zones);
        destroyServerGroup.put("target", "current_asg_dynamic");

        return destroyServerGroup;
    }
}