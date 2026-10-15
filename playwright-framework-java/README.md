# Playwright Automation Framework (Java)

Framework automation web có thể tái sử dụng cho nhiều dự án. Source chỉ chứa hạ tầng automation; URL, selector, page, step, credential, và test data thuộc về repo ứng dụng tiêu dùng.

## Module

```text
playwright-framework-java/
├── framework-core/                 # Playwright, locator, lifecycle, cấu hình, evidence
├── framework-testng/               # TestNG thuần, độc lập với Cucumber
└── framework-cucumber-testng/      # Cucumber world, hooks, và browser matrix runner
```

Dependency chỉ chảy từ adapter xuống core. `framework-core` không có runtime dependency nào tới Cucumber hay TestNG.

## Cài đặt (dự án consumer)

Artifacts không publish lên Maven Central. Dự án mới nên bắt đầu từ
[`template-consumer-java`](../template-consumer-java/README.md), template độc lập dùng các artifact
trong GitLab Maven Package Registry. Nếu cần thử framework trực tiếp từ source, clone và install
reactor vào local Maven repository:

```bash
git clone https://github.com/vocongthanh247/playwright-framework.git
cd playwright-framework/playwright-framework-java
./mvnw clean install
```

Sau đó thêm dependency vào `pom.xml` của dự án consumer:

```xml
<dependency>
    <groupId>vn.ots.qc</groupId>
    <artifactId>framework-testng</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
<!-- hoặc framework-cucumber-testng cho BDD -->
```

Với Registry, dùng project-scoped Maven repository, server ID `playwright-framework-registry`,
và job-token allowlist như hướng dẫn trong template. Project ID được truyền qua
`PLAYWRIGHT_FRAMEWORK_PROJECT_ID`; không ghi token vào POM. Local install ở trên dành cho phát
triển framework hoặc khi chưa cấu hình quyền đọc Registry.

### Cấu trúc dự án consumer

Framework không đóng gói sẵn page, step, hay feature nào — đó là layout riêng của consumer, theo
đúng convention test chuẩn của Maven:

```text
your-app-repo/
├── src/test/java/com/yourcompany/
│   ├── pages/              # page object, extend BasePage
│   └── tests/               # TestNG test, extend BaseWebTest
├── src/test/resources/
│   ├── config/
│   │   └── application.properties   # BASE_URL, browser targets, device profiles
│   └── features/            # *.feature, nếu dùng framework-cucumber-testng
└── pom.xml
```

## Locator động (dynamic locator)

Consumer định nghĩa locator logic và các phương án dự phòng theo thứ tự cố định:

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

Page và step gọi action bằng locator logic:

```java
actions.fill(EMAIL, email);
actions.click(SAVE);
```

Framework đánh giá các candidate theo đúng thứ tự khai báo, dùng web-first wait, và báo lại tên logic cùng các chiến lược đã thử. Log không bao giờ chứa giá trị đã điền. Dùng biến thể sensitive cho mật khẩu và token:

```java
actions.fillSensitive(PASSWORD, password);
```

Không có cơ chế nào tự đoán XPath từ text. Role, label, và test ID được ưu tiên; CSS và XPath là fallback minh bạch do consumer tự định nghĩa.

Component scope ngăn 1 locator match nhầm phần tử ở vùng không liên quan:

```java
ElementActions dialog = actions.within(ACCOUNT_DIALOG);
dialog.fill(EMAIL, email);
dialog.click(SAVE);
```

File và browser event cũng dùng locator logic:

```java
actions.upload(IMPORT_FILE, Path.of("employees.xlsx"));
Path file = events.downloadTo(() -> actions.click(EXPORT), Path.of("target/downloads/export.xlsx"));
BrowserEvents.DialogResult dialog = events.acceptNextDialog(() -> actions.click(DELETE));
Page popup = events.popup(() -> actions.click(OPEN_DETAILS));
```

Event listener luôn được đăng ký trước action kích hoạt. Đường dẫn download được chuẩn hóa, và upload fail nhanh nếu file không tồn tại.

## Dùng với TestNG

```java
public final class AccountTest extends BaseWebTest {
    @Test(dataProvider = "browserTargets")
    public void savesAccount(BrowserTarget target) {
        actions().fill(EMAIL, "qa@example.test");
        actions().click(SAVE);
    }
}
```

Mỗi method có 1 `WebTestContext` riêng. `@AfterMethod(alwaysRun = true)` luôn đóng browser và publish đường dẫn evidence vào `ITestResult`.

## Dùng với Cucumber và TestNG

```java
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.yourcompany.tests", "vn.ots.qc.web.cucumber"}
)
public final class RunCucumberTest extends AbstractMatrixCucumberTest {
}
```

Step inject `CucumberWorld` qua constructor. Hook chung quản lý lifecycle browser và evidence, nên dự án ứng dụng không phải copy code lifecycle.

## Cấu hình ứng dụng

Tạo `src/test/resources/config/application.properties` bên consumer:

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

Tên device được chuẩn hóa thành tiền tố property; framework không hard-code catalog device nào. `QA Phone` map thành `device.qa-phone.*`.

Thứ tự ưu tiên cấu hình:

```text
JVM system property
  → environment variable
  → config.file / CONFIG_FILE
  → consumer config/application.properties
  → config/framework.properties
```

Framework hỗ trợ viewport, locale, timezone, permission, geolocation, browser executable tùy chỉnh, storage state có hạn dùng, video, và browser matrix cấu hình được.

## Evidence và bảo mật

Khi 1 test fail:

- screenshot che mật khẩu và các phần tử `[data-sensitive='true']`;
- HTML được tạo từ DOM clone với nội dung nhạy cảm thay bằng `<redacted>`;
- trace được lưu dưới `target/evidence` khi bật;
- Cucumber đính kèm screenshot và HTML mặc định, nhưng không đính kèm file trace ZIP để tránh đưa quá nhiều dữ liệu nhạy cảm vào report.

Consumer có thể mở rộng `evidence.mask-selector` cho UI riêng của ứng dụng. Artifact và storage state nằm dưới `target` và không được commit.

## Kiểm thử (verification)

Unit test và contract test:

```bash
./mvnw clean verify
```

Contract tích hợp browser thật:

```bash
./mvnw -Pbrowser-contract clean verify
```

CI chạy profile `browser-contract` trên image Playwright pin đúng version. Integration test thật verify locator fallback, component scope, fill, click, upload, download, dialog, và popup behavior trên Chromium.

## Publishing

`.gitlab-ci.yml` có `deploy` stage theo đúng pattern `api-framework`/`playwright-framework-typescript`:

- Push tag `vX.Y.Z` → bump tất cả module lên version release đó và deploy lên GitLab Maven registry.
- Push lên `develop` → deploy version `-SNAPSHOT` hiện tại (Maven cho phép ghi đè SNAPSHOT, khác version release).

## So sánh kiến trúc với `playwright-framework-typescript`

Cả 2 framework theo đúng cùng 1 kiến trúc 3 lớp: **consumer sở hữu app** → **framework nội bộ
(`vn.ots.qc`/`@ots`) sở hữu cơ chế dùng chung** → **thư viện bên thứ 3 làm việc nặng** (điều
khiển browser thật). Java "dày" hơn ở lớp giữa vì Playwright Java **không đi kèm test runner/
fixture** như Playwright Node — nhiều class ở đây tồn tại chỉ để bù lại thứ mà `@playwright/test`
(TS) đã có sẵn natively.

| Nhóm | Java (`vn.ots.qc.web.*`) | Vai trò | TS (`@ots/playwright-core-ts`) |
|---|---|---|---|
| **Page** | `page.BasePage` | Base cho page object của consumer | `base-page.ts` — `BasePage` |
| | `page.BaseComponent` | Base cho component, giữ locator con trong scope gốc | — (TS chain `Locator` con trực tiếp) |
| **Locator** | `locator.WebLocators` | Factory tạo locator theo semantic | `locators.ts` — `role/label/text/testId/css` |
| | `locator.WebLocator` | Locator logic bất biến, có fallback candidate | `locators.ts` — `dynamicLocator` |
| | `locator.LocatorResolver` | Resolve candidate theo thứ tự | — (`Locator.or()` gốc của Playwright) |
| | `locator.LocatorStrategy` | Interface 1 chiến lược locate | `locators.ts` — type `LocatorStrategy` |
| | `locator.LocatorCandidate` | 1 cách cụ thể để locate 1 phần tử | — (gộp vào `LocatorStrategy`) |
| | `locator.LocatorDefinition` | Contract locator logic do app định nghĩa | — (dùng function trực tiếp, không cần interface) |
| | `locator.ResolvedLocator` | Locator đã resolve + tên chiến lược | — (`Locator.describe()` có sẵn) |
| | `locator.LocatorResolutionException` | Lỗi khi không resolve được | — (Playwright tự throw timeout error) |
| **Browser/session** | `browser.BrowserFactory` | Launch browser theo config | — (`playwright.config.ts` + `projects`) |
| | `browser.BrowserContextOptionsFactory` | Build context options từ config | — (`use: {...}` trong config) |
| | `browser.BrowserSession` | Sở hữu resource Playwright theo scenario | — (fixture `page`/`context` tự quản lý) |
| | `browser.BrowserTarget`, `ExecutionTarget` | Target browser cho adapter song song | — (`projects` trong config) |
| | `browser.BrowserTargetMatrix` | Resolve ma trận browser/device | — (`projects` + `devices`) |
| | `browser.DeviceProfile` | Device profile custom | — (`devices['...']` có sẵn) |
| **Config** | `config.FrameworkConfig` | Config layered, bất biến | — (`process.env`/`dotenv` qua `config`/`config-local`) |
| | `config.FrameworkConfigValidator` | Validate config trước khi launch | **gap thật** — TS chưa có |
| | `config.ConfigurationException` | Lỗi config | — (dùng `Error` thường) |
| **Context/lifecycle** | `context.WebTestContext` | Composition root theo từng test | — (fixture `test`/`page`) |
| | `testng.BaseWebTest` | Lifecycle TestNG | `fixtures.ts` — `test`/`expect` |
| | `testng.TestArtifactReporter` | Publish đường dẫn artifact | — (`@playwright/test` tự có reporter) |
| **Interaction** | `interaction.ElementActions` | Action web-first, không log giá trị | — (gọi thẳng `.fill()`/`.click()` trên `Locator`) |
| | `interaction.BrowserEvents` | Đăng ký event trước khi trigger | — (`page.waitForEvent()` gốc) |
| **Evidence** | `artifact.ArtifactPaths` | Sinh đường dẫn artifact an toàn | — (`@playwright/test` tự đặt tên file) |
| | `artifact.EvidenceCollector` | Thu evidence, che field nhạy cảm | — (`screenshot: 'only-on-failure'` có sẵn) |
| | `artifact.EvidenceBundle` | Gói evidence trả về | — (report HTML tự gộp) |
| | `artifact.TraceRecorder` | Quản lý trace theo context | — (`trace: 'on-first-retry'` có sẵn) |
| **Auth** | `auth.StorageStateManager` | Load/lưu storage state, có hạn dùng | **gap thật** — TS có `storageState` option nhưng chưa có cơ chế hạn dùng |
| **Cucumber** | `cucumber.*` (3 class) | Runner + hook + world cho BDD | **gap thật** — TS chưa có adapter BDD tương đương |

Phần lớn cột TS là "—" không phải vì framework Java làm thừa, mà vì Node/Playwright Test đã cho
sẵn cơ chế đó (fixture lifecycle, browser launch, trace/evidence, reporter) ở tầng ngôn ngữ — Java
phải tự viết để đạt cùng mức năng lực. 3 dòng đánh dấu **gap thật** là chỗ TS thực sự còn thiếu so
với Java.

Xem thêm [architecture](docs/ARCHITECTURE.md).
