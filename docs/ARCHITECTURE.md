# Architecture

## Source organization

The Java and TypeScript implementations remain independent because their execution models differ. Java uses a small set of Maven modules and capability-oriented packages (`browser`, `locator`, `interaction`, `artifact`); TestNG and Cucumber integrations stay in separate adapter modules. Framework contract tests mirror those package boundaries.

TypeScript keeps the public package surface behind `src/index.ts` and extends Playwright Test through native fixtures. Its source may remain a few focused files while the API is small; split into subdirectories by public capability only when that improves navigation. Do not reproduce Java modules or lifecycle adapters mechanically in TypeScript.

Consumer page objects, components, and tests follow the shared portal/module ownership model. Java uses Maven packages with `pages/components/tests`; TypeScript keeps root Playwright fixtures/config and mirrors portal/module ownership under `pages/` and `tests/`. `service` is an API-only term and is not added to UI source trees. See [Consumer Source Layout](CONSUMER_SOURCE_LAYOUT.md).

```text
Java:       portals/<portal>/modules/<module>/{pages,components,tests}/
TypeScript: pages/<portal>/<module>/, components/<portal>/<module>/,
            tests/<portal>/<module>/*.spec.ts
            fixtures.ts and playwright.config.ts stay at project root
Both:       workflows/<workflow>/tests/ or *.spec.ts for journeys across modules
```

The TypeScript template extends the framework's native Playwright `test` with fixtures; it does not add Java-style base test classes or Maven module structure. Product selectors, pages, fixtures, and assertions remain in consumer repositories.

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

Core owns automation mechanics. Consumers own application behavior.

| Framework owns | Application owns |
|---|---|
| layered configuration and validation | environment-specific URLs |
| browser, context, and page lifecycle | page and component objects |
| logical locator contract and resolver | logical locator definitions |
| browser and device matrix | features, steps, and test data |
| storage-state policy | login flows and credentials |
| traces, screenshots, redacted HTML, and video | business assertions |
| TestNG and Cucumber adapters | tags and suite policy |

Never add product selectors, coordinates, UI content, accounts, or named product steps to the framework.

## Locator pipeline

```text
logical locator
  → ordered semantic candidates
  → optional component scope
  → per-candidate web-first visibility probe
  → resolved Playwright Locator
  → action or assertion
```

Supported strategies include role, label, test ID, placeholder, alt text, title, text, CSS, and XPath. Consumers choose fallback order; the resolver neither generates selectors nor retries actions with side effects.

Playwright already provides actionability checks and auto-waiting. The framework retries only candidate resolution; it does not automatically click twice because doing so could submit a form or create duplicate data.

## Execution lifecycle

```text
resolve configuration and target
  → fail-fast validation
  → launch browser
  → build isolated context options
  → load fresh storage state (optional)
  → start trace (optional)
  → create page and navigate (optional)
  → execute test behavior
  → capture masked evidence on failure
  → persist storage state (optional)
  → close context, browser, and Playwright
```

Every test or scenario has one `WebTestContext`. Mutable `Page` and `BrowserContext` objects are never shared between workers. A Cucumber target exists in `ThreadLocal` only while its scenario runs and is always cleared in `finally`.

## Failure and security policy

- Invalid configuration stops execution before browser launch.
- Evidence is captured while the page is alive; resources close in `finally`.
- Screenshot and HTML capture are independent, so one artifact failure does not suppress the other artifact.
- Action logs contain only logical locator names and strategies, never field values.
- Screenshots use mask selectors; page source is redacted on a DOM clone.
- Traces may contain sensitive data and are not attached to Cucumber reports by default.
- Artifact names are sanitized before filesystem paths are created.

## Extension points

- Consumers implement `LocatorDefinition` directly or use `WebLocator`.
- Pages extend `BasePage`; components with a root scope extend `BaseComponent`.
- Non-BDD projects choose `framework-testng`.
- BDD projects choose `framework-cucumber-testng` and inject `CucumberWorld`.
- Reporting integrations can read `EvidenceBundle` from TestNG results or Cucumber attachments without modifying core.
- `BrowserEvents` manages downloads, popups, and dialogs under the register-before-trigger contract; file input still uses a logical locator.
