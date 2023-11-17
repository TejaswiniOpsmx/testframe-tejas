package com.opsmx.oes.spintests.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsmx.oes.examples.*;
import com.opsmx.oes.pipelinebuilder.json.Pipeline;
import com.opsmx.oes.spintests.base.APIEndpoints;
import com.opsmx.oes.spintests.base.BaseClass;
import com.opsmx.oes.spintests.base.Constants;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.restassured.RestAssured.with;

public class Pipelines extends BaseClass {

    private static Response response;

    /**
     * This method is used to create pipelines
     *
     * @return response
     * @throws Exception
     */
    public static synchronized Response createPipeline(final String type, final String appName, final String pipelineName)
            throws Exception {
        try {
            if(type == Constants.DEPLOY) {
                payload = new DeployPipelineBuilder().buildPipeline(appName, pipelineName).toJson();
            } else if(type == Constants.CRON_TRIGGER) {
                payload = new CronTriggerPipelineBuilder().buildPipeline(appName, pipelineName).toJson();
            } else if(type == Constants.JENKINS_TRIGGER) {
                payload = new JenkinsTriggerPipelineBuilder().buildPipeline(appName, pipelineName).toJson();
            } else if(type == Constants.GIT_TRIGGER) {
                payload = new GitTriggerPipelineBuilder().buildPipeline(appName, pipelineName).toJson();
            } else if(type == Constants.DOCKER_REGISTRY_TRIGGER) {
                payload = new DockerTriggerPipelineBuilder().buildPipeline(appName, pipelineName).toJson();
            } else if(type == Constants.JENKINS) {
                payload = new JenkinsBuildDeployPipelineBuilder().buildPipeline(appName, pipelineName).toJson();
            } else if(type == Constants.EC2) {
                payload = new EC2DeployPipelineBuilder().buildPipeline(appName, pipelineName).toJson();
            }else if(type == Constants.ECS) {
                payload = new AwsEcsPipelineBuilder().buildPipeline(appName, pipelineName).toJson();
            } else if(type == Constants.HELM) {
                payload = new HelmDeployPipelineBuilder().buildPipeline(appName, pipelineName).toJson();
            } else if(type == Constants.KUSTOMIZE) {
                payload = new KustomizeDeployPipelineBuilder().buildPipeline(appName, pipelineName).toJson();
            }
            LOGGER.info("Payload to create pipeline in spinnaker :" + payload);

            URL = spinnakerURL + APIEndpoints.createPipelineInSpinnaker;
            LOGGER.info("End point URL to create pipeline in spinnaker: " + URL);

            response = requestSpec.queryParam("staleCheck", "true").body(payload).post(URL).thenReturn();
            responseData = response.asString();
            LOGGER.info("Response from create pipeline API: " + responseData);
        } catch (Exception e) {
            LOGGER.error("Create Pipeline Failed - " + e.getMessage());
        }
        return response;
    }

    /**
     * This method is used to execute pipeline
     *
     * @return response
     */
    public static Response executePipeline(final String appName) {
        try {
            JSONObject executePipelinePayload = new JSONObject();
            executePipelinePayload.put("buildNumber", "");
            executePipelinePayload.put("type", "manual");
            executePipelinePayload.put("dryRun", false);
            executePipelinePayload.put("user", spinUsername);

            payload = executePipelinePayload.toString();
            LOGGER.info("Payload for executing pipeline :" + payload);

            //Adding delay before executing pipeline so that all spinnaker services got synced
            Thread.sleep(20000);

            String deployPipeline = config.getConfigData("deployPipelineName");
            String jenkinsBuildAndDeployPipeline = config.getConfigData("jenkinsBuildAndDeployPipelineName");
            String helmDeploymentPipeline = config.getConfigData("helmDeploymentPipelineName");
            String kustomizeDeploymentPipeline = config.getConfigData("kustomizeDeploymentPipelineName");
            String ec2DeploymentPipeline = config.getConfigData("ec2DeploymentPipelineName");
            String ecsDeploymentPipeline = config.getConfigData("ecsDeploymentPipelineName");

            List<String> pipelinesList = new ArrayList<>();
            pipelinesList.add(deployPipeline);
            pipelinesList.add(jenkinsBuildAndDeployPipeline);
            pipelinesList.add(helmDeploymentPipeline);
            pipelinesList.add(kustomizeDeploymentPipeline);
            pipelinesList.add(ec2DeploymentPipeline);
            pipelinesList.add(ecsDeploymentPipeline);

            // Iterate over the pipelines to execute
            for (String pipeline : pipelinesList) {
                URL = spinnakerURL + APIEndpoints.executePipeline.replace("appName", appName)
                        .replace("pipelineName", pipeline);
                LOGGER.info("End point URL: " + URL);
                Response response = requestSpec.body(payload).post(URL).then().extract().response();
                LOGGER.info("Response from executing pipeline=" + pipeline + ": " + response.asString());
            }
        } catch (Exception e) {
            LOGGER.error("ExecutePipeline Failed - " + e.getMessage());
        }
        return response;
    }

    /**
     * This method is used to update pipeline
     *
     * @return response
     */
    public static Response updatePipeline(final String appName, final String originalPipelineName,
                                          final String updatedPipelineName) {
        try {
            String pipelineConfigJSONResponse = Pipelines.getPipelineConfigData(appName).asString();
            String pipelineID = fetchPipelineID(pipelineConfigJSONResponse, originalPipelineName);
            if(Objects.equals(pipelineID, "Invalid")) {
                LOGGER.error("updatePipeline() : Getting Invalid pipelineID, pipelineUpdate Failed");
                Response customResponse = (Response) with().response().statusCode(400);
                return customResponse;
            }

            URL = spinnakerURL + APIEndpoints.updatePipeline.replace("pipelineID", pipelineID);
            LOGGER.info("End point URL: " + URL);

            Pipeline pipeline = new CronTriggerPipelineBuilder().buildPipeline(appName, updatedPipelineName);
            pipeline.setId(pipelineID);
            payload = pipeline.toJson();
            LOGGER.info("Payload for executing pipeline :" + payload);

            //Adding delay before updating pipeline so that all spinnaker services got synced
            Thread.sleep(5000);

            Response response = requestSpec.body(payload).put(URL).then().extract().response();
            LOGGER.info("Response from updating pipeline : " + response.asString());
        } catch (Exception e) {
            LOGGER.error("UpdatePipeline Failed - " + e.getMessage());
        }
        return response;
    }

    /**
     * This method is used to fetch pipeline configuration details
     *
     * @return response
     * @throws Exception
     */
    public static Response getPipelineConfigData(final String appName) throws Exception {
        try {
            URL = spinnakerURL + APIEndpoints.getPipelineConfigs.replaceAll("spinnakerAppName", appName);
            LOGGER.info("End point URL to retrieve pipeline data: " + URL);

            response = requestSpec.get(URL);
            LOGGER.info("Response from getPipelineConfigData(): " + response.asString());
        } catch (Exception e) {
            LOGGER.error("getPipelineConfigData() Failed - " + e.getMessage());
        }
        return response;
    }

    private static String fetchPipelineID(final String jsonData, final String originalPipelineName) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(jsonData);
            for (JsonNode pipeline : root) {
                // Check if the "name" field of the pipeline matches the originalPipelineName
                if (pipeline.has("name") && pipeline.get("name").asText().equals(originalPipelineName)) {
                    // Retrieve "id" field
                    if (pipeline.has("id")) {
                        String pipelineId = pipeline.get("id").asText();
                        return pipelineId;
                    }
                }
            }
            // If the pipeline with the specified name is not found
            System.out.println("Pipeline with name '" + originalPipelineName + "' not found.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Invalid";
    }
}
