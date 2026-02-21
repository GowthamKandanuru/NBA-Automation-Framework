package com.veeva.dp2.pages;

import com.veeva.dp2.model.FooterLink;
import com.veeva.framework.config.ConfigManager;
import com.veeva.framework.pages.BasePage;
import com.veeva.framework.pages.Element;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;

import java.util.*;

/**
 * BullsFooterPage - Page Object for TC4.
 *
 * Sits in src/main/java as a reusable page component.
 * Locators declared as {@link Element} fields — no @FindBy, no PageFactory, no raw By.
 * TC4: Scroll to footer, collect all hyperlinks per category, detect duplicates.
 */
public class BullsFooterPage extends BasePage {

    private final String baseUrl = ConfigManager.get("urls.derived-product2", "https://www.nba.com/bulls/");

    // ─── Locators ─────────────────────────────────────────────────────────────

    private final Element footerLinks = Element.xpath(
            "//footer//li[@class='mb-2 last:mb-0 lg:block lg:w-full']/a");
    private final Element footerSection  = Element.css(
            "footer a, .site-footer a, [class*='footer'] a");
    private final Element cookiesAcceptBtn = Element.xpath("//button[text()='I Accept']");

    // ─── Actions ──────────────────────────────────────────────────────────────

    @Step("Open Bulls home page")
    public BullsFooterPage open() {
        navigateTo(baseUrl);
        log.info("Opened Bulls home page: {}", baseUrl);
        dismissCookieBannerIfPresent(cookiesAcceptBtn);
        return this;
    }

    @Step("Scroll to footer")
    public BullsFooterPage scrollToFooter() {
        scrollToBottom();
       /* try {
            scrollToElement(footerSection);
            Thread.sleep(500);
        } catch (Exception e) {
            log.warn("Footer element not found via locator, scrolled to bottom: {}", e.getMessage());
        }
        log.info("Scrolled to footer");*/
        return this;
    }

    @Step("Collect all footer hyperlinks with their categories")
    public List<FooterLink> collectFooterLinks() {
        List<WebElement> links = findAllVisible(footerLinks);
        List<FooterLink> result = new ArrayList<>();

        for (WebElement linkEl : links) {
            try {
                String href     = linkEl.getAttribute("href");
                String text     = linkEl.getText().trim();
                /*String category = getCategoryForLink(linkEl);*/

                if (href != null && !href.isEmpty() && !href.equals("#") && !text.isEmpty()) {
                    result.add(new FooterLink(text, href));
                    log.debug("Footer link: {} → {}", text, href);
                }
            } catch (Exception e) {
                log.warn("Error reading footer link: {}", e.getMessage());
            }
        }
        log.info("Total footer links collected: {}", result.size());
        return result;
    }

    @Step("Find duplicate hyperlinks in footer")
    public ArrayList<String> findDuplicateLinks(List<FooterLink> links) {
        ArrayList<String> duplicates = new ArrayList<>();
        /*Map<String, List<String>> urlToTexts = new LinkedHashMap<>();
        for (FooterLink link : links) {
            urlToTexts.computeIfAbsent(link.getHref(), k -> new ArrayList<>()).add(link.getText());
        }

        Map<String, List<String>> duplicates = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : urlToTexts.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.put(entry.getKey(), entry.getValue());
            }
        }

        log.info("Duplicate URLs found: {}", duplicates.size());
        duplicates.forEach((url, texts) ->
                log.warn("DUPLICATE → {} | Appears as: {}", url, texts));*/

        Map<String,Integer> urls = new LinkedHashMap<>();
        for(FooterLink link : links)
        {
            urls.put(link.getHref(),urls.getOrDefault(link.getHref(), 0)+1);
        }
        for(Map.Entry<String,Integer> ma: urls.entrySet())
        {
            if(ma.getValue() > 1)
            {
                duplicates.add(ma.getKey());
            }
        }
        return duplicates;
    }

    /**
     * Walks up the DOM from a link to find the nearest section heading — used as the category label.
     */
    private String getCategoryForLink(WebElement link) {

       /* try {
            String result = (String) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "var el = arguments[0];" +
                    "while (el && el !== document.body) {" +
                    "  el = el.parentElement;" +
                    "  var h = el.querySelector('h3,h4,h5,strong,.footer-heading,.category-title');" +
                    "  if (h) return h.innerText.trim();" +
                    "} return 'General';",
                    link);
            return result != null ? result : "General";
        } catch (Exception e) {
            return "General";
        }*/
        return "";
    }

    // ─── Data record ──────────────────────────────────────────────────────────

    /*public record FooterLink(String text, String href, String category) {
        public String[] toCsvRow(boolean isDuplicate) {
            return new String[]{text, href, category, isDuplicate ? "YES" : "NO"};
        }
    }*/
}
