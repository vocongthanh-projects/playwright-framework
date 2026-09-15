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

Publish trên GitLab Maven Package Registry — đây là registry **private**, nên resolve
dependency cũng cần auth, không chỉ deploy. Thêm repository vào `pom.xml` của dự án consumer:

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
    <!-- hoặc framework-cucumber-testng cho BDD -->
</dependencies>
```

Sau đó thêm credential tương ứng vào `~/.m2/settings.xml` **local** của bạn (personal access
token, scope `read_api` là đủ — đừng dùng lại token có scope `write_repository`/`api` cho việc
này):

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

Xem thêm [architecture](docs/ARCHITECTURE.md).
