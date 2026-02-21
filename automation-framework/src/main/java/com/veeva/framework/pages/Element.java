package com.veeva.framework.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * Element - A lazy, self-resolving WebElement wrapper.
 *
 * Design goal:
 *   - Page Objects declare locators as {@code Element} fields using
 *     static factory methods (css / xpath / id / name etc.)
 *   - The {@code By} class is hidden entirely inside this wrapper.
 *   - Elements are resolved fresh from the DOM on every interaction,
 *     which naturally handles StaleElementReferenceException.
 *   - All waits are delegated to the caller (BasePage), keeping this
 *     class purely responsible for location + retrieval.
 *
 * Usage in a Page Object (no @FindBy, no By, no PageFactory):
 * <pre>
 *   private final Element shopMenu   = Element.css("nav a.shop-link");
 *   private final Element searchBox  = Element.id("search-input");
 *   private final Element loginBtn   = Element.xpath("//button[text()='Login']");
 * </pre>
 */
public final class Element {

    private final By locator;

    // ─── Private constructor ───────────────────────────────────────────────────

    private Element(By locator) {
        this.locator = locator;
    }

    // ─── Static factory methods (Page Objects use ONLY these) ─────────────────

    public static Element css(String selector) {
        return new Element(By.cssSelector(selector));
    }

    public static Element xpath(String expression) {
        return new Element(By.xpath(expression));
    }

    public static Element id(String id) {
        return new Element(By.id(id));
    }

    public static Element name(String name) {
        return new Element(By.name(name));
    }

    public static Element linkText(String text) {
        return new Element(By.linkText(text));
    }

    public static Element partialLinkText(String text) {
        return new Element(By.partialLinkText(text));
    }

    public static Element tagName(String tag) {
        return new Element(By.tagName(tag));
    }

    // ─── Resolution methods ───────────────────────────────────────────────────

    /**
     * Resolves the element immediately from the driver (no wait).
     * Prefer {@link #waitUntilVisible(WebDriver, WebDriverWait)} for interactions.
     */
    public WebElement find(WebDriver driver) {
        return driver.findElement(locator);
    }

    /**
     * Resolves all matching elements immediately from the driver (no wait).
     */
    public List<WebElement> findAll(WebDriver driver) {
        return driver.findElements(locator);
    }

    /**
     * Waits until the element is visible in the DOM, then returns it.
     */
    public WebElement waitUntilVisible(WebDriver driver, WebDriverWait wait) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits until the element is clickable, then returns it.
     */
    public WebElement waitUntilClickable(WebDriver driver, WebDriverWait wait) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    /**
     * Waits until all matching elements are visible, then returns the list.
     */
    public List<WebElement> waitAllVisible(WebDriver driver, WebDriverWait wait) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    /**
     * Waits until at least one matching element is present in the DOM.
     */
    public List<WebElement> waitPresence(WebDriver driver, WebDriverWait wait) {
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    /**
     * Returns true if at least one element matching the locator exists in the DOM.
     */
    public boolean isPresent(WebDriver driver) {
        return !driver.findElements(locator).isEmpty();
    }

    /**
     * Returns true if the located element is currently displayed.
     */
    public boolean isDisplayed(WebDriver driver) {
        try {
            return find(driver).isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException |
                 org.openqa.selenium.StaleElementReferenceException e) {
            return false;
        }
    }

    /**
     * Exposes the underlying By only for internal framework use
     * (e.g., ExpectedConditions in BasePage). Never call this in Page Objects.
     *
     * @return the encapsulated {@link By} locator
     */
    By getLocator() {
        return locator;
    }

    @Override
    public String toString() {
        return String.valueOf(locator);
    }
}
