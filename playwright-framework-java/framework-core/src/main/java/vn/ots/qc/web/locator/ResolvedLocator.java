package vn.ots.qc.web.locator;

import com.microsoft.playwright.Locator;

public record ResolvedLocator(Locator locator, LocatorCandidate candidate) {
}
