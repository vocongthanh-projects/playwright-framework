# Architecture

## Dependency direction

```text
application tests
   ├── framework-testng ───────────┐
   └── framework-cucumber-testng ─┤
                                  ▼
                           framework-core
                                  ▼
                           Playwright Java
```

Core sở hữu automation mechanics. Consumer sở hữu application behavior.

| Framework owns | Application owns |
|---|---|
| layered config và validation | URL theo môi trường |
| browser/context/page lifecycle | page/component objects |
| logical locator contract và resolver | logical locator definitions |
| browser/device matrix | feature, steps và test data |
| storage-state policy | login flow và credentials |
| trace, screenshot, redacted HTML, video | business assertions |
| TestNG/Cucumber adapters | tags và suite policy |

Không thêm selector, tọa độ, nội dung UI, tài khoản hoặc step mang tên sản phẩm vào framework.

## Locator pipeline

```text
logical locator
  → ordered semantic candidates
  → optional component scope
  → per-candidate web-first visibility probe
  → resolved Playwright Locator
  → action/assertion
```

Supported strategies: role, label, test ID, placeholder, alt text, title, text, CSS và XPath. Consumer quyết định fallback order; resolver không tự sinh selector hoặc retry action có side effect.

Playwright đã có actionability checks và auto-wait. Framework chỉ retry việc resolve candidate; không tự click lại vì có thể gửi form hoặc tạo dữ liệu hai lần.

## Execution lifecycle

```text
resolve config and target
  → fail-fast validation
  → launch browser
  → build isolated context options
  → load fresh storage state (optional)
  → start trace (optional)
  → create page and navigate (optional)
  → execute test behavior
  → capture masked evidence on failure
  → persist storage state (optional)
  → close context, browser and Playwright
```

Mỗi test/scenario có một `WebTestContext`. Mutable `Page` và `BrowserContext` không được chia sẻ giữa worker. Target Cucumber chỉ tồn tại trong `ThreadLocal` trong thời gian scenario chạy và luôn được clear trong `finally`.

## Failure and security policy

- Config sai dừng trước browser launch.
- Evidence được lấy khi page còn sống; resource đóng trong `finally`.
- Capture screenshot và HTML độc lập, nên lỗi một artifact không làm mất artifact còn lại.
- Action logs chỉ chứa logical locator name và strategy, không chứa field value.
- Screenshot dùng mask selector; page source được redacted trên DOM clone.
- Trace có thể chứa dữ liệu nhạy cảm nên mặc định không attach vào Cucumber report.
- Tên artifact được sanitize trước khi tạo filesystem path.

## Extension points

- Consumer triển khai `LocatorDefinition` trực tiếp hoặc dùng `WebLocator`.
- Page kế thừa `BasePage`; component có root scope kế thừa `BaseComponent`.
- Project không dùng BDD chọn `framework-testng`.
- Project dùng BDD chọn `framework-cucumber-testng` và inject `CucumberWorld`.
- Reporting vendor có thể đọc `EvidenceBundle` từ TestNG result hoặc Cucumber attachment mà không thay core.
- `BrowserEvents` quản lý download, popup và dialog theo đúng register-before-trigger contract; file input vẫn đi qua logical locator.
