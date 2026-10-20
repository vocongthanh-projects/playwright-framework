import { test, expect } from '../src/fixtures';
import { BasePage } from '../src/base-page';
import { role, testId, dynamicLocator } from '../src/locators';

/**
 * Contract test for the framework itself — proves BasePage navigation and the locator
 * strategy helpers resolve correctly. Runs against a self-contained data: URL, never a
 * real product, so it stays a framework contract test rather than an application test.
 */
class ContractPage extends BasePage {
  readonly heading = role('heading', { name: 'Framework contract' })(this.page);
  readonly marker = testId('marker')(this.page);
}

test('BasePage and locator helpers resolve against a static document', async ({ page }) => {
  const html =
    'data:text/html,<h1>Framework contract</h1><div data-testid="marker">ok</div>';
  const contractPage = new ContractPage(page);

  await contractPage.goto(html);

  await expect(contractPage.heading).toBeVisible();
  await expect(contractPage.marker).toHaveText('ok');
});

test('dynamicLocator falls through to the next strategy when the first misses', async ({
  page,
}) => {
  // No data-testid on this button — only the first strategy (testId) will miss.
  const html = 'data:text/html,<button aria-label="Save">Save</button>';
  const save = dynamicLocator('actions.save', testId('save-button'), role('button', { name: 'Save' }))(
    page,
  );

  await page.goto(html);

  await expect(save).toBeVisible();
});

test('dynamicLocator rejects an empty strategy list', () => {
  expect(() => dynamicLocator('actions.save')).toThrow(/needs at least one strategy/);
});
