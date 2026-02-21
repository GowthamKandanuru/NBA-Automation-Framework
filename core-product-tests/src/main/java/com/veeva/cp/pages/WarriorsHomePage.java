package com.veeva.cp.pages;

import com.veeva.framework.config.ConfigManager;
import com.veeva.framework.pages.BasePage;
import com.veeva.framework.pages.Element;
import io.qameta.allure.Step;
import org.openqa.selenium.NoSuchElementException;

/**
 * WarriorsHomePage - Entry point for Core Product (Warriors).
 *
 * Sits in src/main/java as a reusable page component.
 * Locators declared as {@link Element} fields — no @FindBy, no PageFactory, no raw By.
 */
public class WarriorsHomePage extends BasePage {

    private final String baseUrl = ConfigManager.get("urls.core-product", "https://www.nba.com/warriors");

    // ─── Locators ─────────────────────────────────────────────────────────────

    //private final Element cookieBanner    = Element.css("[id*='cookie'], [class*='cookie-banner']");

    private final Element hamburgerIcon      = Element.xpath(
            "//span[text()='...']/parent::button/parent::li");
    private final Element nbaHome = Element.xpath("//a[@title='Home']");
    private final Element newAndFeaturesLink = Element.xpath(
            "//ul[@role='menubar']//a[@title='News & Features']");
    private final Element teams = Element.xpath(
            "//div[contains(@class,'brand-font _headerMenu')]//ul[@role='menubar']//span[text()='Team']");
    private final Element teamsStats = Element.xpath("//ul[@role='menubar']//a[@title='Team Stats']");
    private final Element preSaleDialog = Element.xpath("//div[@class='p-2 absolute right-3 hover:cursor-pointer']");

    private final Element cookiesAcceptBtn = Element.xpath("//button[text()='I Accept']");

    // ─── Actions ──────────────────────────────────────────────────────────────

    @Step("Open Warriors home page")
    public WarriorsHomePage open() {
        navigateTo(baseUrl);
        log.info("Opened Warriors home page: {}", baseUrl);
        dismissCookieBannerIfPresent(cookiesAcceptBtn);
        click(preSaleDialog);
        return this;
    }

    @Step("Open Menu in the Home Page")
    public WarriorsHomePage openMenu()
    {
        hover(hamburgerIcon);
        return this;
    }
    @Step("Hover on hamburger menu → click 'New & Features'")
    public WarriorsNewsPage navigateToNewAndFeatures() {
        int counter = 0;
        while(counter < 3) {
            try {
                openMenu();
                click(newAndFeaturesLink);
                break;
            } catch (Exception e) {
                log.info("Unable to click News and features option so Falling back to direct News menu click again");
                counter++;
                hover(nbaHome);
            }
        }
        if(counter == 3)
        {
            throw new NoSuchElementException("Not able to click the News and Features after retrying 3 times");
        }
        log.info("Navigated to New & Features | URL: {}", getCurrentUrl());
        return new WarriorsNewsPage();
    }
    /*private void dismissCookieBannerIfPresent() {
        try {
            *//*if (isDisplayed(cookieBanner) && isDisplayed(cookieAcceptBtn)) {*//*
                click(cookiesAcceptBtn);
                log.info("Cookie banner dismissed");
           *//* }*//*
        } catch (Exception e) {
            log.debug("No cookie banner found or already dismissed");
        }
    }*/
    @Step("Hover on hamburger menu → click 'TEAMS'")
    public WarriorsStatsPage navigateToTeams()
    {
        hover(teams);
        click(teamsStats);
        String newWindowTitle = switchToWindow();
        /*if(newWindowTitle.contains("Golden State Warriors Team Info and News | NBA.com"))
        {
            return new WarriorsStatsPage();
        }else
        {
            throw new RuntimeException("Windows Switch doesn't happen");
        }*/
        return new WarriorsStatsPage();
    }
}
