# Playwright + TypeScript Framework

Framework automation Playwright + TypeScript có thể tái sử dụng, đóng gói thành thư viện npm cài
đặt được — bản TypeScript song song với `playwright-framework-java` (`framework-core`/
`framework-testng`/`framework-cucumber-testng`). Tách y hệt bên Java: package này chỉ sở hữu cơ
chế automation. Repo ứng dụng tiêu dùng sở hữu page object, test, locator sản phẩm, và
credential — không bao giờ ngược lại.

## Cấu trúc

```text
playwright-framework-typescript/
├── src/                 # source của package được publish
│   ├── base-page.ts     # BasePage — extend trong page object của consumer
│   ├── locators.ts       # helper chiến lược locator role/label/text/testId/css
│   ├── fixtures.ts        # re-export test/expect — điểm mở rộng của framework
│   └── index.ts           # public API barrel (những gì `dist/index.js` expose ra)
├── test/                 # contract test CỦA CHÍNH framework (không code sản phẩm, không fixture app)
├── dist/                 # build output (bị git-ignore), publish lên npm
├── playwright.config.ts
├── tsconfig.json          # config dev/typecheck
├── tsconfig.build.json    # config `npm run build` — compile src/ -> dist/
├── eslint.config.mjs
└── .prettierrc.json
```

Framework không đóng gói sẵn file `config`/`config-local` nào — đó là quy ước của consumer (xem
bên dưới), giống hệt Java không ship `application.properties` trong chính repo framework.

`src/` không bao giờ chứa page object hay test gắn với 1 sản phẩm cụ thể — làm vậy sẽ phá vỡ khả
năng tái sử dụng, y hệt việc `framework-core` (Java) mà mọc ra 1 page riêng cho sản phẩm nào đó.

## Cài đặt (dự án consumer)

Package này không publish lên npm registry công khai. Với dự án consumer, dùng
[`template-consumer-typescript`](../template-consumer-typescript/README.md), cấu hình `.npmrc` cho
GitLab Package Registry nội bộ và cài version đã publish. Template giải thích domain/project ID,
token, lockfile, browser binaries và CI.

Khi phát triển framework, có thể clone và build source local:

```bash
git clone https://github.com/vocongthanh247/playwright-framework.git framework-playwright
cd framework-playwright/playwright-framework-typescript
npm install
npm run build
```

Rồi tham chiếu package qua đường dẫn local (`npm install ../framework-playwright/playwright-framework-typescript`
hoặc `npm link`) để thử thay đổi framework trước khi publish. Đây là đường phát triển local, không
phải quy trình khởi tạo consumer dùng chung.

`@playwright/test` là peer dependency — cài nó ở dự án consumer luôn (thường đã có sẵn, vì đó là
test runner của bạn).

## CI/CD và đồng bộ source

Repo GitLab độc lập `<GITLAB_GROUP>/playwright-framework-typescript` là source chính. Source pipeline
chạy typecheck, lint và build, không chạy contract/product test; khi các bước compile qua trên `develop`, job
`mirror_personal_github` cập nhật riêng thư mục `playwright-framework-typescript/` trong repo
GitHub cá nhân `vocongthanh247/playwright-framework` trên nhánh `main`. Job không force-push và
không xóa file ngoài thư mục TypeScript.

Để bật đồng bộ, tạo GitLab CI/CD variable `GITHUB_PUSH_TOKEN` ở dạng masked và protected. Token
GitHub fine-grained chỉ cần quyền **Contents: Read and write** trên repo
`vocongthanh247/playwright-framework`. Bảo vệ nhánh `develop` để chỉ pipeline tin cậy nhận được
biến này. Token không được lưu trong source.

Job `deploy` hiện tại vẫn xuất bản prerelease `-dev.<sha>` lên dist-tag `next` khi push vào
`develop`; release stable chỉ xuất bản khi tạo tag `vX.Y.Z`.

### Cấu trúc dự án consumer

Package không đóng gói sẵn `pages/`/`tests/` — đó là layout riêng của consumer, tạo 1 lần:

```text
your-app-repo/
├── pages/          # page object của bạn, extend BasePage từ @ots/playwright-core-ts
├── tests/          # *.spec.ts, import { test, expect } từ @ots/playwright-core-ts
├── config          # giá trị mặc định cho product, commit vào git (BASE_URL môi trường staging...)
├── config-local    # override riêng của máy bạn, gitignored (BASE_URL local, secret...)
├── .npmrc          # registry + auth, như trên
└── playwright.config.ts
```

`config` chứa giá trị dùng chung, an toàn để commit (giống `application.properties` bên Java).
`config-local` chỉ tồn tại trên máy bạn, không commit — dùng khi cần override giá trị trong
`config` (BASE_URL trỏ tới môi trường local, credential cá nhân...). `playwright.config.ts` nên
load cả 2, `config-local` load sau để đè lên `config`:

```typescript
dotenv.config({ path: 'config' });
dotenv.config({ path: 'config-local', override: true });
```

## Cách dùng

```typescript
import { test, expect, BasePage, role, testId } from '@ots/playwright-core-ts';

class LoginPage extends BasePage {
  readonly email = testId('email')(this.page);
  readonly submit = role('button', { name: 'Sign in' })(this.page);

  login(user: string) {
    return this.email.fill(user).then(() => this.submit.click());
  }
}

test('signs in', async ({ page }) => {
  const loginPage = new LoginPage(page);
  await loginPage.goto('/login');
  await loginPage.login('qa@example.test');
  await expect(page).toHaveURL(/dashboard/);
});
```

Luôn extend `test`/`expect` từ package này, không lấy thẳng từ `@playwright/test` — đó là điểm
nối duy nhất mà framework dùng để thêm hành vi cross-cutting sau này (thu evidence, chính sách
storage-state, validate config) mà không consumer nào phải đổi import.

## Quy ước

- **Locator**: chọn đúng 1 locator cho mỗi phần tử theo thứ tự ưu tiên của Playwright —
  `role` → `label`/`placeholder` → `text` → `testId` → `css` là phương án cuối. Role/label/text
  resolve dựa vào accessibility tree, nên vẫn hoạt động qua các lần đổi markup/CSS mà không cần
  sửa code. Locator chỉ sống trong page object, không bao giờ viết inline trong test.
- **Dynamic locator**: dùng `dynamicLocator(name, ...strategies)` để ghép các cách định vị thay
  thế cho cùng một control, chẳng hạn trong giai đoạn chuyển đổi markup. Hàm dùng `Locator.or()`
  của Playwright nên đây là phép hợp locator, không phải fallback lần lượt; thứ tự candidate
  không quyết định candidate nào thắng. Tại thời điểm thao tác, chỉ một candidate nên khớp. Nếu
  nhiều candidate khớp các phần tử khác nhau, Playwright strict mode sẽ báo locator mơ hồ.
  `name` được gắn qua `Locator.describe()` để lỗi/report hiển thị tên logic thay vì chuỗi selector.

  ```typescript
  readonly email = dynamicLocator(
    'account.email',
    testId('account-email'),
    label('Email'),
    css("input[name='email']"),
  )(this.page);
  ```
- **Page object**: class thuần extend `BasePage`, expose field `Locator` và action method, do
  repo app consumer sở hữu — không bao giờ commit vào package này.
- **Assertion**: web-first, tự động retry `expect(locator)...` — không bao giờ tự poll/`sleep`
  thủ công.
- **Async**: mọi lệnh gọi Playwright đều là `Promise`; luôn `await` nó.
- `eitherVariant()` bọc `Locator.or()` cho đúng 1 trường hợp nó sinh ra: 2 trạng thái UI thực sự
  khác nhau chiếm cùng 1 vị trí (biến thể A/B, feature flag), chỉ 1 trong 2 tồn tại trong DOM tại
  1 thời điểm. Đừng dùng nó để nối chuỗi các phỏng đoán không chắc chắn cho cùng 1 phần tử.

## Lệnh

```bash
npm install
npx playwright install        # chỉ lần đầu, tải browser binary

npm run build                 # compile src/ -> dist/ (entry point main/types)
npm test                      # chạy contract test local của chính framework
npm run test:ui                # chế độ UI tương tác
npm run typecheck              # tsc --noEmit
npm run lint                   # eslint .
npm run format                 # prettier --write .
```

## Publishing

Publish lên npm registry nội bộ, theo cùng quy trình package với
`framework-api`. Source pipeline typecheck/lint/build, không chạy contract test; `.gitlab-ci.yml` có `deploy` stage:

- Push tag `vX.Y.Z` → publish đúng version đó dưới dist-tag `latest`. Đây là đường duy nhất
  consumer nên cài từ đó.
- Push lên `develop` → publish bản prerelease `-dev.<sha>` dưới dist-tag `next` (version npm là
  bất biến, khác Maven SNAPSHOT, nên `develop` không thể republish cùng 1 version).

Publish thủ công: `npm version <x.y.z> && npm publish && git push --tags`.

## Cấu hình

`playwright.config.ts` đọc `BASE_URL` từ environment (qua `dotenv`, xem mục "Cấu trúc dự án
consumer" ở trên) — không có target mặc định do framework sở hữu, khớp với bên Java, cũng không
bao giờ hard-code URL của consumer. Trên CI (`process.env.CI` được set) nó bật retry, 1 worker, và reporter
`list` + `html` + `junit`; local thì dùng reporter `html`, không retry.

## So sánh kiến trúc với `playwright-framework-java`

Cả 2 framework theo đúng cùng 1 kiến trúc 3 lớp: **consumer sở hữu app** → **framework nội bộ
(`vn.ots.qc`/`@ots`) sở hữu cơ chế dùng chung** → **thư viện bên thứ 3 làm việc nặng** (điều
khiển browser thật). Khác biệt là Java phải tự viết nhiều ở lớp giữa, vì Playwright Java không đi
kèm test runner/fixture như Playwright Node — dưới đây là map đầy đủ, lấy từ đúng 32 class thật
trong `framework-core`/`framework-testng`/`framework-cucumber-testng` bên Java.

| Nhóm | Java (`vn.ots.qc.web.*`) | Vai trò | TS (`@ots/playwright-core-ts`) |
|---|---|---|---|
| **Page** | `page.BasePage` | Base cho page object của consumer | `base-page.ts` — `BasePage` |
| | `page.BaseComponent` | Base cho component, giữ locator con trong scope gốc | — (TS chain `Locator` con trực tiếp) |
| **Locator** | `locator.WebLocators` | Factory tạo locator theo semantic | `locators.ts` — `role/label/text/testId/css` |
| | `locator.WebLocator` | Locator logic bất biến, kết hợp candidate thay thế | `locators.ts` — `dynamicLocator` (`Locator.or()`, không ưu tiên theo thứ tự) |
| | `locator.LocatorResolver` | Chọn candidate theo thứ tự | — (không tương đương; `Locator.or()` tạo phép hợp locator) |
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
| **Cucumber** | `cucumber.*` (3 class) | Runner + hook + world cho BDD | **gap thật** — chưa có, xem phần BDD bên dưới |

Phần lớn cột TS là "—" không phải vì thiếu sót, mà vì `@playwright/test` (bên thứ 3) đã làm sẵn
việc đó natively (fixture lifecycle, browser launch, trace/evidence, reporter) — Java phải tự viết
vì Playwright Java không đi kèm runner. 3 dòng đánh dấu **gap thật** là những chỗ TS thực sự thiếu
so với Java, không phải do nền tảng đã lo sẵn.

### Về Cucumber/BDD

TS hiện **chưa có** adapter BDD tương đương `framework-cucumber-testng`. Cân nhắc thêm chỉ khi
business/BA thật sự đọc feature file (không phải QA tự viết tự đọc) — nếu đúng nhu cầu đó,
`playwright-bdd` là lựa chọn hiện đại, giữ được trace viewer/fixture gốc của Playwright Test thay
vì dùng `@cucumber/cucumber` thô.
