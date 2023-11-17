package com.opsmx.oes.spintests.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsmx.oes.examples.ApplicationBuilder;
import com.opsmx.oes.spintests.base.APIEndpoints;
import com.opsmx.oes.spintests.base.BaseClass;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application extends BaseClass {

    private static Response response;

    /**
     * This method is used to create application in Spinnaker
     *
     * @return response
     * @throws Exception
     */
    public static Response createApplication(final String appName, final String appDescription,
                                             final String appEmail) throws Exception {
        try {
            payload = new ApplicationBuilder().buildApplication(appName, appDescription, appEmail).toJson();
            LOGGER.info("Payload to create application in Spinnaker: " + payload);

            URL = spinnakerURL + APIEndpoints.createApplicationInSpinnaker;
            LOGGER.info("End point URL to create Application: " + URL);

            response = requestSpec.body(payload).post(URL).then().extract().response();
            responseData = response.asString();
            LOGGER.info("Response received from Application creation API:" + response.asString());
        } catch (Exception e) {
            LOGGER.error("Create Application Failed - " + e.getMessage());
        }
        return response;
    }

    /**
     * This method is used to add email/slack notification to application
     *
     * @return response
     * @throws Exception
     */
    public static Response addEmailAndSlackNotificationToApplication(final String appName,
                                                                     final String emailNotificationAddress,
                                                                     final String slackNotificationAddress)
            throws Exception {
        try {
            payload = generateEmailAndSlackNotificationForApplication(appName, emailNotificationAddress,
                    slackNotificationAddress);
            LOGGER.info("Payload for addEmailAndSlackNotificationToApplication():" + payload);

            URL = spinnakerURL + APIEndpoints.addNotificationToApplication.replace("spinnakerAppName", appName);
            LOGGER.info("End point URL: " + URL);

            response = requestSpec.body(payload).post(URL).thenReturn();
            responseData = response.asString();
            LOGGER.info("Response from addEmailAndSlackNotificationToApplication(): " + responseData);

        } catch (Exception e) {
            LOGGER.error("addNotificationToApplication Failed - " + e.getMessage());
        }
        return response;
    }

    /**
     * This method is used to update an application in Spinnaker
     *
     * @return deleteAppResponse
     * @throws Exception
     */
    public static Response updateApplication(final String appName, final String updatedAppDescription) throws Exception {
        try {
            payload = new ApplicationBuilder().updateApplication(appName, updatedAppDescription).toJson();
            LOGGER.info("Payload to Update application in Spinnaker: " + payload);

            URL = spinnakerURL + APIEndpoints.updateApplicationInSpinnaker;
            LOGGER.info("End point URL: " + URL);

            //Adding delay before updating application so that all spinnaker services got synced
            Thread.sleep(5000);

            response = requestSpec.body(payload).post(URL).then().contentType(ContentType.JSON).extract().response();
            responseData = response.asString();
            LOGGER.info("Response received from Spinnaker Application Deletion API :" + responseData);

        } catch (Exception e) {
            LOGGER.error("Delete Application Failed - " + e.getMessage());
        }
        return response;
    }

    /**
     * This method is used to delete an application in Spinnaker
     *
     * @return deleteAppResponse
     * @throws Exception
     */
    public static Response deleteApplication(final String appName) throws Exception {
        try {
            payload = new ApplicationBuilder().deleteApplication(appName).toJson();
            LOGGER.info("Payload to Delete application in Spinnaker: " + payload);

            URL = spinnakerURL + APIEndpoints.deleteApplicationInSpinnaker;
            LOGGER.info("End point URL: " + URL);

            //Adding delay before deleting application so that all spinnaker services got synced
            Thread.sleep(30000);

            response = requestSpec.body(payload).post(URL).then().contentType(ContentType.JSON).extract().response();
            responseData = response.asString();
            LOGGER.info("Response received from Spinnaker Application Deletion API :" + responseData);

        } catch (Exception e) {
            LOGGER.error("Delete Application Failed - " + e.getMessage());
        }
        return response;
    }

    private static String generateEmailAndSlackNotificationForApplication(final String appName,
                                                                          final String emailNotificationAddress,
                                                                          final String slackNotificationAddress)
            throws JsonProcessingException {
        List<Map<String, Object>> emailMsgList = new ArrayList<>();
        List<Map<String, Object>> slackMsgList = new ArrayList<>();
        Map<String, Object> emailEntry = new HashMap<>();
        Map<String, Object> slackEntry = new HashMap<>();

        List<String> whenList = new ArrayList<>();
        whenList.add("pipeline.starting");
        whenList.add("pipeline.complete");
        whenList.add("pipeline.failed");

        emailEntry.put("level", "application");
        emailEntry.put("when", whenList);
        emailEntry.put("type", "email");
        emailEntry.put("address", emailNotificationAddress);

        slackEntry.put("level", "application");
        slackEntry.put("when", whenList);
        slackEntry.put("type", "slack");
        slackEntry.put("address", slackNotificationAddress);

        // Create a Map to represent the "message" entry
        Map<String, Map<String, String>> messageMap = new HashMap<>();

        // Create a Map entry for "pipeline.starting"
        Map<String, String> startingMessage = new HashMap<>();
        startingMessage.put("text", "pipeline is starting");
        messageMap.put("pipeline.starting", startingMessage);

        // Create a Map entry for "pipeline.complete"
        Map<String, String> completeMessage = new HashMap<>();
        completeMessage.put("text", "pipeline completed");
        messageMap.put("pipeline.complete", completeMessage);

        // Create a Map entry for "pipeline.failed"
        Map<String, String> failedMessage = new HashMap<>();
        failedMessage.put("text", "pipeline failed");
        messageMap.put("pipeline.failed", failedMessage);

        slackEntry.put("message", messageMap);
        emailEntry.put("message", messageMap);
        emailMsgList.add(emailEntry);
        slackMsgList.add(slackEntry);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("application", appName);
        dataMap.put("email", emailMsgList);
        dataMap.put("slack", slackMsgList);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = null;
        try {
            // Convert the Map to a JSON string
            jsonString = objectMapper.writeValueAsString(dataMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
}
