package com.veeva.cp.pages;

import com.veeva.framework.pages.BasePage;
import com.veeva.framework.pages.Element;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * WarriorsShopPage - Page Object for the Warriors Shop (Men's → Jackets).
 *
 * Sits in src/main/java as a reusable page component.
 * Locators declared as {@link Element} fields — no @FindBy, no PageFactory, no raw By.
 * TC1: Navigate Shop > Men's > Jackets and collect all products across paginated pages.
 */
public class WarriorsStatsPage extends BasePage {

    // ─── Navigation Locators ──────────────────────────────────────────────────

    List<String> OverAlllines = new ArrayList<>();
    PrintWriter pw;
    private final Element statsDivision = Element.xpath(
            "//div[contains(@class,'Crom_base')]");
    private String rows = "//div[contains(@class,'Crom_base')][%d]//tr";
    private String headers = "//div[contains(@class,'Crom_base')][%d]//th";
    private String body = "//div[contains(@class,'Crom_base')][%d]//tr[%d]/td";
    // ─── Actions ──────────────────────────────────────────────────────────────

    @Step("Navigate to Warriors Shop > Men's section")
    public String captureTeamStats() {

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String dir = "target/test-outputs/" + "tc1-text";
        new File(dir).mkdirs();
        String filePath = dir + "/bulls_footer_links_" + timestamp + ".txt";
        try {
            pw = new PrintWriter(new FileWriter(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<WebElement> statsBoxes = findAllVisible(statsDivision);
        for(int i = 1 ; i <= statsBoxes.size() ; i++)
        {
            List<WebElement> trows = findAllVisible(Element.xpath(String.format(rows, i)));
            for(int j = 1 ; j < trows.size() ; j++) {
                List<WebElement> headTexts = findAllVisible(Element.xpath(String.format(headers, i)));
                for(WebElement e : headTexts)
                {
                    OverAlllines.add(e.getText());
                }
                pw.println("********************************************************************************");
                for (String line : OverAlllines) {
                        pw.println(line); // println() adds the line separator
                    }
                    System.out.println("Lines written to the file successfully using PrintWriter.");
                pw.println("********************************************************************************");
                OverAlllines.clear();
                List<WebElement> bodyText = findAllVisible(Element.xpath(String.format(body,i,j)));
                for(WebElement e : bodyText)
                {
                    OverAlllines.add(e.getText());
                }
                    for (String line : OverAlllines) {
                        pw.println(line); // println() adds the line separator
                    }
                    OverAlllines.clear();
                }
            }
        return filePath;
    }
}