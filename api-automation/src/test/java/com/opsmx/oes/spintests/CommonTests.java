package com.opsmx.oes.spintests;

import com.opsmx.oes.common.util.ConfigDataProvider;
import com.opsmx.oes.spintests.base.BaseClass;
import com.opsmx.oes.spintests.base.Constants;
import com.opsmx.oes.spintests.service.Application;
import com.opsmx.oes.spintests.util.ExtentTestManager;
import com.opsmx.oes.spintests.service.Pipelines;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class CommonTests extends BaseClass {

    private Response response;
    private final ConfigDataProvider config = new ConfigDataProvider(System.getProperty("user.dir")
            + "/../config.properties", System.getProperty("user.dir")
            + "/../config-overide.properties");

    /**
     * This TC creates spinnaker application
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY, Constants.TEST_GROUP_APPLICATION}, priority = 0)
    public void createApplication(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "createApplication");
        String appName = config.getConfigData("appName") + timestamp;
        String appDescription = config.getConfigData("appDescription");
        String appEmail = config.getConfigData("appEmail");
        response = Application.createApplication(appName, appDescription, appEmail);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
        Thread.sleep(10000);
    }

    /**
     * This TC adds Email & Slack notifications at Application level
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY, Constants.TEST_GROUP_EMAIL, Constants.TEST_GROUP_SLACK},
            dependsOnMethods = "createApplication", priority = 1)
    public void addEmailAndSlackNotificationForApplication(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "addEmailAndSlackNotificationForApplication");
        String appName = config.getConfigData("appName") + timestamp;
        String emailNotificationAddress = config.getConfigData("emailNotificationAddress");
        String slackNotificationAddress = config.getConfigData("slackNotificationAddress");
        response = Application.addEmailAndSlackNotificationToApplication(appName, emailNotificationAddress,
                slackNotificationAddress);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    /**
     * This TC creates pipeline with wait stage with Cron trigger
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY, Constants.TEST_GROUP_PIPELINE, Constants.TEST_GROUP_CRON},
            dependsOnMethods = "createApplication", priority = 2)
    public void createPipelineWithCronTrigger(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "createPipelineWithCronTrigger");
        String appName = config.getConfigData("appName") + timestamp;
        String pipelineName = config.getConfigData("cronPipelineName");
        response = Pipelines.createPipeline("cronTrigger", appName, pipelineName);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    /**
     * This TC creates pipeline with DeployManifestStage, ManualJudgementStage, DeleteManifestStage
     * with Email & Slack Notifications at pipeline & stage levels with Webhook Trigger
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY, Constants.TEST_GROUP_PIPELINE, Constants.TEST_GROUP_EMAIL,
            Constants.TEST_GROUP_SLACK, Constants.TEST_GROUP_WEBHOOK}, dependsOnMethods = "createApplication",
            priority = 3)
    public void createPipelineWithDeployStage(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "createPipelineWithDeployStage");
        String appName = config.getConfigData("appName") + timestamp;
        String pipelineName = config.getConfigData("deployPipelineName");
        response = Pipelines.createPipeline("deploy", appName, pipelineName);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    /**
     * This TC creates pipeline with Wait stage with Jenkins Trigger
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY, Constants.TEST_GROUP_PIPELINE, Constants.TEST_GROUP_JENKINS},
            dependsOnMethods = "createApplication", priority = 4)
    public void createPipelineWithJenkinsTrigger(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "createPipelineWithJenkinsTrigger");
        String appName = config.getConfigData("appName") + timestamp;
        String pipelineName = config.getConfigData("jenkinsTriggerPipelineName");
        response = Pipelines.createPipeline("jenkinsTrigger", appName, pipelineName);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    /**
     * This TC creates pipeline with Wait stage with Git Trigger
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY, Constants.TEST_GROUP_PIPELINE, Constants.TEST_GROUP_GIT},
            dependsOnMethods = "createApplication", priority = 5)
    public void createPipelineWithGitTrigger(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "createPipelineWithGitTrigger");
        String appName = config.getConfigData("appName") + timestamp;
        String pipelineName = config.getConfigData("gitTriggerPipelineName");
        response = Pipelines.createPipeline("gitTrigger", appName, pipelineName);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    /**
     * This TC creates pipeline with Wait stage with DockerRegistry Trigger
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY, Constants.TEST_GROUP_PIPELINE, Constants.TEST_GROUP_DOCKER_REGISTRY},
            dependsOnMethods = "createApplication", priority = 6)
    public void createPipelineWithDockerRegistryTrigger(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "createPipelineWithDeployStage");
        String appName = config.getConfigData("appName") + timestamp;
        String pipelineName = config.getConfigData("dockerRegistryTriggerPipelineName");
        response = Pipelines.createPipeline("dockerRegistryTrigger", appName, pipelineName);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    /**
     * This TC creates pipeline with JenkinsBuild, DeployManifest, DeleteManifest Stages
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY, Constants.TEST_GROUP_PIPELINE, Constants.TEST_GROUP_JENKINS},
            dependsOnMethods = "createApplication", priority = 7)
    public void createPipelineWithJenkinsBuildAndDeploy(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "createPipelineWithJenkinsBuildAndDeploy");
        String appName = config.getConfigData("appName") + timestamp;
        String pipelineName = config.getConfigData("jenkinsBuildAndDeployPipelineName");
        response = Pipelines.createPipeline("jenkins", appName, pipelineName);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    /**
     * This TC creates helm pipeline with Bake, DeployManifest, Wait, DeleteManifest Stages
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY, Constants.TEST_GROUP_PIPELINE, Constants.TEST_GROUP_HELM},
            dependsOnMethods = "createApplication", priority = 8)
    public void createPipelineWithHelmDeployment(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "createPipelineWithHelmDeployment");
        String appName = config.getConfigData("appName") + timestamp;
        String pipelineName = config.getConfigData("helmDeploymentPipelineName");
        response = Pipelines.createPipeline("helm", appName, pipelineName);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    /**
     * This TC creates kustomize pipeline with Bake, DeployManifest, Wait, DeleteManifest Stages
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY, Constants.TEST_GROUP_PIPELINE, Constants.TEST_GROUP_KUSTOMIZE},
            dependsOnMethods = "createApplication", priority = 9)
    public void createPipelineWithKustomizeDeployment(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "createPipelineWithKustomizeDeployment");
        String appName = config.getConfigData("appName") + timestamp;
        String pipelineName = config.getConfigData("kustomizeDeploymentPipelineName");
        response = Pipelines.createPipeline("kustomize", appName, pipelineName);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    /**
     * This TC creates pipeline with Bake, DeployManifest, Wait, DeleteManifest Stages
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY, Constants.TEST_GROUP_PIPELINE, Constants.TEST_GROUP_EC2},
            dependsOnMethods = "createApplication", priority = 10)
    public void createPipelineWithEC2Deployment(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "createPipelineWithEC2Deployment");
        String appName = config.getConfigData("appName") + timestamp;
        String pipelineName = config.getConfigData("ec2DeploymentPipelineName");
        response = Pipelines.createPipeline("ec2", appName, pipelineName);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    /**
     * This TC create ecs pipeline with findimagefromtag, deploy and destroyservergroup stages
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY, Constants.TEST_GROUP_PIPELINE, Constants.TEST_GROUP_ECS},
            dependsOnMethods = "createApplication", priority = 11)
    public void createPipelineWithECSDeployment(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "createPipelineWithECSDeployment");
        String appName = config.getConfigData("appName") + timestamp;
        String pipelineName = config.getConfigData("ecsDeploymentPipelineName");
        response = Pipelines.createPipeline(Constants.ECS, appName, pipelineName);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }
    /**
     * This TC execute pipelines
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY}, dependsOnMethods = "createApplication",
            priority = 12)
    public void executePipeline(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "executePipeline");
        String appName = config.getConfigData("appName") + timestamp;
        response = Pipelines.executePipeline(appName);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    /**
     * This TC updates Cron pipeline with new name
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY, Constants.TEST_GROUP_CRON},
            dependsOnMethods = "createPipelineWithCronTrigger", priority = 13)
    public void updateCronPipeline(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "updateCronPipeline");
        String appName = config.getConfigData("appName") + timestamp;
        String originalPipelineName = config.getConfigData("cronPipelineName");
        String updatedPipelineName = config.getConfigData("updatedCronPipelineName");
        response = Pipelines.updatePipeline(appName, originalPipelineName, updatedPipelineName);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    /**
     * This TC updates application with new application description
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY}, dependsOnMethods = "createApplication", priority = 14)
    public void updateApplication(Method m) throws Exception {
        ExtentTestManager.startTest(m.getName(), "updateApplication");
        String appName = config.getConfigData("appName") + timestamp;
        String updatedAppDescription = config.getConfigData("updatedAppDescription");
        response = Application.updateApplication(appName, updatedAppDescription);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    /**
     * This TC deletes spinnaker application
     */
    @Test(groups = {Constants.TEST_GROUP_SANITY}, dependsOnMethods = "createApplication", priority = 15)
    public void deleteSpinnakerApplication(Method m) throws Exception {
        String appName = config.getConfigData("appName") + timestamp;
        ExtentTestManager.startTest(m.getName(), "deleteSpinnakerApplication");
        response = Application.deleteApplication(appName);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }
}
