# 🎭 Playwright + TypeScript Consumer Template

> Bộ khung mẫu khởi tạo dự án kiểm thử tự động Web UI (Consumer Test Repository) dành cho các sản phẩm mới, tích hợp sẵn thư viện hạ tầng `@ots/playwright-core-ts`.

---

## 1. Phân Biệt: Framework Package vs Template Consumer vs Dự Án Thật

| Tiêu chí | Framework Package (`@ots/playwright-core-ts`) | Template Consumer (Repository này) | Dự Án Sản Phẩm Thật (Sau khi clone từ template) |
| :--- | :--- | :--- | :--- |
| **Bản chất** | **Thư viện nền tảng (Shared Library)** | **Bộ khung mẫu rỗng (Starter Kit / Skeleton)** | **Repository kiểm thử sản phẩm cụ thể** (vd: `ui-product`) |
| **Trách nhiệm** | Cung cấp `BasePage`, helper định vị (`role`, `dynamicLocator`), `test`, `expect` | Cung cấp cấu trúc thư mục, cấu hình runner, fixtures mở rộng, smoke test giả lập | Cung cấp Page Object nghiệp vụ, kịch bản test sản phẩm thật |
| **Dữ liệu & Credentials** | **Không** chứa URL, không selector sản phẩm, không credential | **Chỉ chứa Placeholder mẫu** (`REPLACE_ME`); hoàn toàn KHÔNG có dữ liệu thật | Chứa URL thật của môi trường (staging/dev) và tài khoản test thật |
| **Cách dùng** | Được import vào `fixtures.ts` và `pages/` | Dùng làm gốc để tạo repo Git mới cho sản phẩm | Thực thi hàng ngày trên local máy tester và CI/CD |

---

## 2. Cách Tạo Repo Dự Án Mới Từ Template Này

### Cách A: Dùng chức năng GitLab Template (Khuyến nghị)
1. Lên GitLab nội bộ của tổ chức.
2. Chọn **New Project** ➡️ **Create from template** (hoặc chọn template nếu nhóm đã đánh dấu).
3. Đặt tên cho dự án mới (ví dụ: `ui-product`, `ui-portal-test`).

### Cách B: Copy thư mục thủ công
```bash
# 1. Copy toàn bộ thư mục template sang thư mục dự án mới
cp -r /path/to/template-consumer-typescript /path/to/my-product-tests
cd /path/to/my-product-tests

# 2. Khởi tạo Git mới (nếu bắt đầu từ đầu)
git init
```

Sau khi tạo xong, mở file `package.json` và sửa lại trường `"name"` cho phù hợp với sản phẩm của bạn:
```json
{
  "name": "ui-my-product-tests",
  "version": "1.0.0",
  "private": true
}
```

---

## 3. Cài Đặt Dependencies & Cấu Hình Registry

Package `@ots/playwright-core-ts` được lưu trữ trên **GitLab Package Registry**.

### Bước 1: Cấu hình file `.npmrc`
File `.npmrc` đã được chuẩn bị sẵn mẫu cấu hình. Bạn thay thế `<GITLAB_DOMAIN>` (luôn ưu tiên HTTPS để bảo vệ token khi truyền trên mạng) và `<PROJECT_ID>` của repo framework:
```properties
@ots:registry=https://<GITLAB_DOMAIN>/api/v4/projects/<PROJECT_ID>/packages/npm/
//<GITLAB_DOMAIN>/api/v4/projects/<PROJECT_ID>/packages/npm/:_authToken=${GITLAB_TOKEN}
```

### Bước 2: Cung cấp `GITLAB_TOKEN`
* **Trên máy cá nhân (Local):**
  Lấy Personal Access Token từ GitLab (cần quyền `read_package_registry`) và lưu vào cấu hình shell máy:
  ```bash
   Không ghi token thật vào README, `.zshrc` hoặc câu lệnh có thể nằm trong shell history.
   Khi chạy local, nạp token từ secret manager hoặc file `.env` bị Git ignore và giới hạn quyền đọc; trong CI, dùng GitLab CI/CD variable `GITLAB_TOKEN` dạng masked/protected.
  source ~/.zshrc
  ```
* **Trên CI/CD (GitLab Runner):**
  Vào **Settings ➡️ CI/CD ➡️ Variables** của repo mới trên GitLab, thêm biến `GITLAB_TOKEN` (khuyến nghị tích chọn ô **Mask variable**). Tuyệt đối không commit token trực tiếp vào file mã nguồn.

### Bước 3: Cài đặt packages & Khởi tạo Lockfile
```bash
# 1. Cài đặt toàn bộ dependencies và sinh ra package-lock.json chính xác cho dự án:
npm install
```

> 💡 **Lưu ý về Lockfile (`package-lock.json`):**
> Template này **chủ đích không commit sẵn file `package-lock.json`** vì endpoint registry phụ thuộc vào domain và Project ID của từng tổ chức. Khi người dùng clone template về, điền `.npmrc` thật và chạy `npm install`, file `package-lock.json` sẽ tự động được tạo khớp với registry đó. **Hãy commit file `package-lock.json` mới sinh đó vào Git của dự án consumer.**

Template có `.gitlab-ci.yml` dùng image Playwright 1.63.0, chạy `npm ci`, typecheck và smoke suite. Trước pipeline đầu tiên, consumer cần cấu hình `.npmrc`, cấp quyền `CI_JOB_TOKEN` đọc package framework, sinh rồi commit `package-lock.json`. CI tự dùng `CI_JOB_TOKEN` nếu project không cấp riêng `GITLAB_TOKEN`.

### Bước 4: Cài đặt Browser Binaries (Local vs CI)
Lưu ý rằng việc khai báo browser trong `playwright.config.ts` **không tự động tải binary** về máy hay runner.

* **Trên máy cá nhân (Local dev):**
  Cài đặt 2 trình duyệt phục vụ chạy local theo quy ước nhóm:
  ```bash
  npx playwright install chromium webkit
  ```
* **Trên CI/CD Runner:**
  Cần cài đặt đầy đủ cả 3 trình duyệt (bao gồm Firefox) cùng các thư viện hệ điều hành:
  ```bash
  npx playwright install --with-deps chromium webkit firefox
  ```

> ⚠️ **Quy tắc khớp Docker Image với Playwright:**
> Template pin `@playwright/test` ở `1.63.0` để khớp image CI `mcr.microsoft.com/playwright:v1.63.0-noble`. Consumer phải commit lockfile sau lần `npm install` đầu tiên; nếu nâng Playwright, nâng đồng bộ lockfile và tag image.

---

## 4. Chiến Lược Ma Trận Trình Duyệt (Browser Matrix)

Cấu hình trong `playwright.config.ts` được phân định rõ theo quy ước kiểm thử của nhóm:
* **Ở máy cá nhân (Local dev):** Mặc định chạy song song **Chromium** và **WebKit (Safari Engine)** để tối ưu tốc độ phản hồi, giảm tải tài nguyên và phục vụ vòng lặp phản hồi nhanh của kiểm thử viên.
* **Trên hệ thống CI/CD (`process.env.CI`):** Tự động mở rộng thêm **Firefox** (`...(process.env.CI ? [{ name: 'firefox', ... }] : [])`) để đảm bảo ma trận kiểm thử toàn diện trên 3 trình duyệt chính.

> 💡 **Ghi chú về việc chạy Firefox tại local:**
> Đây là chính sách phân bổ môi trường kiểm thử chủ động của nhóm. Sự cố khởi chạy Firefox (như thông báo lỗi khởi tạo thư mục profile) là hiện tượng quan sát được trong một số môi trường máy trạm cá nhân cụ thể (chẳng hạn như ghi nhận tại [Playwright Issue #42768](https://github.com/microsoft/playwright/issues/42768)), không phải đặc tính cố hữu của mọi máy macOS. Việc đưa Firefox về thực thi chính trên môi trường Linux CI Runner tiêu chuẩn giúp đảm bảo tính đồng nhất, loại bỏ các biến số phụ thuộc vào cấu hình máy trạm cá nhân. Kỹ sư vẫn có thể chạy Firefox tại local khi cần bằng lệnh: `CI=true npx playwright test --project=firefox`.

---

## 5. Cấu Hình Môi Trường: `config` vs `config-local`

Hệ thống sử dụng cơ chế **2 file cấu hình phân tách** để bảo vệ an toàn thông tin:

```text
my-product-tests/
├── config                # [1] Cấu hình mặc định dùng chung (COMMIT vào Git - chỉ chứa placeholder)
├── config-local.example  # [2] File mẫu hướng dẫn cấu hình cá nhân (COMMIT vào Git - chứa placeholder)
└── config-local          # [3] Cấu hình thực tế trên máy cá nhân (ĐÃ ĐƯỢC .gitignore BỎ QUA)
```

### Cách tạo và sử dụng `config-local`:
1. Copy file mẫu ra file chạy cá nhân:
   ```bash
   cp config-local.example config-local
   ```
2. Mở file `config-local` và thay thế các giá trị `REPLACE_ME` bằng URL thật, tài khoản/mật khẩu kiểm thử:
   ```properties
   BASE_URL=https://staging.myproduct.example
   HEADLESS=false
   TEST_USERNAME=REPLACE_ME_WITH_REAL_USER
   TEST_PASSWORD=REPLACE_ME_WITH_REAL_PASS
   ```
3. Trong `playwright.config.ts`, cơ chế `dotenv` sẽ đọc file `config` trước, sau đó cho phép `config-local` ghi đè:
   ```typescript
   dotenv.config({ path: 'config' });
   dotenv.config({ path: 'config-local', override: true });
   ```

> ⚠️ **Lưu ý bảo mật:**
> Việc đưa `config-local` vào `.gitignore` là **hàng rào kỹ thuật phòng ngừa (guardrail)** nhằm hạn chế nguy cơ vô tình commit file chứa credential cá nhân lên Git. Đây không phải là giải pháp bảo mật tuyệt đối nếu máy trạm bị xâm nhập. Trên môi trường CI/CD, toàn bộ secret kiểm thử phải được quản lý tập trung qua **GitLab CI/CD Variables** (được cấu hình Protected & Masked).

---

## 6. Lưu Ý Kỹ Thuật Về `dynamicLocator`

Hàm `dynamicLocator` trong framework kết hợp các chiến lược định vị qua phương thức native `Locator.or()` của Playwright:
* **Bản chất:** Đây là danh sách các **locator thay thế lẫn nhau (Alternative Candidates)** (ví dụ: dùng trong giai đoạn chuyển đổi markup hoặc A/B testing).
* **Quy tắc quan trọng:** Vì `Locator.or()` tìm tất cả các phần tử thỏa mãn bất kỳ điều kiện nào (chứ không phải vòng lặp `try/catch` tuần tự), tại một thời điểm trên trang **chỉ nên có duy nhất 1 candidate khớp**. Không nên viết các candidate mà cả 2 cùng đồng thời khớp vào các phần tử trên DOM, vì Playwright sẽ báo lỗi vi phạm chế độ nghiêm ngặt (*strict mode violation*).

---

## 7. Cấu Trúc Thư Mục Chuẩn Cho Consumer

```text
template-consumer-typescript/
├── pages/                 # Nơi chứa các Page Object của sản phẩm
│   └── sample-page.ts     # Page Object mẫu kế thừa BasePage và dùng dynamicLocator thay thế
├── tests/                 # Nơi chứa các kịch bản kiểm thử (*.spec.ts)
│   └── smoke.spec.ts      # Smoke test mẫu chạy trên static HTML ảo (không cần mạng)
├── fixtures.ts            # Nơi mở rộng fixture và tiêm Page Object
├── config                 # Môi trường baseline (chỉ chứa placeholder)
├── config-local.example   # Mẫu override local (chứa placeholder REPLACE_ME)
├── .gitignore             # Bỏ qua node_modules, config-local, test-results
├── .npmrc                 # Mẫu định tuyến registry qua ${GITLAB_TOKEN} (HTTPS)
├── playwright.config.ts   # Cấu hình test runner Playwright
├── tsconfig.json          # Cấu hình TypeScript compiler (Strict mode)
└── package.json           # Quản lý script và dependencies
```

---

## 8. Các Lệnh Thực Thi Thường Dùng

| Lệnh Terminal | Mục đích |
| :--- | :--- |
| `npm test` | Chạy toàn bộ test suite (mặc định headless trên Chromium & WebKit) |
| `npm run test:headed` | Chạy test mở giao diện trình duyệt trực quan |
| `npm run test:ui` | Mở giao diện Playwright UI Runner tương tác cao |
| `npm run report` | Mở báo cáo HTML kết quả kiểm thử |
| `npm run typecheck` | Soi lỗi kiểu dữ liệu TypeScript mà không chạy test |
