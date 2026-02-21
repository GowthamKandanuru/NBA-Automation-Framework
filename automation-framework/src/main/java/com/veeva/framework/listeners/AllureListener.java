package com.veeva.framework.listeners;

import com.veeva.framework.driver.DriverManager;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.*;

import java.io.ByteArrayInputStream;

/**
 * AllureListener - Integrates TestNG events with Allure reporting.
 * Automatically attaches screenshots on failure.
 * Register in testng.xml or via @Listeners annotation.
 */
public class AllureListener implements ITestListener, ISuiteListener {

    private static final Logger log = LogManager.getLogger(AllureListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        log.info("[TEST START] {}.{}", result.getTestClass().getName(), result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("[TEST PASS] {}", result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("[TEST FAIL] {} | Reason: {}", result.getName(),
                result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown");
        attachScreenshot(result.getName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("[TEST SKIP] {}", result.getName());
    }

    @Override
    public void onStart(ISuite suite) {
        log.info("========== SUITE START: {} ==========", suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("========== SUITE FINISH: {} ==========", suite.getName());
    }

    private void attachScreenshot(String testName) {
        try {
            WebDriver driver = DriverManager.getDriver();
           TakesScreenshot ts = (TakesScreenshot) driver;
                byte[] screenshot = ts.getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("Failure Screenshot - " + testName,
                        "image/png", new ByteArrayInputStream(screenshot), "png");

        } catch (Exception e) {
            log.warn("Screenshot capture failed: {}", e.getMessage());
        }
    }
}
