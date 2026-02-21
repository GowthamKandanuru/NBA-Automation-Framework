package com.veeva.cp.tests;

import com.veeva.cp.pages.WarriorsHomePage;
import com.veeva.cp.pages.WarriorsNewsPage;
import com.veeva.framework.constants.AppConstants;
import com.veeva.framework.utils.BaseTest;
import io.qameta.allure.*;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * TC2 - Warriors Video Feeds Count Test
 * Navigate: Home > Hamburger Menu > New & Features
 * - Count total video feeds
 * - Count videos >= 3 days old
 */
@Epic("Core Product - Golden State Warriors")
@Feature("News - Video Feeds")
@Listeners(com.veeva.framework.listeners.AllureListener.class)
public class TC2_WarriorsVideosTest extends BaseTest {

    WarriorsNewsPage warriorsNewsPage;

    @Test(description = "Count total video feeds and video feeds which are there more than and equal to 3 days")
    @Story("TC2.1: Navigate to New & Features; count video feeds")
    @Severity(SeverityLevel.NORMAL)
    public void countVideoFeedsAndFilterByAge() {
        // Open home page and Navigate to New & Features
        warriorsNewsPage = new WarriorsHomePage().open().openMenu().navigateToNewAndFeatures();
        softAssert.assertTrue(warriorsNewsPage.waitForTitleIs(AppConstants.NEWS_PAGE_TITLE), AppConstants.TITLE_NOT_FOUND);
        // Count all videos
        int totalVideos = warriorsNewsPage.getTotalVideoCount();
        Allure.addAttachment("Total Video Feeds", "text/plain", "Total Videos: " + totalVideos);
        log.info("Total video feeds: {}", totalVideos);
        softAssert.assertTrue(totalVideos > 0, AppConstants.TOTAL_VIDEOS_ASSERTION_MESSAGE);
        int videosOlderThan3Days = warriorsNewsPage.getVideosOlderThan3Days();
        Allure.addAttachment("Videos >= 3 Days", "text/plain",
                "Videos >= 3d: " + videosOlderThan3Days);
        /*softAssert.assertTrue(videosOlderThan3Days > 0,
                AppConstants.VIDEOS_COUNT_ASSERTION_MESSAGE);*/
        log.info("TC2 PASSED | Total:>= 3d: {}", videosOlderThan3Days);
        softAssert.assertAll();
    }
}
