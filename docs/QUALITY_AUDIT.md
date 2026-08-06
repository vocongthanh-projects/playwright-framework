# Comparative quality audit

All three frameworks are evaluated against the same rubric: reusability, application independence, configuration, safe lifecycle management, dynamic locators and requests, evidence security, runner independence, contract tests, dependency pinning, and reproducible CI.

| Framework | Score | Verification evidence |
|---|---:|---|
| API | 10/10 | 5 modules and 28 executed verification tests |
| Appium | 10/10 | 3 modules, 21 executed verification tests, and 1 abstract runner entry point |
| Playwright | 10/10 | 3 modules, 29 executed verification tests, and 1 abstract runner entry point |

## API

- Request models and transport adapters are separated; retry runs only for idempotent requests.
- Service URLs, timeouts, retry policy, and environment are validated fail-fast; absolute request paths and credential-bearing URIs are rejected.
- JSON, form text, bearer tokens, errors, and traces all pass through redaction.
- The fixture ledger cleans up in LIFO order and continues after an individual resource cleanup failure.
- The starter demonstrates the consumer boundary without embedding product endpoints or credentials in the framework.
- Multipart uploads and binary downloads are transport-neutral; traces contain neither file content nor file names.

## Appium

- Core, TestNG, and Cucumber-TestNG are separate modules with no application-specific screens, steps, features, locators, or test data.
- `MobileLocator` uses a logical name with separate ordered Android and iOS fallbacks; actions never log entered values.
- Layered configuration and validation check endpoints, application identifiers, timeouts, reset policies, and ports before session creation.
- Evidence includes screenshots and redacted XML; the XML parser blocks DOCTYPE declarations, external entities, and XInclude.
- Appium 3 and Android/iOS drivers are pinned. CI uses `npm ci`, audits dependencies, and checks the driver registry.
- The device matrix replaces duplicated runners and hooks and rejects UDID and port collisions before session creation.

## Playwright

- Core, TestNG, and Cucumber-TestNG are separate modules with dependencies pointing only toward core.
- Logical locators provide ordered fallbacks and component scope; role, label, and test ID are first-class strategies.
- Each test or scenario has an isolated browser context; consumers configure matrix targets and device profiles.
- Screenshots mask sensitive data, while HTML and traces follow the evidence policy.
- Unit and adapter contracts plus a real browser contract verify fallback, scope, fill, and click behavior.
- The real browser contract also verifies upload, download, dialog, and popup behavior through event-safe APIs.

“10/10” means all three frameworks satisfy the published acceptance rubric and current verification gate. It does not mean their dependencies will never require future updates.

The `@Test` annotation on each abstract Cucumber runner is an entry point for consumer subclasses, not a verification test executed by the reactor. All three repositories use GitLab CI with the same merge-request and `main` branch policy.
