import { BasePage, role, text, testId, dynamicLocator } from '@ots/playwright-core-ts';

/**
 * Neutral Sample Page Object demonstrating how consumer pages extend BasePage
 * and utilize framework locator helpers.
 */
export class SamplePage extends BasePage {
  readonly heading = role('heading', { name: 'Sample Application' })(this.page);

  /**
   * dynamicLocator combines candidate locators via Playwright's native Locator.or().
   * IMPORTANT: Because Locator.or() resolves to any matching element in the DOM (not a procedural
   * try/catch loop), these represent ALTERNATIVE candidates (e.g. across markup migrations or A/B
   * experiments) where only ONE candidate should be present/match at any given time to avoid strict
   * mode ambiguity.
   */
  readonly submitButton = dynamicLocator(
    'sample.submit_button',
    testId('sample-submit-btn'),
    role('button', { name: 'Submit' })
  )(this.page);

  readonly statusMessage = text(/Success/i)(this.page);

  async clickSubmit(): Promise<void> {
    await this.submitButton.click();
  }
}
