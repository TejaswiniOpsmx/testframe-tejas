package com.opsmx.oes.spintests.base;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;

import com.aventstack.extentreports.Status;
import com.opsmx.oes.common.util.ConfigDataProvider;
import com.opsmx.oes.spintests.util.ExtentTestManager;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.json.simple.parser.ParseException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseClass {

    public static Logger LOGGER = LogManager.getLogger(BaseClass.class);
    public static ConfigDataProvider config = new ConfigDataProvider(System.getProperty("user.dir")
            + "/../config.properties", System.getProperty("user.dir")
            + "/../config-overide.properties");

    public static String spinnakerAppName = null;
    public static String spinAuthToken = null;

    public static String spinUsername = config.getConfigData("username");
    public static String spinPassword = config.getConfigData("password");
    public static String spinnakerURL = config.getConfigData("url");
    public static String authn = config.getConfigData("authn");
    public static String extentReportFilePath = null;
    public static String extentReportFileName = null;
    public static String appLogFilePath = System.getProperty("user.dir");
    public static String appLogFileName = null;

    public static String URL;
    public static String payload;
    public static String responseData;

    public static RequestSpecification requestSpec;
    public static String timestamp;

    @BeforeSuite(alwaysRun = true)
    public void addTimestamp() {
        timestamp = String.valueOf(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());
        System.out.println("Current Date and Time in epoch: " + timestamp);
    }

    @BeforeSuite(alwaysRun = true)
    public static void spinnakerLogin() throws IOException, ParseException, InterruptedException {
        if("true".equals(authn)) {
            if (spinUsername == null || spinPassword == null || spinnakerURL == null) {
                LOGGER.info("spinnaker credentials not available");
            } else {
                LOGGER.info(spinnakerURL + " - " + spinUsername);
                String authUrl = spinnakerURL + "/login";
                HttpClient client = HttpClientBuilder.create().build();
                HttpPost post = new HttpPost(authUrl);
                List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                urlParameters.add(new BasicNameValuePair("username", spinUsername));
                urlParameters.add(new BasicNameValuePair("password", spinPassword));
                urlParameters.add(new BasicNameValuePair("submit", "Login"));
                try {
                    post.setEntity(new UrlEncodedFormEntity(urlParameters));
                    HttpResponse response = client.execute(post);
                    System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
                    if (response.getStatusLine().getStatusCode() == 302) {
                        Header[] headers = response.getAllHeaders();
                        Map<String, String> nameVsValue = new HashMap<>();
                        for (Header header : headers) {
                            nameVsValue.put(header.getName(), header.getValue());
                        }
                        String location = nameVsValue.get("Location");
                        String cookiesStr = nameVsValue.get("Set-Cookie");
                        if (!location.contains("error")) {
                            if (cookiesStr != null) {
                                cookiesStr = cookiesStr.trim();
                                if (cookiesStr.contains(";")) {
                                    String cookies[] = cookiesStr.split(";");
                                    for (String cookie : cookies) {
                                        if (cookie.contains("SESSION=")) {
                                            spinAuthToken = cookie;
                                            requestSpec = given().accept("application/json")
                                                    .headers("Cookie", spinAuthToken,
                                                            "Content-Type", "application/json",
                                                            "x-spinnaker-user", spinUsername);
                                            System.out.println("Session : " + spinAuthToken);
                                        }
                                    }
                                }
                            }
                        } else {
                            System.out.println("Authentication Failed");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            requestSpec = given().accept("application/json")
                    .headers("Content-Type", "application/json",
                            "x-spinnaker-user", spinUsername);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void addLogsToExtentReport() {
        if ((ExtentTestManager.getTest().getStatus().equals(Status.FAIL))) {
            ExtentTestManager.addToFailedReport(URL, payload, responseData);
        }
        if ((ExtentTestManager.getTest().getStatus().equals(Status.PASS))) {
            ExtentTestManager.addToPassedReport(URL, payload, responseData);
        }
        if ((ExtentTestManager.getTest().getStatus().equals(Status.SKIP))) {
            ExtentTestManager.addToSkippedReport(URL, payload, responseData);
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void clearExtentReportData() {
        ExtentTestManager.clearReportingData();
    }

    @AfterSuite(alwaysRun = false)
    public static void sendToS3() {
        String accessKey = config.getConfigData(Constants.AWS_ACCESS_KEY);
        String secretKey = config.getConfigData(Constants.AWS_SECRET_KEY);
        String bucketName = config.getConfigData(Constants.S3_BUCKET_TO_STORE_REPORT);

        // Initialize AWS credentials and S3 client
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3Client = AmazonS3Client.builder()
                .withRegion("us-west-2")
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        // Upload the report to S3
        System.out.println(extentReportFilePath);
        System.out.println(extentReportFileName);

        appLogFileName = getApplicationLogFileName();
        System.out.println(appLogFilePath);
        System.out.println(appLogFileName);

        uploadFileToS3(s3Client, bucketName, "reports/" + extentReportFileName,
                extentReportFilePath + extentReportFileName, "text/html");
        uploadFileToS3(s3Client, bucketName, appLogFileName,
                appLogFilePath + "/" + appLogFileName, "text/plain");

        makeS3ObjectToPublic(s3Client, bucketName, "reports/" + extentReportFileName);
        makeS3ObjectToPublic(s3Client, bucketName, appLogFileName);
    }

    private static void uploadFileToS3(final AmazonS3 s3Client, final String bucketName,
                                       final String objectKey, final String filePath, final String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);

            s3Client.putObject(new PutObjectRequest(bucketName, objectKey, new File(filePath))
                    .withMetadata(metadata));
            LOGGER.info("File uploaded to S3 successfully, Link: https://s3.console.aws.amazon.com/s3/object/" + bucketName +
                    "?region=us-west-2&prefix=" + objectKey);
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            LOGGER.error("Error uploading report to S3: " + e.getMessage());
        }
    }

    private static void makeS3ObjectToPublic(final AmazonS3 s3Client, final String bucketName,
                                             final String objectKey) {
        // Set the ACL to public-read (making the object public)
        try {
            s3Client.setObjectAcl(bucketName, objectKey, CannedAccessControlList.PublicRead);
            LOGGER.info("S3 object: " + objectKey + " is given public read access");
            LOGGER.info("Access Link: https://" + bucketName + ".s3.us-west-2.amazonaws.com/" + objectKey);
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            LOGGER.error("Error making object to public: " + e.getMessage());
        }
    }

    private static String getApplicationLogFileName() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);

        // Get the appender by name
        RollingFileAppender appender = (RollingFileAppender) context.getConfiguration()
                .getAppender("RollingAppender");

        // Get the file name from the appender
        String applicationLogfileName = appender.getFileName();
        return applicationLogfileName;
    }
}
