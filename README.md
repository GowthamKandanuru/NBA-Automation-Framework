# Veeva Test Automation Framework

> A **multi-module, parallel-ready Selenium test automation framework** built with Java 11 · Selenium 4 · TestNG · Allure · Selenoid

---

## Table of Contents

- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Test Cases](#test-cases)
- [Configuration](#configuration)
- [Running Tests](#running-tests)
- [Parallel Execution](#parallel-execution)
- [Remote Execution — Selenoid](#remote-execution--selenoid)
- [Allure Reports](#allure-reports)
- [CI/CD — Jenkins](#cicd--jenkins)
- [Design Decisions](#design-decisions)

---

## Architecture

```
veeva-automation-parent (root pom)
├── automation-framework          ← Shared library: drivers, pages, utils, config
├── core-product-tests            ← Warriors  (https://www.nba.com/warriors)
├── derived-product1-tests        ← Sixers    (https://www.nba.com/sixers/)
└── derived-product2-tests        ← Bulls     (https://www.nba.com/bulls/)
```

Each product module depends on `automation-framework` and contains its own page objects, test classes, test data, and `testng.xml`. No test logic leaks into the shared framework.

---

## Technology Stack

| Category | Technology | Version |
|---|---|---|
| Language | Java | 11 |
| Browser Automation | Selenium | 4.18.1 |
| Test Framework | TestNG | 7.9.0 |
| Reporting | Allure | 2.25.0 |
| Build Tool | Maven (Multi-Module) | 3.8+ |
| Browser Driver Management | WebDriverManager | 5.7.0 |
| Logging | Log4j2 | 2.23.1 |
| Test Data | Jackson (JSON + YAML) | 2.17.0 |
| CSV Export | OpenCSV | 5.9 |
| Remote Grid | Selenoid (aerokube) | latest |
| CI/CD | Jenkins Declarative Pipeline | — |

---

## Prerequisites

| Tool | Minimum Version | Check |
|---|---|---|
| Java JDK | 11 | `java -version` |
| Apache Maven | 3.8 | `mvn -version` |
| Chrome / Firefox / Edge | Latest stable | — |
| Allure CLI | 2.x | `allure --version` |
| Docker (for Selenoid) | 20+ | `docker --version` |

**Install Allure CLI:**
```bash
# macOS
brew install allure

# Windows (via Scoop)
scoop install allure

# Linux
sudo apt-get install allure
```

---

## Project Structure

```
veeva-automation-framework/
│
├── pom.xml                                         ← Parent POM — dependency versions, plugin config
│
├── automation-framework/                           ← Module 1: Shared framework library
│   └── src/main/java/com/veeva/framework/
│       ├── config/
│       │   └── ConfigManager.java                  ← YAML + System property + Env var resolution
│       ├── constants/
│       │   └── AppConstants.java                   ← Shared assertion messages and page titles
│       ├── driver/
│       │   ├── DriverFactory.java                  ← Browser creation: Chrome / Firefox / Edge / Remote
│       │   └── DriverManager.java                  ← ThreadLocal WebDriver — parallel-safe
│       ├── pages/
│       │   ├── BasePage.java                       ← All Selenium interactions (click, type, hover…)
│       │   └── Element.java                        ← Locator wrapper — replaces raw By usage
│       ├── listeners/
│       │   └── AllureListener.java                 ← Auto-screenshot on test failure
│       └── utils/
│           ├── BaseTest.java                       ← TestNG lifecycle (@BeforeTest / @AfterTest)
│           ├── FileUtils.java                      ← Text and CSV file writing
│           └── TestDataLoader.java                 ← JSON / YAML test data loader
│   └── src/main/resources/
│       ├── config.yaml                             ← All runtime configuration
│       └── log4j2.xml                              ← Logging configuration
│
├── core-product-tests/                             ← Module 2: Warriors tests
│   └── src/
│       ├── main/java/com/veeva/cp/pages/
│       │   ├── WarriorsHomePage.java
│       │   ├── WarriorsNewsPage.java
│       │   └── WarriorsStatsPage.java
│       ├── test/java/com/veeva/cp/tests/
│       │   ├── TC1_WarriorsTeamStats.java
│       │   └── TC2_WarriorsVideosTest.java
│       └── test/resources/
│           └── testng.xml                          ← parallel="tests" thread-count="2"
│
├── derived-product1-tests/                         ← Module 3: Sixers tests
│   └── src/
│       ├── main/java/com/veeva/dp1/
│       │   ├── model/SixersTicketsTestData.java
│       │   ├── model/SlideData.java
│       │   └── pages/SixersTicketsPage.java
│       ├── test/java/com/veeva/dp1/tests/
│       │   └── TC3_SixersTicketCarouselTest.java
│       └── test/resources/
│           ├── testdata/sixers_tickets_testdata.json
│           ├── testdata/sixers_tickets_testdata.yaml
│           └── testng.xml
│
└── derived-product2-tests/                         ← Module 4: Bulls tests
    └── src/
        ├── main/java/com/veeva/dp2/
        │   ├── model/FooterLink.java
        │   └── pages/BullsFooterPage.java
        ├── test/java/com/veeva/dp2/tests/
        │   └── TC4_BullsFooterLinksTest.java
        └── test/resources/
            └── testng.xml
```

---

## Test Cases

### Module: `core-product-tests` — Golden State Warriors

| ID | Test Method | Description |
|---|---|---|
| TC1 | `collectTeamStatsAndExportToFile` | Navigate Home → Teams → Team Stats (opens new tab), capture team stats data and export to a text file, attach to Allure report |
| TC2 | `countVideoFeedsAndFilterByAge` | Navigate Home → Hamburger Menu → News & Features, count total video feeds and count videos that are ≥ 3 days old, attach counts to Allure report |

**Parallel config:** TC1 runs on **Chrome**, TC2 runs on **Edge** — simultaneously via `parallel="tests" thread-count="2"`.

---

### Module: `derived-product1-tests` — Philadelphia 76ers

| ID | Test Method | Description |
|---|---|---|
| TC3 | `validateTicketCarouselSlides` | Navigate to Sixers Tickets page, count carousel slides (assert ≥ 2), collect slide titles and validate against expected titles in `sixers_tickets_testdata.json` |

---

### Module: `derived-product2-tests` — Chicago Bulls

| ID | Test Method | Description |
|---|---|---|
| TC4 | `collectFooterLinksAndDetectDuplicates` | Navigate to Bulls home page, scroll to footer, collect all hyperlinks with their text, export to a timestamped CSV file, detect and report any duplicate URLs in Allure |

---

## Configuration

All configuration lives in `automation-framework/src/main/resources/config.yaml`.

```yaml
# Browser: chrome | firefox | edge
browser: chrome
headless: false

# Remote / Selenoid
remote:
  enabled: false
  hub_url: "http://localhost:4444/wd/hub"

# Firefox binary (only needed if geckodriver picks wrong executable)
# e.g. C:/Program Files/Mozilla Firefox/firefox.exe
firefox:
  binary: ""

# Timeouts (seconds)
implicit:
  wait: 10
explicit:
  wait: 60
page:
  load:
    timeout: 30

# Product URLs
urls:
  core-product:    https://www.nba.com/warriors
  derived-product1: https://www.nba.com/sixers/
  derived-product2: https://www.nba.com/bulls/
```

**Override priority (highest to lowest):**

```
-D system property  →  Environment variable  →  config.yaml  →  hardcoded default
```

Override without touching the file:
```bash
# System property
mvn test -Dbrowser=firefox -Dheadless=true -Dimplicit.wait=15

# Environment variable
BROWSER=edge HEADLESS=true mvn test
```

---

## Running Tests

### Build the entire project
```bash
mvn clean install -DskipTests
```

### Run all modules
```bash
mvn clean test
```

### Run a specific module
```bash
# Core Product — Warriors (TC1 + TC2)
mvn clean test -pl core-product-tests -am

# Derived Product 1 — Sixers (TC3)
mvn clean test -pl derived-product1-tests -am

# Derived Product 2 — Bulls (TC4)
mvn clean test -pl derived-product2-tests -am
```

### Override browser at runtime
```bash
mvn clean test -pl core-product-tests -am -Dbrowser=firefox
mvn clean test -pl core-product-tests -am -Dbrowser=edge -Dheadless=true
```

### Run headless (CI mode)
```bash
mvn clean test -Dheadless=true
```

---

## Parallel Execution

The `core-product-tests` module runs **TC1 and TC2 in parallel on different browsers** using TestNG's `parallel="tests"` mode.

**`testng.xml` configuration:**
```xml
<suite name="Core Product - Warriors Test Suite" parallel="tests" thread-count="2">

    <test name="TC1 - Warriors Team Stats">
        <parameter name="browser" value="chrome" />
        <classes>
            <class name="com.veeva.cp.tests.TC1_WarriorsTeamStats"/>
        </classes>
    </test>

    <test name="TC2 - Warriors Video Feeds">
        <parameter name="browser" value="edge" />
        <classes>
            <class name="com.veeva.cp.tests.TC2_WarriorsVideosTest"/>
        </classes>
    </test>

</suite>
```

**How thread safety is achieved:**

- `DriverManager` stores each thread's `WebDriver` in a `ThreadLocal<WebDriver>` — threads never share a driver instance
- `DriverManager` also stores the browser name in a `ThreadLocal<String>` — parallel threads never overwrite each other's browser
- The `browser` parameter flows directly from `testng.xml` → `@BeforeTest` → `DriverManager.initDriver(browser)` → `DriverFactory.createDriver(browser)` — **`System.setProperty` is never used**, which would be a shared global and break parallel runs
- `BaseTest.@BeforeTest` creates the driver; `@AfterTest` quits it — one driver per `<test>` block, matching the TestNG lifecycle correctly

---

## Remote Execution — Selenoid

The framework supports **Selenoid** (or any Selenium Grid 4 hub) for remote execution.

### Start Selenoid
```bash
# 1. Start Selenoid hub first
docker start <selenoid-container-id>

# 2. Start Selenoid UI
docker start <selenoid-ui-container-id>

# Verify Selenoid is ready
curl http://localhost:4444/status

# Open Selenoid dashboard
# http://localhost:8080
```

> Do **not** start browser containers manually — Selenoid manages `selenoid/vnc_chrome:128.0` and `selenoid/vnc_firefox:125.0` automatically per session.

### Enable remote in config
```yaml
remote:
  enabled: true
  hub_url: "http://localhost:4444/wd/hub"
```

Or pass at runtime:
```bash
mvn clean test -Dremote=true -Dhub_url=http://localhost:4444/wd/hub -Dbrowser=chrome
```

### Supported remote browser images
| Browser | Docker Image |
|---|---|
| Chrome | `selenoid/vnc_chrome:128.0` |
| Firefox | `selenoid/vnc_firefox:125.0` |

Live sessions are visible in the Selenoid UI at `http://localhost:8080` (VNC enabled).

---

## Allure Reports

### Generate and open report after a test run
```bash
# Generate for a specific module
mvn allure:report -pl core-product-tests

# Open in browser
allure open core-product-tests/target/site/allure-maven-plugin
```

### Serve live results
```bash
allure serve core-product-tests/target/allure-results
```

### What's captured in the report
- ✅ Test steps via `@Step` annotations on every page action
- ✅ Auto-screenshot attached on every test failure (`AllureListener`)
- ✅ Custom attachments: video count (TC2), team stats file (TC1), CSV file (TC4), slide titles (TC3)
- ✅ Epics, Features, Stories, Severity levels on every test
- ✅ Full test execution log per test

### Output files

| File | Location | Produced by |
|---|---|---|
| Team Stats TXT | `core-product-tests/target/test-outputs/` | TC1 |
| Bulls Footer CSV | `derived-product2-tests/target/test-outputs/tc4-footer/` | TC4 |
| Allure raw results | `*/target/allure-results/` | All modules |
| Surefire XML | `*/target/surefire-reports/` | All modules |

---

## CI/CD — Jenkins

A `Jenkinsfile.groovy` is included at the project root.

### Pipeline Parameters

| Parameter | Options | Default | Description |
|---|---|---|---|
| `BROWSER` | `chrome` / `firefox` / `edge` | `chrome` | Browser for test execution |
| `MODULE` | `all` / `core-product-tests` / `derived-product1-tests` / `derived-product2-tests` | `all` | Module to run |
| `HEADLESS` | `true` / `false` | `true` | Run browsers headlessly |

### Pipeline Stages

```
Checkout  →  Build & Compile  →  Run Tests  →  Generate Allure Report  →  Archive Artifacts
```

1. **Checkout** — clones from your configured Git repo URL
2. **Build & Compile** — compiles `automation-framework` and all dependent modules
3. **Run Tests** — executes the selected module with browser and headless parameters
4. **Generate Allure Report** — builds the HTML report and publishes in Jenkins UI via the Allure plugin
5. **Archive Artifacts** — archives output files, logs, and surefire XML reports

### Jenkins Setup Requirements
- JDK 11 tool configured as `JDK-11`
- Maven 3.9 tool configured as `Maven-3.9`
- [Allure Jenkins Plugin](https://plugins.jenkins.io/allure-jenkins-plugin/) installed
- Update the repo URL in `Jenkinsfile.groovy`:
```groovy
git branch: 'main', url: 'https://github.com/YOUR_USERNAME/veeva-automation-framework.git'
```

---

## Design Decisions

| Decision | Rationale |
|---|---|
| **Multi-Module Maven** | Clean boundary between the reusable framework and product-specific test code. Product modules only add a single dependency on `automation-framework`. |
| **`Element` wrapper (no raw `By`)** | All locators are declared as `Element` fields using `Element.xpath()`, `Element.css()`, `Element.id()`. No `By` class is used directly in any page object — cleaner, more readable, and explicit-wait aware by default. |
| **ThreadLocal WebDriver** | Each parallel test thread gets a completely isolated `WebDriver` instance stored in `ThreadLocal`. No static driver fields, no synchronization needed. |
| **Browser via ThreadLocal (not `System.setProperty`)** | `System.setProperty` is JVM-global — writing to it from a parallel thread would overwrite another thread's browser choice. The browser name is instead stored in `ThreadLocal<String>` in `DriverManager` and passed explicitly to `DriverFactory`. |
| **`@BeforeTest` / `@AfterTest` scope** | The driver is created once per TestNG `<test>` block (not per method). `@AfterTest` quits the driver after all methods in that `<test>` have run. `@BeforeMethod` only resets `SoftAssert`. This matches the parallel execution lifecycle correctly. |
| **`ConfigManager` priority chain** | System Property → Environment Variable → `config.yaml` → default. Follows 12-factor app config principles — no values need to be changed in code for different environments. |
| **`SoftAssert` per method** | A fresh `SoftAssert` is created in `@BeforeMethod` so each test method collects its own failures independently and reports them all together at `assertAll()`. |
| **Allure `@Step` on all page actions** | Every `click`, `type`, `hover`, `navigate` call in `BasePage` and all page objects is annotated or wrapped with Allure steps, producing a full human-readable execution trace in the report without any extra effort in test classes. |
| **Selenoid VNC images** | Using `selenoid/vnc_chrome` and `selenoid/vnc_firefox` enables live visual monitoring of parallel test sessions via the Selenoid UI dashboard. |
