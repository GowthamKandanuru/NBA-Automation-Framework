package com.veeva.framework.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

/**
 * DriverManager - Thread-safe WebDriver management via ThreadLocal.
 * Ensures each parallel test thread gets its own isolated WebDriver instance.
 */
public class DriverManager {

    private static final Logger log = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String>    browserThreadLocal = new ThreadLocal<>();

    private DriverManager() {}

    public static void initDriver(String browser) {
        browserThreadLocal.set(browser);
        if (driverThreadLocal.get() == null) {
            WebDriver driver = DriverFactory.createDriver(browser);
            driverThreadLocal.set(driver);
            log.info("WebDriver [{}] initialized for thread: {}", browser, Thread.currentThread().getName());
        }
    }

    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver not initialized. Call initDriver() first.");
        }
        return driver;
    }

    public static String getBrowser() {
        return browserThreadLocal.get();
    }

    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
            browserThreadLocal.remove();
            log.info("WebDriver quit for thread: {}", Thread.currentThread().getName());
        }
    }
}
