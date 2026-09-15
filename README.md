# Playwright Automation Framework (Java)

A reusable web automation framework for multiple projects. The source contains only automation infrastructure; URLs, selectors, pages, steps, credentials, and test data belong to the consuming application repository.

## Modules

```text
playwright-framework-java/
├── framework-core/                 # Playwright, locators, lifecycle, configuration, evidence
├── framework-testng/               # plain TestNG, independent of Cucumber
└── framework-cucumber-testng/      # Cucumber world, hooks, and browser matrix runner
```

Dependencies flow only from adapters down to core. `framework-core` has no runtime dependency on Cucumber or TestNG.

## Installing (consumer projects)

Published on the GitLab Maven Package Registry — this is a **private** registry, so resolving
dependencies needs auth too, not just deploying. Add the repository to your consumer project's
`pom.xml`:

```xml
<repositories>
    <repository>
        <id>gitlab-maven</id>
        <url>http://10.0.0.40/api/v4/groups/qc-quality-control/-/packages/maven</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>vn.ots.qc</groupId>
        <artifactId>framework-testng</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <scope>test</scope>
    </dependency>
    <!-- or framework-cucumber-testng for BDD -->
</dependencies>
```

Then add the matching server credentials to your **local** `~/.m2/settings.xml` (a personal
access token, scope `read_api` is enough — do not reuse a `write_repository`/`api`-scoped
token for this):

```xml
<settings>
  <servers>
    <server>
      <id>gitlab-maven</id>
      <configuration>
        <httpHeaders>
          <property>
            <name>Private-Token</name>
            <value>your-personal-access-token</value>
          </property>
        </httpHeaders>
      </configuration>
    </server>
  </servers>
</settings>
```

### Consumer project structure

The framework ships no pages, steps, or features — that's the consumer's own layout, following
standard Maven test conventions:

```text
your-app-repo/
├── src/test/java/com/yourcompany/
│   ├── pages/              # page objects, extend BasePage
│   └── tests/               # TestNG tests, extend BaseWebTest
├── src/test/resources/
│   ├── config/
│   │   └── application.properties   # BASE_URL, browser targets, device profiles
│   └── features/            # *.feature, if using framework-cucumber-testng
└── pom.xml
```

## Dynamic locators

Consumers define logical locators and fallbacks in a stable order:

```java
private static final WebLocator EMAIL = WebLocator.of(
    "account.email",
    WebLocators.testId("account-email"),
    WebLocators.label("Email"),
    WebLocators.css("input[name='email']")
);

private static final WebLocator SAVE = WebLocator.of(
    "account.save",
    WebLocators.role(AriaRole.BUTTON, "Save")
);
```

Pages and steps call actions using logical locators:

```java
actions.fill(EMAIL, email);
actions.click(SAVE);
```

The framework evaluates candidates in declaration order, uses web-first waits, and reports the logical name and attempted strategies. Logs never contain filled values. Use the sensitive variant for passwords and tokens:

```java
actions.fillSensitive(PASSWORD, password);
```

There is no mechanism that guesses XPath from text. Role, label, and test ID are preferred; CSS and XPath are transparent consumer-defined fallbacks.

Component scope prevents a locator from matching an element in an unrelated region:

```java
ElementActions dialog = actions.within(ACCOUNT_DIALOG);
dialog.fill(EMAIL, email);
dialog.click(SAVE);
```

Files and browser events also use logical locators:

```java
actions.upload(IMPORT_FILE, Path.of("employees.xlsx"));
Path file = events.downloadTo(() -> actions.click(EXPORT), Path.of("target/downloads/export.xlsx"));
BrowserEvents.DialogResult dialog = events.acceptNextDialog(() -> actions.click(DELETE));
Page popup = events.popup(() -> actions.click(OPEN_DETAILS));
```

Event listeners are always registered before the trigger action. Download destinations are normalized, and uploads fail fast when the file does not exist.

## TestNG usage

```java
public final class AccountTest extends BaseWebTest {
    @Test(dataProvider = "browserTargets")
    public void savesAccount(BrowserTarget target) {
        actions().fill(EMAIL, "qa@example.test");
        actions().click(SAVE);
    }
}
```

Every method gets a separate `WebTestContext`. `@AfterMethod(alwaysRun = true)` always closes the browser and publishes evidence paths to `ITestResult`.

## Cucumber and TestNG usage

```java
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.yourcompany.tests", "vn.ots.qc.web.cucumber"}
)
public final class RunCucumberTest extends AbstractMatrixCucumberTest {
}
```

Steps inject `CucumberWorld` through their constructors. Generic hooks manage the browser and evidence lifecycle, so application projects do not copy lifecycle code.

## Application configuration

Create `src/test/resources/config/application.properties` in the consumer:

```properties
navigation.enabled=true
navigation.base-url=https://your-application.example

browser.targets=chromium,firefox,webkit@qa-phone
browser.headless=true

device.qa-phone.viewport.width=390
device.qa-phone.viewport.height=844
device.qa-phone.scale-factor=3
device.qa-phone.mobile=true
device.qa-phone.touch=true
device.qa-phone.user-agent=Your test user agent
```

Device names are normalized into property prefixes; the framework does not hard-code a device catalog. `QA Phone` maps to `device.qa-phone.*`.

Configuration precedence:

```text
JVM system property
  → environment variable
  → config.file / CONFIG_FILE
  → consumer config/application.properties
  → config/framework.properties
```

The framework supports viewport, locale, timezone, permissions, geolocation, custom browser executables, expiring storage state, video, and a configurable browser matrix.

## Evidence and security

When a test fails:

- screenshots mask passwords and `[data-sensitive='true']` elements;
- HTML is produced from a DOM clone with sensitive content replaced by `<redacted>`;
- traces are stored under `target/evidence` when enabled;
- Cucumber attaches screenshots and HTML by default, but does not attach trace ZIP files to avoid placing excessive sensitive data in reports.

Consumers can extend `evidence.mask-selector` for application-specific UI. Artifacts and storage state live under `target` and must not be committed.

## Verification

Unit and contract tests:

```bash
./mvnw clean verify
```

Browser integration contract:

```bash
./mvnw -Pbrowser-contract clean verify
```

CI runs the `browser-contract` profile in a Playwright image pinned to the same version. The real integration test verifies logical locator fallback, component scope, fill, click, upload, download, dialog, and popup behavior on Chromium.

## Publishing

`.gitlab-ci.yml` has a `deploy` stage mirroring the `api-framework`/`playwright-framework-typescript` pattern:

- Push a `vX.Y.Z` tag → bumps every module to that release version and deploys to the GitLab Maven registry.
- Push to `develop` → deploys the current `-SNAPSHOT` version (Maven allows overwriting SNAPSHOTs, unlike release versions).

See [architecture](docs/ARCHITECTURE.md).
