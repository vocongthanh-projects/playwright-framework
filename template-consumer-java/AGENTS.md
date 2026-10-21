# Consumer Repository AI Instructions

This is a Java consumer starter, not the Playwright framework source. The consumer owns product pages, selectors, scenarios, URLs, credentials, and test data; the framework owns reusable automation infrastructure.

- Read this file, `README.md`, and the framework APIs used by the change before editing.
- Do not add product-specific behavior to the shared framework or invent APIs absent from its published/source version.
- Keep environment values and credentials in local or CI secret configuration, never in source control.
- On GitLab 10, source push pipelines are limited to configuration/dependency checks and compile/package. Run browser suites only through the separately configured test infrastructure or an explicitly requested trigger.
- Do not publish, deploy, push, or trigger a remote pipeline unless explicitly requested.
