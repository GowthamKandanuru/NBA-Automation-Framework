package com.veeva.dp1.tests;

import com.veeva.dp1.model.SixersTicketsTestData;
import com.veeva.dp1.pages.SixersTicketsPage;
import com.veeva.framework.constants.AppConstants;
import com.veeva.framework.utils.BaseTest;
import com.veeva.framework.utils.TestDataLoader;
import io.qameta.allure.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

/**
 * TC3 - Sixers Ticket Carousel Test
 * - Navigate to Sixers home page
 * - Count slides below Tickets menu
 * - Validate slide titles against expected test data
 */
@Epic("Derived Product 1 - Sixers")
@Feature("Tickets - Carousel Slides")
@Listeners(com.veeva.framework.listeners.AllureListener.class)
public class TC3_SixersTicketCarouselTest extends BaseTest {

    SixersTicketsTestData testData;
    List<String> expectedTitles;
    @BeforeClass
    public void loadTestData() {
        testData = TestDataLoader.loadJson(
                "testdata/sixers_tickets_testdata.json",
                SixersTicketsTestData.class
        );
        expectedTitles = testData.getExpectedTitles();
    }

    @Test(description = "Count and validate Sixers ticket carousel slides")
    @Story("TC3: Validate ticket carousel slide count, titles, and durations")
    @Severity(SeverityLevel.NORMAL)
    public void validateTicketCarouselSlides() {

        SixersTicketsPage sixersPage = new SixersTicketsPage();
        // Count slides
        int slideCount = sixersPage.open().getSlideCount();
        Allure.addAttachment("Slide Count", "text/plain", "Total slides: " + slideCount);
        log.info("Slide count: {}", slideCount);
        softAssert.assertTrue(slideCount >= 2,
                AppConstants.SLIDES_COUNT_ASSERTION_FAIL_MESSAGE + slideCount);
        // Get and validate titles
        List<String> actualTitles = sixersPage.getSlideTitles();
        log.info("Slide titles: {}", actualTitles);
        Allure.addAttachment("Slide Titles", "text/plain", String.join("\n", actualTitles));
        softAssert.assertEquals(actualTitles, expectedTitles,AppConstants.TITLES_MISMATCH_METHOD);
        softAssert.assertFalse(actualTitles.isEmpty(),
                AppConstants.TITLES_SIZE_ASSERTION_FAIL_MESSAGE);
        log.info("TC3 PASSED | Slides: {} | Titles: {}", slideCount, actualTitles.size());
        softAssert.assertAll();
    }
}
