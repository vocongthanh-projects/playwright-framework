import type { Page } from '@playwright/test';

export class BasePage {
  constructor(readonly page: Page) {}

  goto(path = '/') {
    return this.page.goto(path);
  }
}
