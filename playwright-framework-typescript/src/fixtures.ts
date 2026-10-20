import { test, expect } from '@playwright/test';

/**
 * Single extension point for the whole framework. Consumers extend `test` from here (never
 * straight from `@playwright/test`) to inject their own page-object fixtures — the same seam
 * Java's `BaseWebTest`/`WebTestContext` play, so future cross-cutting concerns (evidence capture,
 * storage-state policy, config validation) can be added here once and reach every consumer.
 */
export { test, expect };
