package com.veeva.framework.utils;

import com.veeva.framework.driver.DriverManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * BaseTest - Parent class for all test classes.
 *
 * Parallel browser fix:
 *   - @BeforeTest receives the browser parameter from testng.xml and passes it
 *     directly to DriverManager.initDriver(browser) — NO System.setProperty().
 *   - The browser is stored in DriverManager.browserThreadLocal so each thread
 *     keeps its own isolated value throughout the test run.
 *   - @AfterTest quits the driver (matches @BeforeTest scope — one driver per
 *     <test> block, not one per method).
 *   - @BeforeMethod / @AfterMethod handle per-method concerns only
 *     (SoftAssert reset, logging, screenshots).
 */
public class BaseTest {

    protected final Logger log = LogManager.getLogger(getClass());
    public SoftAssert softAssert;

    // ─── Driver lifecycle: one driver per <test> block ────────────────────────

    @BeforeTest(alwaysRun = true)
    @Parameters({"browser"})
    public void setUpTest(@Optional("firefox") String browser, ITestContext context) {
        log.info("========== Starting Test Block: {} | Browser: {} ==========",
                context.getName(), browser);
        // Pass browser directly — never via System.setProperty()
        // Each parallel thread stores its own browser in ThreadLocal
        DriverManager.initDriver(browser);
    }

    @AfterTest(alwaysRun = true)
    public void tearDownTest(ITestContext context) {
        log.info("========== Finished Test Block: {} ==========", context.getName());
        DriverManager.quitDriver();
    }

    // ─── Per-method lifecycle: SoftAssert + screenshot ────────────────────────

    @BeforeMethod(alwaysRun = true)
    public void setUpMethod(Method method) {
        log.info("---------- Starting Method: {} ----------", method.getName());
        softAssert = new SoftAssert();   // fresh SoftAssert for every @Test method
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownMethod(ITestResult result) {
        String status;
        switch (result.getStatus()) {
            case ITestResult.SUCCESS: status = "PASSED";  break;
            case ITestResult.FAILURE: status = "FAILED";  break;
            case ITestResult.SKIP:    status = "SKIPPED"; break;
            default:                  status = "UNKNOWN"; break;
        }
        log.info("---------- Method: {} | Status: {} ----------", result.getName(), status);

        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshotOnFailure(result.getName());
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    protected WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    @Step("Capturing screenshot on failure: {testName}")
    private void captureScreenshotOnFailure(String testName) {
        try {
            byte[] screenshot = ((org.openqa.selenium.TakesScreenshot) DriverManager.getDriver())
                    .getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
            Allure.getLifecycle().addAttachment(
                    "Screenshot on Failure - " + testName,
                    "image/png", "png", screenshot
            );
            log.info("Screenshot captured for failed test: {}", testName);
        } catch (Exception e) {
            log.warn("Could not capture screenshot: {}", e.getMessage());
        }
    }

    protected void attachFileToReport(String name, String filePath, String mimeType) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Allure.addAttachment(name, mimeType, Files.newInputStream(path), "");
                log.info("Attached file to report: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("Could not attach file to report: {}", e.getMessage());
        }
    }

    protected String ensureOutputDir(String subDir) {
        String dir = "target/test-outputs/" + subDir;
        new File(dir).mkdirs();
        return dir;
    }
}
