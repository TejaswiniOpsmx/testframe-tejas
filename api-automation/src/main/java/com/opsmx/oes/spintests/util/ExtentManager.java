package com.opsmx.oes.spintests.util;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.opsmx.oes.spintests.base.BaseClass;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtentManager extends BaseClass {
    public static final ExtentReports extentReports = new ExtentReports();

    public synchronized static ExtentReports createExtentReports() {
        ExtentSparkReporter reporter;

        // Define a timestamp format
        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

        // Get the current timestamp as a string
        String timestamp = timestampFormat.format(new Date());

        // Append the timestamp to the filename
        extentReportFilePath = System.getProperty("user.dir") + "/test-output/";
        extentReportFileName = "extentreport_" + timestamp + ".html";

        // Create the ExtentSparkReporter with the updated filename
        reporter = new ExtentSparkReporter(extentReportFilePath + extentReportFileName);

        reporter.config().setReportName("API Automation Report");
        extentReports.attachReporter(reporter);
        extentReports.setSystemInfo("Environment", "Test");
        return extentReports;
    }
}