package com.veeva.framework.pages;

import com.veeva.framework.config.ConfigManager;
import com.veeva.framework.driver.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * BasePage - Foundation for all Page Objects.
 *
 * Design:
 *   - No @FindBy annotations, no PageFactory, no direct By usage in subclasses.
 *   - Subclasses declare locators as {@link Element} fields using the static
 *     factory methods: Element.css(), Element.xpath(), Element.id(), etc.
 *   - All Selenium interactions go through the protected methods below,
 *     which accept {@link Element} and resolve them with explicit waits.
 *
 * Example in a Page Object:
 * <pre>
 *   private final Element loginBtn  = Element.css("button.login");
 *   private final Element username  = Element.id("username");
 *   private final Element navLinks  = Element.css("nav a");
 *
 *   public void login(String user) {
 *       type(username, user);
 *       click(loginBtn);
 *   }
 * </pre>
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Actions actions;
    //private boolean initialized = false;

    public BasePage() {
        this.driver = DriverManager.getDriver();
        int explicitWait = Integer.parseInt(ConfigManager.get("explicit.wait", "70"));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
        this.actions = new Actions(driver);
    }
    //public BasePage() { }   // safe — nothing runs at instantiation time

   /* private void init() {
        if (!initialized) {
            this.driver  = DriverManager.getDriver();      // same line
            int explicitWait = Integer.parseInt(ConfigManager.get("explicit.wait", "20")); // same line
            this.wait    = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));    // same line
            this.actions = new Actions(driver);            // same line
            initialized  = true;
        }
    }*/

   /* private void ensureInitialized() {
        if (!initialized) init();
    }*/

    // ─── Navigation ───────────────────────────────────────────────────────────

    public void navigateTo(String url) {
        log.info("Navigating to: {}", url);
        driver.get(url);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getTitle() {
        return driver.getTitle();
    }

    // ─── Single Element Interactions (accept Element wrapper) ─────────────────

    /**
     * Waits until the element is clickable then clicks it.
     */
    protected void click(Element element) {
     //   ensureInitialized();
        log.debug("Clicking: {}", element);
        element.waitUntilVisible(driver, wait).click();
       // element.waitUntilClickable(driver, wait).click();
    }

    /**
     * Waits until visible, clears the field, then types the given text.
     */
    protected void type(Element element, String text) {
        log.debug("Typing '{}' into: {}", text, element);
        WebElement el = element.waitUntilVisible(driver, wait);
        el.clear();
        el.sendKeys(text);
    }

    /**
     * Waits until visible and returns the element's trimmed text.
     */
    protected String getText(Element element) {
        return element.waitUntilVisible(driver, wait).getText().trim();
    }

    /**
     * Waits until visible and returns the value of the given attribute.
     */
    protected String getAttribute(Element element, String attribute) {
        return element.waitUntilVisible(driver, wait).getAttribute(attribute);
    }

    /**
     * Moves the mouse over the element (hover).
     */
    protected void hover(Element element) {
        log.debug("Hovering over: {}", element);
        WebElement el = element.waitUntilVisible(driver, wait);
        actions.moveToElement(el).perform();
    }

    public boolean waitForTitleIs(String title)
    {
        return wait.until(ExpectedConditions.titleIs(title));
    }
    /**
     * Hovers over {@code hoverTarget} then clicks {@code clickTarget}.
     */
    protected void hoverThenClick(Element hoverTarget, Element clickTarget) {
        log.debug("Hover {} → click {}", hoverTarget, clickTarget);
        actions.moveToElement(hoverTarget.waitUntilVisible(driver, wait)).perform();
        clickTarget.waitUntilClickable(driver, wait).click();
    }

    /**
     * Scrolls the element into the viewport using JavaScript.
     */
    protected void scrollToElement(Element element) {
        WebElement el = element.find(driver);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", el);
    }

    /**
     * Clicks an element via JavaScript (useful when overlays block normal click).
     */
    protected void jsClick(Element element) {
        log.debug("JS click: {}", element);
        WebElement el = element.waitUntilVisible(driver, wait);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    /**
     * Returns true if the element is present and visible on the page.
     */
    protected boolean isDisplayed(Element element) {
        return element.isDisplayed(driver);
    }
    /**
     * Waits until the given text is present inside the element.
     */
    protected boolean waitForText(Element element, String text) {
        return wait.until(
                ExpectedConditions.textToBePresentInElementLocated(element.getLocator(), text));
    }

    // ─── Multi-Element Interactions (accept Element wrapper) ──────────────────

    /**
     * Waits until all elements matching the locator are visible, returns the list.
     */
    protected List<WebElement> findAllVisible(Element element) {
        return element.waitAllVisible(driver, wait);
    }

    /**
     * Finds all elements matching the locator (no wait, may be empty).
     */
    protected List<WebElement> findAll(Element element) {
        return element.findAll(driver);
    }

    protected WebElement find(Element element)
    {
        return element.find(driver);
    }

    /**
     * Returns the count of elements matching the locator currently in the DOM.
     */
    protected int count(Element element) {
        return element.findAll(driver).size();
    }

    // ─── Direct WebElement Utilities (for when a raw element is already held) ─

    /**
     * Scrolls a raw WebElement into view (used when iterating a collected list).
     */
    protected void scrollToWebElement(WebElement webElement) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", webElement);
    }

    // ─── Page-level Utilities ─────────────────────────────────────────────────

    protected void scrollToBottom() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    protected void scrollToTop() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
    }

    public byte[] takeScreenshot() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    public String switchToWindow()
    {
        String parent = driver.getWindowHandle();
        Set<String> openedWindows = driver.getWindowHandles();
        Iterator<String> it = openedWindows.iterator();
        while(it.hasNext())
        {
            String currentWindow = it.next();
            if(!currentWindow.equals(parent))
            {
                driver.switchTo().window(currentWindow);
                break;
            }
        }
        return driver.getTitle();
    }

    public String getLocatorAsString(Element element)
    {
        return toString();
    }
    public void dismissCookieBannerIfPresent(Element cookiesAcceptBtn) {
        try {
            click(cookiesAcceptBtn);
            log.info("Cookie banner dismissed");
        } catch (Exception e) {
            log.debug("No cookie banner found or already dismissed");
        }
    }
}
