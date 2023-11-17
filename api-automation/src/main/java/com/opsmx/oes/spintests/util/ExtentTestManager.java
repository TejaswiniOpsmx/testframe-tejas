package com.opsmx.oes.spintests.util;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.opsmx.oes.spintests.base.BaseClass;

import java.util.HashMap;
import java.util.Map;

public class ExtentTestManager {

    static Map<Integer, ExtentTest> extentTestMap = new HashMap<>();
    static ExtentReports extent = ExtentManager.createExtentReports();

    public static synchronized ExtentTest getTest() {
        return extentTestMap.get((int) Thread.currentThread().getId());
    }

    public static synchronized ExtentTest startTest(String testName, String desc) {
        ExtentTest test = extent.createTest(testName, desc);
        extentTestMap.put((int) Thread.currentThread().getId(), test);
        return test;
    }

    public static void addToFailedReport(String url, String payload, String response) {
        getTest().fail("End Point URL:");
        getTest().fail(url);
        getTest().fail("Request JSON:");
        getTest().fail(payload);
        getTest().fail("Response JSON:");
        getTest().fail(response);
        clearReportingData();
    }

    public static void addToPassedReport(String url, String payload, String response) {
        getTest().pass("End Point URL: " + url);
        getTest().pass("Request JSON: ");
        getTest().pass(payload);
        getTest().pass("Response JSON: ");
        getTest().pass(response);
        clearReportingData();
    }

    public static void addToSkippedReport(String url, String payload, String response) {
        getTest().skip("End Point URL:");
        getTest().skip(url);
        getTest().skip("Request JSON:");
        getTest().skip(payload);
        getTest().skip("Response JSON:");
        getTest().skip(response);
        clearReportingData();
    }

    public static void clearReportingData() {
        //empty the reporting variables
        BaseClass.URL = "";
        BaseClass.payload = "";
        BaseClass.responseData = "";
    }
}