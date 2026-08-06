# Comparative quality audit

Ba framework được chấm trên cùng rubric: reusable, không phụ thuộc ứng dụng, config hóa, lifecycle an toàn, dynamic locator/request, bảo mật evidence, runner độc lập, test contract, dependency pinning và CI tái tạo được.

| Framework | Score | Verification evidence |
|---|---:|---|
| API | 10/10 | 5 module, 28 executed verification tests |
| Appium | 10/10 | 3 module, 21 executed verification tests + 1 abstract runner entry point |
| Playwright | 10/10 | 3 module, 29 executed verification tests + 1 abstract runner entry point |

## API

- Request model và transport adapter tách rời; retry chỉ chạy với idempotent request.
- Service URL, timeout, retry và environment được validate fail-fast; absolute request path và URI chứa credential bị từ chối.
- JSON, form text, bearer token, error và trace đều đi qua redaction.
- Fixture ledger cleanup theo LIFO, vẫn cleanup phần còn lại khi một resource lỗi.
- Starter chỉ minh họa consumer boundary, không đưa endpoint/credential sản phẩm vào framework.
- Multipart upload và binary download là transport-neutral; trace không chứa file content hoặc filename.

## Appium

- Core, TestNG và Cucumber-TestNG tách module; không còn screen, step, feature, locator hay test data của ứng dụng cụ thể.
- `MobileLocator` dùng logical name cùng ordered fallback riêng Android/iOS; action không log dữ liệu được nhập.
- Config nhiều tầng và validator kiểm tra endpoint, app ID, timeout, reset policy, port trước khi tạo session.
- Evidence gồm screenshot và XML đã redact; XML parser chặn DOCTYPE, external entity và XInclude.
- Appium 3 cùng Android/iOS driver được pin. CI tái tạo bằng `npm ci`, audit dependency và kiểm tra driver registry.
- Device matrix thay thế runner/hook nhân bản và chặn UDID/port collision trước session creation.

## Playwright

- Core, TestNG và Cucumber-TestNG tách module, dependency chỉ hướng xuống core.
- Logical locator có ordered fallback và component scope; role/label/test-id là first-class strategy.
- Browser context cô lập theo test/scenario, matrix target và device profile do consumer cấu hình.
- Screenshot mask dữ liệu nhạy cảm; HTML và trace tuân theo evidence policy.
- Unit/adapter contracts cộng với browser contract thật kiểm tra fallback, scope, fill và click.
- Browser contract thật còn kiểm tra upload, download, dialog và popup theo event-safe API.

“10/10” nghĩa là cả ba đạt đầy đủ acceptance rubric đã công bố và verification gate hiện tại; không có nghĩa dependency sẽ không cần tiếp tục được cập nhật trong tương lai.

`@Test` trên abstract Cucumber runner là entry point dành cho consumer subclass, không phải verification test do reactor tự chạy. CI của cả ba repository dùng GitLab CI với cùng policy: merge request và nhánh `main`.
