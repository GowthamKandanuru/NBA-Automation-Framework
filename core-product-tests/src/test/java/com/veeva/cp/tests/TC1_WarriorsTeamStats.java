package com.veeva.cp.tests;

import com.veeva.cp.pages.WarriorsHomePage;
import com.veeva.cp.pages.WarriorsStatsPage;
import com.veeva.framework.utils.BaseTest;
import io.qameta.allure.*;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * TC1 - Warriors Jackets Collection Test
 * Navigate: Home > Shop > Men's > Jackets
 * - Collect all jackets from all paginated pages
 * - Store Title, Price, Top Seller info to a text file
 * - Attach the text file to Allure report
 */
@Epic("Core Product - Golden State Warriors")
@Feature("Shop - Men's Jackets")
@Listeners(com.veeva.framework.listeners.AllureListener.class)
public class TC1_WarriorsTeamStats extends BaseTest {

    @Test(description = "Collect all Men's Jackets and export to text file")
    @Story("TC1: Find all Jackets across paginated pages and store to file")
    @Severity(SeverityLevel.NORMAL)
    public void collectTeamStatsAndExportToFile() {
        // Open home page
        WarriorsHomePage homePage = new WarriorsHomePage();
        WarriorsStatsPage warriorsStatsPage = homePage.open().navigateToTeams();
        String textFilePath = warriorsStatsPage.captureTeamStats();
        attachFileToReport("Team Stats Text File",textFilePath , "text/csv");
    }
}
