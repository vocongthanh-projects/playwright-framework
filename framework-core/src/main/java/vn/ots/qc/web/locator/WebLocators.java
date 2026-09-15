package vn.ots.qc.web.locator;

import com.microsoft.playwright.options.AriaRole;

/** Semantic locator factories. CSS and XPath remain explicit fallbacks. */
public final class WebLocators {
    private WebLocators() {
    }

    public static LocatorCandidate role(AriaRole role, String accessibleName) {
        return new LocatorCandidate(LocatorStrategy.ROLE, accessibleName, role, true);
    }

    public static LocatorCandidate role(AriaRole role, String accessibleName, boolean exact) {
        return new LocatorCandidate(LocatorStrategy.ROLE, accessibleName, role, exact);
    }

    public static LocatorCandidate label(String label) {
        return candidate(LocatorStrategy.LABEL, label, true);
    }

    public static LocatorCandidate testId(String testId) {
        return candidate(LocatorStrategy.TEST_ID, testId, true);
    }

    public static LocatorCandidate placeholder(String placeholder) {
        return candidate(LocatorStrategy.PLACEHOLDER, placeholder, true);
    }

    public static LocatorCandidate altText(String text) {
        return candidate(LocatorStrategy.ALT_TEXT, text, true);
    }

    public static LocatorCandidate title(String title) {
        return candidate(LocatorStrategy.TITLE, title, true);
    }

    public static LocatorCandidate text(String text) {
        return candidate(LocatorStrategy.TEXT, text, true);
    }

    public static LocatorCandidate text(String text, boolean exact) {
        return candidate(LocatorStrategy.TEXT, text, exact);
    }

    public static LocatorCandidate css(String selector) {
        return candidate(LocatorStrategy.CSS, selector, true);
    }

    public static LocatorCandidate xpath(String expression) {
        return candidate(LocatorStrategy.XPATH, expression, true);
    }

    private static LocatorCandidate candidate(LocatorStrategy strategy, String value, boolean exact) {
        return new LocatorCandidate(strategy, value, null, exact);
    }
}
