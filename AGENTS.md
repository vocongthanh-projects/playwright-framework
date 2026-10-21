# Repository AI Instructions

This repository contains two separate reusable web automation implementations: Java and TypeScript. Product URLs, selectors, pages, steps, credentials, test data, and business scenarios belong to consumer repositories. Consumer layouts share portal/module ownership while keeping Java and TypeScript runner idioms.

## Before changing files

- Read this file, `README.md`, [consumer source layout](docs/CONSUMER_SOURCE_LAYOUT.md), and the relevant implementation/module documentation.
- Keep the Java and TypeScript implementations independent. Do not assume they share code, dependencies, release versions, or package mechanisms.
- Treat the existing framework and consumer boundary as authoritative. Add a capability to the shared framework only when it is product-neutral; keep application behavior in consumers.
- In Java, group source packages by automation capability inside the Maven modules and mirror them in contract tests. In TypeScript, keep the public surface in `src/index.ts`; group source by capability only when it improves navigation, and keep tests under Playwright's configured test directory.
- Preserve existing package and artifact coordinates unless the task explicitly asks to change them.

## OTS/FLI CI boundary

- A source push pipeline on GitLab 10 must stay lightweight: validate configuration and dependency metadata, then compile/package as configured. Do not add or trigger browser/product test suites on that runner.
- Browser tests run through separately configured test infrastructure, such as an explicit manual or scheduled trigger. Do not assume the GitLab source runner has browser dependencies, network access, application data, or a suitable runtime.
- Do not publish, deploy, push, or trigger remote pipelines unless the user explicitly asks for that action.
