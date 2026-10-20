# Playwright Automation Framework

A reusable web automation framework for multiple projects. The source contains only automation infrastructure; URLs, selectors, pages, steps, credentials, and test data belong to the consuming application repository.

## Modules

```text
framework-playwright/
├── playwright-framework-java/          # Java + Maven framework (this document)
│   ├── framework-core/                 # Playwright, locators, lifecycle, configuration, evidence
│   ├── framework-testng/               # plain TestNG, independent of Cucumber
│   └── framework-cucumber-testng/      # Cucumber world, hooks, and browser matrix runner
├── playwright-framework-typescript/    # Playwright + TypeScript framework, independent stack
├── template-consumer-java/             # independent Java consumer starter
└── template-consumer-typescript/       # independent TypeScript consumer starter
```

The two stacks are kept in separate top-level folders and never share code or dependencies —
see [`docs/JAVA_VS_TYPESCRIPT.md`](docs/JAVA_VS_TYPESCRIPT.md) for a syntax comparison.

Use the matching starter when creating an application test repository:
[`template-consumer-java`](template-consumer-java/README.md) or
[`template-consumer-typescript`](template-consumer-typescript/README.md). Each is an independent
consumer project and is not part of its framework build reactor.

Dependencies flow only from adapters down to core. `framework-core` has no runtime dependency on Cucumber or TestNG.

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

```xml
<dependency>
    <groupId>vn.ots.qc</groupId>
    <artifactId>framework-testng</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

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

```xml
<dependency>
    <groupId>vn.ots.qc</groupId>
    <artifactId>framework-cucumber-testng</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

```java
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.yourcompany.tests", "vn.ots.qc.web.cucumber"}
)
public final class RunCucumberTest extends AbstractMatrixCucumberTest {
}
```

Steps inject `CucumberWorld` through their constructors. Generic hooks manage the browser and evidence lifecycle, so application projects do not copy lifecycle code.

## TypeScript usage

The TypeScript sibling (`playwright-framework-typescript/`) follows the same split — see its
own [README](playwright-framework-typescript/README.md) for full usage. Quick version:

```bash
npm install @ots/playwright-core-ts
```

```typescript
import { test, expect, BasePage, role, testId } from '@ots/playwright-core-ts';

class LoginPage extends BasePage {
  readonly email = testId('email')(this.page);
  readonly submit = role('button', { name: 'Sign in' })(this.page);
}

test('signs in', async ({ page }) => {
  const loginPage = new LoginPage(page);
  await loginPage.goto('/login');
});
```

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

Run from the `playwright-framework-java/` directory:

Unit and contract tests:

```bash
cd playwright-framework-java && ./mvnw clean verify
```

Browser integration contract:

```bash
cd playwright-framework-java && ./mvnw -Pbrowser-contract clean verify
```

These commands are for local verification. OTS/FLI source pipelines only typecheck, lint, build, and compile; they do not run browser tests. Run the browser contract suite from the dedicated test infrastructure.

See [architecture](docs/ARCHITECTURE.md) and the [quality audit](docs/QUALITY_AUDIT.md).
