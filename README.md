# Playwright Automation Framework

Framework web automation dùng chung cho nhiều dự án. Source chỉ chứa automation infrastructure; URL, selector, page, steps, credential và test data thuộc repository của ứng dụng sử dụng framework.

## Modules

```text
playwright-framework/
├── framework-core/                 # Playwright, locator, lifecycle, config, evidence
├── framework-testng/               # TestNG thuần, không phụ thuộc Cucumber
└── framework-cucumber-testng/      # Cucumber world, hooks và browser matrix runner
```

Dependency chỉ đi một chiều từ adapter xuống core. `framework-core` không phụ thuộc Cucumber hoặc TestNG ở runtime.

## Dynamic locator

Consumer định nghĩa logical locator và fallback theo thứ tự ổn định:

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

Page hoặc step chỉ gọi action bằng logical locator:

```java
actions.fill(EMAIL, email);
actions.click(SAVE);
```

Framework thử candidate theo đúng thứ tự, dùng web-first wait và báo logical name cùng các strategy đã thử. Log không ghi giá trị được fill. Với password/token:

```java
actions.fillSensitive(PASSWORD, password);
```

Không có cơ chế tự đoán XPath từ text. Role, label và test ID được ưu tiên; CSS/XPath là fallback minh bạch do consumer quyết định.

Component scope ngăn locator bắt nhầm element ở khu vực khác:

```java
ElementActions dialog = actions.within(ACCOUNT_DIALOG);
dialog.fill(EMAIL, email);
dialog.click(SAVE);
```

File và browser event vẫn dùng logical locator:

```java
actions.upload(IMPORT_FILE, Path.of("employees.xlsx"));
Path file = events.downloadTo(() -> actions.click(EXPORT), Path.of("target/downloads/export.xlsx"));
BrowserEvents.DialogResult dialog = events.acceptNextDialog(() -> actions.click(DELETE));
Page popup = events.popup(() -> actions.click(OPEN_DETAILS));
```

Event listener luôn được đăng ký trước action trigger. Download destination được normalize; upload fail-fast nếu file không tồn tại.

## Dùng với TestNG

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

Mỗi method có `WebTestContext` riêng. `@AfterMethod(alwaysRun = true)` luôn đóng browser và publish đường dẫn evidence vào `ITestResult`.

## Dùng với Cucumber + TestNG

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

Steps inject `CucumberWorld` bằng constructor. Generic hooks tự quản lý browser và evidence; project ứng dụng không cần copy lifecycle code.

## Application configuration

Tạo `src/test/resources/config/application.properties` ở consumer:

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

Tên device được chuẩn hóa thành prefix, nên framework không hardcode danh sách thiết bị. `QA Phone` tương ứng `device.qa-phone.*`.

Thứ tự cấu hình:

```text
JVM system property
  → environment variable
  → config.file / CONFIG_FILE
  → config/application.properties của consumer
  → config/framework.properties
```

Framework hỗ trợ viewport, locale, timezone, permissions, geolocation, custom browser executable, storage state có TTL, video và browser matrix qua config.

## Evidence và security

Khi test fail:

- screenshot tự mask password và `[data-sensitive='true']`;
- HTML được tạo từ DOM clone và thay nội dung nhạy cảm bằng `<redacted>`;
- trace được lưu trong `target/evidence` nếu bật;
- Cucumber mặc định attach screenshot và HTML, không attach trace ZIP để tránh làm report chứa quá nhiều dữ liệu nhạy cảm.

Consumer có thể mở rộng `evidence.mask-selector` cho UI riêng. Artifact và storage state nằm dưới `target`, không được commit.

## Verification

Unit và contract tests:

```bash
./mvnw clean verify
```

Browser integration contract:

```bash
./mvnw -Pbrowser-contract clean verify
```

CI chạy profile `browser-contract` trong Playwright image đã pin cùng version. Integration test thật xác nhận logical locator fallback, component scope, fill và click trên Chromium.

Xem [kiến trúc](docs/ARCHITECTURE.md) và [đánh giá chất lượng](docs/QUALITY_AUDIT.md).
