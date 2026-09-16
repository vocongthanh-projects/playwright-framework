import type { Locator, Page } from '@playwright/test';

export type LocatorStrategy = (page: Page) => Locator;

/**
 * Priority hierarchy (Playwright docs + community consensus): prefer role/label/text —
 * they resolve against the accessibility tree, so they survive markup/CSS changes without
 * edits. Fall back to testId, then css, only when nothing accessible identifies the element.
 */
export const role =
  (...args: Parameters<Page['getByRole']>): LocatorStrategy =>
  (page) =>
    page.getByRole(...args);

export const label =
  (text: string | RegExp): LocatorStrategy =>
  (page) =>
    page.getByLabel(text);

export const text =
  (content: string | RegExp): LocatorStrategy =>
  (page) =>
    page.getByText(content);

export const testId =
  (id: string): LocatorStrategy =>
  (page) =>
    page.getByTestId(id);

export const css =
  (selector: string): LocatorStrategy =>
  (page) =>
    page.locator(selector);

/**
 * Combines two locators that represent genuinely different UI states of the same slot
 * (e.g. an A/B variant or a feature-flagged component) — NOT alternative guesses at the same
 * element. Only one of the two should ever be present in the DOM at a time; if both can match
 * simultaneously, Playwright's strict mode will correctly flag it as ambiguous.
 */
export function eitherVariant(a: Locator, b: Locator): Locator {
  return a.or(b);
}

/**
 * A named locator with ordered fallback strategies — the TypeScript counterpart to Java's
 * `WebLocator.of(name, ...)`. Unlike `eitherVariant`, these candidates ARE alternative guesses
 * at the same element: use it when a single strategy (e.g. only `testId`) is one markup change
 * away from breaking and you want the framework to keep resolving through role/label/css
 * fallbacks without a code change. Candidates are tried in the order given — put the most
 * accessible-tree-based strategy first, `css` last.
 *
 * Resolution itself is Playwright's own `Locator.or()` (native auto-waiting, no hand-rolled
 * polling); `name` is attached via `Locator.describe()` so failures and reports show the
 * logical name instead of a raw selector chain.
 */
export function dynamicLocator(name: string, ...strategies: LocatorStrategy[]): LocatorStrategy {
  if (strategies.length === 0) {
    throw new Error(`dynamicLocator("${name}") needs at least one strategy`);
  }
  return (page) =>
    strategies
      .map((strategy) => strategy(page))
      .reduce((combined, candidate) => combined.or(candidate))
      .describe(name);
}
