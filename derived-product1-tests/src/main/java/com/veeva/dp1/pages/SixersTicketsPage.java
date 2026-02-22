package com.veeva.dp1.pages;

import com.veeva.framework.config.ConfigManager;
import com.veeva.framework.pages.BasePage;
import com.veeva.framework.pages.Element;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * SixersTicketsPage - Page Object for TC3.
 *
 * Sits in src/main/java as a reusable page component.
 * Locators declared as {@link Element} fields — no @FindBy, no PageFactory, no raw By.
 * TC3: Count ticket carousel slides, capture titles, validate slide durations.
 */
public class SixersTicketsPage extends BasePage {

    private final String baseUrl = ConfigManager.get("urls.derived-product1", "https://www.nba.com/sixers/");

    // ─── Locators ─────────────────────────────────────────────────────────────

    private final Element carouselSlides       = Element.xpath(
            "//div[contains(@class,'TileHeroStories_tileHeroStoriesButtons')]/button");
    private final Element slideTitles          = Element.xpath(
            "//div[contains(@class,'TileHeroStories_tileHeroStoriesButtons')]/button/div[contains(@class,'ButtonTitle')]");
    private final Element slideTimerIndicators = Element.css(
            "[class*='progress-bar'], [class*='slide-timer'], " +
            ".carousel-indicator, [data-duration], [aria-valuenow]");
    private final Element cookiesAcceptBtn = Element.xpath("//button[text()='I Accept']");

    // ─── Actions ──────────────────────────────────────────────────────────────

    @Step("Open Sixers home page")
    public SixersTicketsPage open() {
        navigateTo(baseUrl);
        log.info("Opened Sixers home page: {}", baseUrl);
        dismissCookieBannerIfPresent(cookiesAcceptBtn);
        return this;
    }
    @Step("Count the number of carousel slides")
    public int getSlideCount() {
        List<WebElement> slides = findAllVisible(carouselSlides);
        log.info("Total carousel slides found: {}", slides.size());
        return slides.size();
    }

    @Step("Get titles of all carousel slides")
    public List<String> getSlideTitles() {
        List<WebElement> titleElements = findAll(slideTitles);
        List<String> titles = new ArrayList<>();
        for (WebElement el : titleElements) {
            try {
                titles.add(el.getText().trim());
            } catch (Exception e) {
                titles.add("(unable to read)");
            }
        }
        return titles;
    }
}
