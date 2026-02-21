package com.veeva.cp.pages;

import com.veeva.framework.pages.BasePage;
import com.veeva.framework.pages.Element;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WarriorsNewsPage - Page Object for Warriors News / New & Features page.
 *
 * Sits in src/main/java as a reusable page component.
 * Locators declared as {@link Element} fields — no @FindBy, no PageFactory, no raw By.
 * TC2: Count total video feeds and those >= 3 days old.
 */
public class WarriorsNewsPage extends BasePage {


    // ─── Video Feed Locators ──────────────────────────────────────────────────

    private final Element videoFeedItems  = Element.xpath(
            "//h3/preceding-sibling::div//*[name()='svg'][@name='video']");
    private final Element video  = Element.xpath(
            "//h3[text()='VIDEOS']");
    private final Element videoTimestamps = Element.xpath(
            "//h3[text()='VIDEOS']/ancestor::div[contains(@class,'Columns_column__dIKLJ columns flex')]//time/span[contains(text(),'d')]");

    // ─── Actions ──────────────────────────────────────────────────────────────
    @Step("Count all video feed items on the page")
    public int getTotalVideoCount() {
        scrollToElement(video);
        List<WebElement> videos = findAllVisible(videoFeedItems);
        log.info("Total video feeds found: {}", videos.size());
        return videos.size();
    }
    @Step("Count video feeds that are >= 3 days old")
    public int getVideosOlderThan3Days() {
        List<WebElement> timestamps = findAll(videoTimestamps);
        int count = 0;
        for (WebElement ts : timestamps) {
            if(!ts.getText().isEmpty()) {
                try {
                    int days = parseDaysFromTimestamps(ts.getText().trim().toLowerCase());
                    if (days >= 3) {
                        count++;
                        log.debug("Video >= 3d: '{}' → {} days", ts.getText(), days);
                    }
                } catch (Exception e) {
                    log.warn("Could not parse timestamp: {}", e.getMessage());
                }
            }
        }
        log.info("Video feeds >= 3 days: {}", count);
        return count;
    }

    public static int parseDaysFromTimestamps(String s)
    {
        String days = s.substring(0, s.indexOf('d'));
        return Integer.parseInt(days);
    }
    private int parseDaysFromTimestamp(String text) {
        Matcher d = Pattern.compile("(\\d+)\\s*d").matcher(text);
        if (d.find()) return Integer.parseInt(d.group(1));

        Matcher w = Pattern.compile("(\\d+)\\s*w").matcher(text);
        if (w.find()) return Integer.parseInt(w.group(1)) * 7;

        Matcher days = Pattern.compile("(\\d+)\\s*days?").matcher(text);
        if (days.find()) return Integer.parseInt(days.group(1));

        Matcher weeks = Pattern.compile("(\\d+)\\s*weeks?").matcher(text);
        if (weeks.find()) return Integer.parseInt(weeks.group(1)) * 7;

        Matcher months = Pattern.compile("(\\d+)\\s*months?").matcher(text);
        if (months.find()) return Integer.parseInt(months.group(1)) * 30;

        if (text.contains("hour") || text.contains("min") || text.contains("just now")) return 0;
        return -1;
    }
}
