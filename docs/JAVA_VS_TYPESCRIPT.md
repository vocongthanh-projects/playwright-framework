# Java vs TypeScript syntax

Quick reference for switching between `playwright-framework-java/framework-core`/`playwright-framework-java/framework-testng` (Java) and
`playwright-framework-typescript/` (TypeScript). Same concepts, different syntax.

## Variables and types

| Java | TypeScript |
|---|---|
| `String name = "qa";` | `const name = "qa";` |
| `final int count = 3;` | `const count: number = 3;` |
| `var list = new ArrayList<String>();` | `const list: string[] = [];` |
| `Integer age = null;` | `let age: number \| null = null;` |
| Primitive vs boxed (`int` / `Integer`) | Single `number` type |

TypeScript types are erased at compile time (pure JS at runtime); Java types are enforced by
the JVM at runtime too.

## Functions / methods

```java
// Java
public String greet(String name) {
    return "Hello, " + name;
}
```

```typescript
// TypeScript
function greet(name: string): string {
  return `Hello, ${name}`;
}
```

- Java always needs a class wrapper for a function. TypeScript allows free functions and arrow
  functions: `const greet = (name: string): string => \`Hello, ${name}\`;`
- Template literals (`` `${x}` ``) replace `String.format` / `+` concatenation.

## Classes

```java
// Java
public final class LoginPage extends BasePage {
    private final WebLocator email = WebLocator.of("login.email", WebLocators.testId("email"));

    public LoginPage(WebTestContext ctx) {
        super(ctx);
    }

    public void login(String user, String pass) {
        actions().fill(email, user);
    }
}
```

```typescript
// TypeScript
export class LoginPage {
  constructor(private readonly page: Page) {}

  async login(user: string, pass: string): Promise<void> {
    await this.page.getByTestId('email').fill(user);
  }
}
```

- `extends` works the same way in both.
- TypeScript has no method overloading by signature the way Java does (it fakes it via union
  types or optional params).
- `private final field` (Java) ≈ `private readonly field` (TypeScript).
- No `public`/`private` keyword required per member in Java outside classes; TypeScript defaults
  class members to `public` unless marked otherwise.

## Interfaces

```java
// Java
public interface LocatorDefinition {
    Locator resolve(Page page);
}
```

```typescript
// TypeScript
interface LocatorDefinition {
  resolve(page: Page): Locator;
}
```

Nearly identical. TypeScript interfaces are structural (duck-typed) — a class doesn't need
`implements X` to satisfy `X`, as long as its shape matches. Java interfaces are nominal and
must be declared with `implements`.

## Null / optional handling

| Java | TypeScript |
|---|---|
| `Optional<String> value` | `string \| undefined` |
| `Objects.requireNonNull(x)` | non-null assertion `x!` or a runtime check |
| `if (x != null)` | `if (x != null)` or `if (x)` |
| `@Nullable` annotation (convention only) | `string \| null` in the type itself (checked by compiler) |

TypeScript's `strict` mode (enabled in `playwright-framework-typescript/tsconfig.json`) makes `null`/`undefined`
part of the type system, so the compiler flags misuse — Java relies on annotations/convention
or throws `NullPointerException` at runtime.

## Async code

```java
// Java (Playwright Java is synchronous/blocking)
page.click("button#save");
String text = page.textContent("h1");
```

```typescript
// TypeScript (Playwright Node is Promise-based)
await page.click('button#save');
const text = await page.textContent('h1');
```

This is the biggest day-to-day difference for Playwright specifically: every Playwright Node
API call returns a `Promise` and must be `await`ed; Playwright Java calls block the current
thread and return the value directly.

## Collections / streams

```java
// Java
List<String> names = List.of("a", "b", "c");
List<String> upper = names.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());
```

```typescript
// TypeScript
const names: string[] = ['a', 'b', 'c'];
const upper = names.map((n) => n.toUpperCase());
```

`.map`/`.filter`/`.reduce` on arrays replace the Stream API — no separate collect step needed.

## Test structure

```java
// TestNG (framework-testng)
public final class AccountTest extends BaseWebTest {
    @Test(dataProvider = "browserTargets")
    public void savesAccount(BrowserTarget target) {
        actions().fill(EMAIL, "qa@example.test");
        actions().click(SAVE);
    }
}
```

```typescript
// @playwright/test (typescript)
import { test, expect } from '@playwright/test';

test('saves account', async ({ page }) => {
  await page.getByTestId('email').fill('qa@example.test');
  await page.getByRole('button', { name: 'Save' }).click();
});
```

- TestNG uses annotations (`@Test`, `@BeforeMethod`) on class methods; Playwright Test uses
  plain functions passed to `test()`.
- Browser/page setup in TestNG goes through `BaseWebTest` + `WebTestContext`; Playwright Test
  injects a ready-to-use `page` fixture per test automatically — no base class needed.
- Assertions: JUnit/AssertJ/TestNG `Assert.assertEquals(a, b)` vs Playwright's
  `expect(locator).toHaveText(...)` (auto-retrying, web-first assertions built in).

## Build / package tooling

| Java | TypeScript |
|---|---|
| Maven (`pom.xml`, `./mvnw clean verify`) | npm (`package.json`, `npm test`) |
| `import vn.ots.qc.web.locator.WebLocator;` | `import { WebLocator } from './locator';` |
| Compiled `.class` / packaged `.jar` | Compiled to plain `.js` (or run directly via `ts-node`/`tsx`) |
| Package = folder path (`vn/ots/qc/web`) | Module = file path, no folder/package coupling required |

## Takeaways for this repo

- `playwright-framework-java/` (`framework-core`/`framework-testng`/`framework-cucumber-testng`) stays Java + Maven —
  nothing there changes.
- `playwright-framework-typescript/` is a separate, independent stack using `@playwright/test` + npm, in its own
  top-level folder with no shared code or dependencies with `playwright-framework-java/`.
- The main mental shift going from Java to TypeScript Playwright: **await everything**, and
  lean on `getByRole`/`getByTestId` + auto-retrying `expect(locator)` assertions instead of
  building your own locator resolver.
