package vn.ots.qc.web.locator;

/** Locator strategies ordered by the consumer from most to least stable. */
public enum LocatorStrategy {
    ROLE,
    LABEL,
    TEST_ID,
    PLACEHOLDER,
    ALT_TEXT,
    TITLE,
    TEXT,
    CSS,
    XPATH
}
