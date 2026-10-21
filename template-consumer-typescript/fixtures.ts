import { test as base, expect } from '@ots/playwright-core-ts';
import { SamplePage } from './pages/sample/home/sample-page';

/**
 * Type-safe consumer fixtures.
 * Consumers extend `test` from `@ots/playwright-core-ts` (never directly from `@playwright/test`)
 * to inject page objects or domain-specific context.
 */
type ConsumerFixtures = {
  samplePage: SamplePage;
};

export const test = base.extend<ConsumerFixtures>({
  samplePage: async ({ page }, use) => {
    await use(new SamplePage(page));
  },
});

export { expect };
