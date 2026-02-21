package com.veeva.framework.driver;

import com.veeva.framework.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * DriverFactory - Responsible for dynamic WebDriver creation.
 * Uses WebDriverManager for automatic binary management.
 * Supports Chrome, Firefox, Edge with headless mode option.
 */
public class DriverFactory {

    private static final Logger log = LogManager.getLogger(DriverFactory.class);

    private DriverFactory() {
        // Utility class, no instantiation
    }

    /**
     * Creates a WebDriver instance based on the browser config.
     * Browser is read from config/env, defaulting to Chrome.
     */
    public static WebDriver createDriver(String browser) {
        boolean headless = Boolean.parseBoolean(ConfigManager.get("headless", "false"));
        boolean remote   = Boolean.parseBoolean(ConfigManager.get("remote", "false"));
        String  hubUrl   = ConfigManager.get("hub_url", "");

        String resolvedBrowser = (browser != null && !browser.isEmpty())
                ? browser.toLowerCase().trim()
                : ConfigManager.get("browser", "chrome").toLowerCase().trim();

        log.info("Initializing WebDriver | Browser: {} | Headless: {} | Remote: {}",
                resolvedBrowser, headless, remote);

        if (remote) {
            return createRemoteDriver(resolvedBrowser, hubUrl, headless);
        }

        switch (resolvedBrowser) {
            case "firefox": return createFirefoxDriver(headless);
            case "edge":    return createEdgeDriver(headless);
            default:        return createChromeDriver(headless);
        }
    }

    private static WebDriver createChromeDriver(boolean headless) {
       /* WebDriverManager.chromedriver().setup();*/
        /*ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu",
                "--window-size=1920,1080", "--disable-extensions", "--disable-blink-features=AutomationControlled","--disable-notifications",
                "-remote-allow-origins=*");
        if (headless) options.addArguments("--headless=new");*/
        WebDriver driver = new ChromeDriver(setChromeOptions(headless));
        configureDriver(driver);
        return driver;
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        /*WebDriverManager.firefoxdriver().setup();*/
        /*FirefoxOptions options = new FirefoxOptions();
        if (headless) options.addArguments("--headless");*/
        WebDriver driver = new FirefoxDriver(setFirefoxOptions(headless));
        configureDriver(driver);
        return driver;
    }

    private static WebDriver createEdgeDriver(boolean headless) {
        /*WebDriverManager.edgedriver().setup();*/
        /*EdgeOptions options = new EdgeOptions();
        if (headless) options.addArguments("--headless=new");
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--disable-extensions",
                "--disable-blink-features=AutomationControlled",
                "--disable-notifications",
                "--remote-allow-origins=*"
        );*/
        WebDriver driver = new EdgeDriver(setEdgeOptions(headless));
        configureDriver(driver);
        return driver;
    }

    private static void configureDriver(WebDriver driver) {
        int implicitWait = Integer.parseInt(ConfigManager.get("implicit.wait", "10"));
        int pageLoadTimeout = Integer.parseInt(ConfigManager.get("page.load.timeout", "30"));
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        log.info("WebDriver configured | ImplicitWait: {}s | PageLoad: {}s", implicitWait, pageLoadTimeout);
    }

    private static WebDriver createRemoteDriver(String browser,String hubUrl,boolean headless) {
        WebDriver driver =null;
        try {
            switch (browser) {
                case "chrome":
                 driver = new RemoteWebDriver(new URL(hubUrl), setChromeOptions(headless));
                configureDriver(driver);
                return driver;
                case "edge":
                     driver = new RemoteWebDriver(new URL(hubUrl), setEdgeOptions(headless));
                    configureDriver(driver);
                    return driver;
                default:
                     driver = new RemoteWebDriver(new URL(hubUrl), setFirefoxOptions(headless));
                    configureDriver(driver);
                    return driver;
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static ChromeOptions setChromeOptions(boolean headless)
    {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu",
                "--disable-extensions", "--disable-blink-features=AutomationControlled","--disable-notifications",
                "-remote-allow-origins=*","--disable-blink-features=AutomationControlled");
        if (headless) options.addArguments("--headless=new");
        options.setCapability("browserName", "chrome");
        options.setBrowserVersion("128.0");
        Map<String,Object> selenoidOptions = new HashMap<>();
        selenoidOptions.put("screenResolution","1920x1080x24");
        selenoidOptions.put("enableVNC", true);
        options.setCapability("selenoid:options", selenoidOptions);
        return options;
    }

    private static EdgeOptions setEdgeOptions(boolean headless)
    {
        EdgeOptions options = new EdgeOptions();
        if (headless) options.addArguments("--headless=new");
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--disable-extensions",
                "--disable-blink-features=AutomationControlled",
                "--disable-notifications",
                "--remote-allow-origins=*"
        );
       /* options.setCapability("browserName", "edge");
        options.setBrowserVersion("128.0");
        Map<String,Object> selenoidOptions = new HashMap<>();
        selenoidOptions.put("screenResolution","1280x1024x24");
        selenoidOptions.put("enableVNC", true);
        options.setCapability("selenoid:options", selenoidOptions);*/
        return options;
    }

    private static FirefoxOptions setFirefoxOptions(boolean headless)
    {
        FirefoxOptions options = new FirefoxOptions();
        // Equivalent of --headless=new in Chrome
        if (headless) options.addArguments("--headless");
      //  options.addArguments("--width=1920", "--height=1080");
        // Equivalent of --disable-notifications
        options.addPreference("dom.webnotifications.enabled", false);
        options.addPreference("dom.push.enabled", false);
        // Equivalent of --disable-extensions
        options.addPreference("extensions.enabled", false);
        // Equivalent of --disable-blink-features=AutomationControlled
        // Removes navigator.webdriver=true flag to avoid bot detection
        options.addPreference("dom.webdriver.enabled", false);
        options.addPreference("useAutomationExtension", false);
        // Selenoid capabilities — identical to Chrome/Edge
        options.setCapability("browserName", "firefox");
        options.setBrowserVersion("125.0");
        Map<String,Object> selenoidOptions = new HashMap<>();
        selenoidOptions.put("screenResolution","1024x768x24");
        selenoidOptions.put("enableVNC", true);
        options.setCapability("selenoid:options", selenoidOptions);
        return options;
    }
}
