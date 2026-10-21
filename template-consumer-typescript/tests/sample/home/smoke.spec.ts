import { test, expect } from '../../../fixtures';

test.describe('Consumer Template - Smoke Verification', () => {
  test('proves test runner, custom fixtures, and alternative dynamic locator resolve correctly', async ({
    samplePage,
  }) => {
    // Static HTML document providing only one of the alternative candidates (role='button')
    // to verify that dynamicLocator resolves cleanly without triggering strict mode ambiguity.
    const sampleHtml =
      'data:text/html,<h1>Sample Application</h1><button>Submit</button><div id="status">Operation Success</div>';

    await samplePage.goto(sampleHtml);

    await expect(samplePage.heading).toBeVisible();
    await expect(samplePage.submitButton).toBeVisible();
    await expect(samplePage.statusMessage).toBeVisible();
  });
});
