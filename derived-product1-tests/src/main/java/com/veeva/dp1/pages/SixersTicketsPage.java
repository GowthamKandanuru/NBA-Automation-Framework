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

   /* @Step("Scroll below Tickets menu to find carousel")
    public SixersTicketsPage scrollToTicketsCarousel() {
        try {
            scrollToElement(ticketsNavLink);
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
            Thread.sleep(800);
        } catch (Exception e) {
            log.warn("Could not locate tickets nav, scrolling to bottom: {}", e.getMessage());
            scrollToBottom();
        }
        return this;
    }
*/
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

    @Step("Get display duration of each slide (ms)")
    public List<Long> getSlideDurations() {
        List<WebElement> indicators = findAll(slideTimerIndicators);
        List<Long> durations = new ArrayList<>();
        for (WebElement indicator : indicators) {
            try {
                String dataDuration = indicator.getAttribute("data-duration");
                if (dataDuration != null && !dataDuration.isEmpty()) {
                    durations.add(Long.parseLong(dataDuration));
                    continue;
                }
                String ariaVal = indicator.getAttribute("aria-valuenow");
                if (ariaVal != null) {
                    durations.add(Long.parseLong(ariaVal));
                    continue;
                }
                String style = indicator.getAttribute("style");
                if (style != null && style.contains("animation-duration")) {
                    durations.add(parseDurationFromStyle(style));
                    continue;
                }
                durations.add(-1L);
            } catch (Exception e) {
                durations.add(-1L);
                log.warn("Could not parse slide duration: {}", e.getMessage());
            }
        }
        log.info("Slide durations collected: {}", durations);
        return durations;
    }

    private long parseDurationFromStyle(String style) {
        try {
            String[] parts = style.split("animation-duration:\\s*");
            if (parts.length > 1) {
                String val = parts[1].split(";")[0].trim();
                if (val.endsWith("ms")) return Long.parseLong(val.replace("ms", ""));
                if (val.endsWith("s"))  return (long) (Double.parseDouble(val.replace("s", "")) * 1000);
            }
        } catch (Exception ignored) {}
        return -1L;
    }
}
