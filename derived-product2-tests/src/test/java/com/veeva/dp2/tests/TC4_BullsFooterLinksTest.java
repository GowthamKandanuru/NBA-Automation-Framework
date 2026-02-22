package com.veeva.dp2.tests;

import com.veeva.dp2.model.FooterLink;
import com.veeva.dp2.pages.BullsFooterPage;
import com.veeva.framework.utils.BaseTest;
import com.veeva.framework.utils.FileUtils;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TC4 - Bulls Footer Links CSV Test
 * - Navigate to Bulls home page
 * - Scroll to footer
 * - Collect all footer links with their categories
 * - Export to CSV file
 * - Report any duplicate hyperlinks
 */
@Epic("Derived Product 2 - Chicago Bulls")
@Feature("Footer - Hyperlinks")
@Listeners(com.veeva.framework.listeners.AllureListener.class)
public class TC4_BullsFooterLinksTest extends BaseTest {

    List<FooterLink> links;
    BullsFooterPage bullsPage;

    @Test(description = "Collect all Bulls footer links, export to CSV, detect duplicates",priority = 0)
    @Story("TC4: Find footer hyperlinks, write CSV, flag duplicate URLs")
    @Severity(SeverityLevel.NORMAL)
    public void collectFooterLinksAndDetectDuplicates() {
         bullsPage = new BullsFooterPage();
         // collect all Footer Links
       links = bullsPage.open()
                 .scrollToFooter().collectFooterLinks();
        softAssert.assertFalse(links.isEmpty(), "Expected at least some footer links to be present");
        // Build CSV rows
        List<String[]> csvRows = new ArrayList<>();
        String[] headers = {"S.No","Footer", "URL"};
        for(int i = 0 ; i < links.size() ; i++)
        {
            csvRows.add(new String[]{String.valueOf(i+1),links.get(i).getText(),links.get(i).getHref()});
        }
        // Write CSV
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String outputDir = ensureOutputDir("tc4-footer");
        String csvPath = outputDir + "/bulls_footer_links_" + timestamp + ".csv";
        FileUtils.writeToCsvFile(csvPath, headers, csvRows);
        // Attach CSV to Allure report
        attachFileToReport("Bulls Footer Links CSV", csvPath, "text/csv");
        // Find duplicates
        ArrayList<String> duplicates = bullsPage.findDuplicateLinks(links);
        // Report duplicates in Allure
        String duplicateSummary = duplicates.isEmpty()
                ? "No duplicate URLs found."
                : "DUPLICATE URLs FOUND:\n" + duplicates.stream()
                .map(e -> e+ " -> [" + String.join(", ", e) + "]")
                .collect(Collectors.joining("\n"));
        Allure.addAttachment("Duplicate URL Report", "text/plain", duplicateSummary);
        log.info("TC4 | Total links: {} | Duplicates: {}", links.size(), duplicates.size());
        log.info(duplicateSummary);
        // TC4 passes regardless of duplicates — requirement is just to REPORT them
        softAssert.assertTrue(links.size() > 0, "Footer links should be present");
        softAssert.assertTrue(duplicates.isEmpty() , "Footer links duplicates are exist");
        log.info("TC4 PASSED | Links : {} | Duplicates flagged: {}", links.size(), duplicates.size());
        softAssert.assertAll();
    }
}
