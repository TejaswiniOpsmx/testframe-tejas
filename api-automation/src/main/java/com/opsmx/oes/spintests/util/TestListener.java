package com.opsmx.oes.spintests.util;

import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.opsmx.oes.spintests.base.BaseClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener extends BaseClass implements ITestListener {

	private static String getTestMethodName(ITestResult iTestResult) {
		return iTestResult.getMethod().getConstructorOrMethod().getName();
	}

	@Override
	public void onTestStart(ITestResult result) {
		LOGGER.info(getTestMethodName(result) + " test is starting.");
		LOGGER.info("------------------------------------------------");
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		LOGGER.info(getTestMethodName(result) + " test is succeed.");
		LOGGER.info("------------------------------------------------");
		//ExtentReports log operation for passed tests.
        ExtentTestManager.getTest().log(Status.PASS, "Test Passed");
        ExtentTestManager.getTest().log(Status.PASS, "Passed Test Case is:"+" "+result.getName());
	}

	@Override
	public void onTestFailure(ITestResult result) {
		LOGGER.info(getTestMethodName(result) + " test is failed.");
		LOGGER.info("------------------------------------------------");
		//ExtentReports log operations for failed tests.
    	ExtentTestManager.getTest().log(Status.FAIL, "Test Failed");
    	ExtentTestManager.getTest().log(Status.FAIL, MarkupHelper.createLabel("FAILED Test Case is:"+" "+result.getName(),ExtentColor.RED));
    	ExtentTestManager.getTest().log(Status.FAIL, MarkupHelper.createLabel("Testcase FAILED due to below issues:"+"",ExtentColor.RED));
    	ExtentTestManager.getTest().fail(result.getThrowable());
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		LOGGER.info(getTestMethodName(result) + " test is skipped.");
		LOGGER.info("------------------------------------------------");
		 //ExtentReports log operation for skipped tests.
    	ExtentTestManager.getTest().log(Status.SKIP, "Test Skipped");
    	ExtentTestManager.getTest().log(Status.SKIP, "Skipped Test Case is:"+" "+result.getName());
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		LOGGER.info("Test failed but it is in defined success ratio " + getTestMethodName(result));
		LOGGER.info("------------------------------------------------");
	}

	@Override
	public void onStart(ITestContext context) {
		 LOGGER.info("onStart method " + context.getName());

	}

	@Override
	public void onFinish(ITestContext context) {
		LOGGER.info("onFinish method " + context.getName());
		//Do tier down operations for ExtentReports reporting!
		ExtentManager.extentReports.flush();
	}

}
