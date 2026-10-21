# Playwright Java Consumer Template

Standalone Java 17/Maven/TestNG starter for a new web UI test repository. It consumes published Playwright framework artifacts; it is not part of the framework Maven reactor and contains no product URL, credentials, selectors, or test data.

## What's included

- `framework-testng` dependency and GitLab Maven Registry wiring.
- Typed framework configuration and local override example.
- Neutral `SamplePage` and an offline smoke test using a `data:` document, grouped under `portals/sample/modules/home/`.
- Playwright browser installation profile and a CI job using a matching Playwright Java image.
- Screenshots, HTML evidence, and traces are produced by the framework on failure according to framework configuration.

## Create a consumer repository

Create an empty GitLab repository, clone it, and copy the contents of this template into its root. Then update `groupId`, `artifactId`, project Java packages (`vn.ots.qc.consumer.template`), and the display name. Keep the framework dependency coordinates under `vn.ots.qc.web` unchanged.

## Consumer source layout

Organize web UI source by portal and feature module. Java keeps page objects, reusable components, and behavior tests together; journeys spanning modules live in `workflows/`. See [Consumer Source Layout](../docs/CONSUMER_SOURCE_LAYOUT.md) for the convention shared with API and Appium consumers.

```text
src/test/java/<package_root>/
├── base/ config/ fixtures/                    # consumer wiring and setup
├── portals/<portal>/modules/<module>/
│   ├── pages/                                 # page objects
│   ├── components/                            # reusable UI components, when needed
│   └── tests/                                 # TestNG scenarios
└── workflows/<workflow>/tests/               # journeys across modules
```

For Cucumber, keep `.feature` files under `src/test/resources/features/<portal>/<module>/` and glue under the matching Java package. Keep assertions in tests and browser interactions/page-specific behavior in page objects.
## Install the framework dependency

The template defaults to `1.0.0-SNAPSHOT`. Confirm the artifact exists in the Playwright Framework project's Maven Package Registry first. In the consumer GitLab project, set `PLAYWRIGHT_FRAMEWORK_PROJECT_ID` to the framework project's ID and add this consumer to that framework project's CI job-token allowlist. CI authenticates with `CI_JOB_TOKEN` and the repository ID `playwright-framework-registry`.

For local Registry access, add a matching server to `~/.m2/settings.xml` (keep credentials out of the repository):

```xml
<settings>
  <servers>
    <server>
      <id>playwright-framework-registry</id>
      <username>YOUR_GITLAB_USERNAME_OR_TOKEN_USER</username>
      <password>YOUR_READ_PACKAGE_TOKEN</password>
    </server>
  </servers>
</settings>
```

Then provide the internal GitLab URL and framework project ID:

```bash
./mvnw -Dplaywright.framework.repository.url="https://<GITLAB_HOST>/api/v4/projects/<PROJECT_ID>/packages/maven" clean verify
```

If developing against a local framework checkout, run `./mvnw clean install` from its `playwright-framework-java/` directory, then return to the consumer directory and run `./mvnw clean verify`.

## Browser setup and running

The checked-in application configuration selects Chromium and does not navigate to an application. The smoke test uses a local HTML data URL, so it checks framework wiring without product access.

Install the local Chromium binary and run the smoke test:

```bash
./mvnw -Pinstall-browsers clean verify
```

To install a different browser, pass `-Dplaywright.browser.install=firefox` or `webkit`. On Linux, install required system libraries or use the Playwright Java Docker image version matching the framework's Playwright dependency (`1.63.0`). CI uses that image and runs `clean verify`.

## Configure a product

Copy `config-local.properties.example` to `config-local.properties` for ignored machine-local overrides, then enable navigation and supply the target URL:

```properties
navigation.enabled=true
navigation.base-url=https://your-test-environment.example
browser.targets=chromium
```

Run with `./mvnw -Dconfig.file=config-local.properties test`. CI can set `CONFIG_FILE` or use environment variables; keys map to uppercase names such as `navigation.base-url` → `NAVIGATION_BASE_URL`. Use protected/masked variables or a secret manager for credentials. Do not commit storage state; it contains an authenticated browser session.

For multiple browsers, set `browser.targets=chromium,firefox,webkit`. Install those browser binaries on local machines or use a matching Playwright image in CI. To reuse authenticated browser state, a consumer test can log in with its normal app flow and set `storage-state.enabled=true` plus `storage-state.persist=true`; the framework writes state to `storage-state.path` after the test and reuses it while fresh. Treat that file as a secret and keep it under ignored `target/`. The framework owns browser lifecycle, locator resolution, storage-state handling, and evidence; the consumer owns page objects, selectors, login flow, and scenarios.

The template targets plain TestNG. For a BDD consumer, replace `framework-testng` with `framework-cucumber-testng` and add consumer-owned features/steps as shown in the framework README.

## Commands

```bash
./mvnw clean verify
./mvnw -Dtest=SmokeTest test
./mvnw -Dconfig.file=config-local.properties test
```
