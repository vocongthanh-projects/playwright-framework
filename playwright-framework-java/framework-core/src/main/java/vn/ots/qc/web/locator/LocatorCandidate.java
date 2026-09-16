package vn.ots.qc.web.locator;

import com.microsoft.playwright.options.AriaRole;

import java.util.Objects;

/** One concrete way to locate a logical element. */
public record LocatorCandidate(LocatorStrategy strategy, String value, AriaRole role, boolean exact) {
    public LocatorCandidate {
        strategy = Objects.requireNonNull(strategy, "strategy");
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Locator value must not be blank");
        }
        value = value.trim();
        if (strategy == LocatorStrategy.ROLE && role == null) {
            throw new IllegalArgumentException("ROLE locator requires an ARIA role");
        }
        if (strategy != LocatorStrategy.ROLE && role != null) {
            throw new IllegalArgumentException("ARIA role is only valid for ROLE locators");
        }
    }

    public String diagnostic() {
        return strategy == LocatorStrategy.ROLE ? "ROLE(" + role + ")" : strategy.name();
    }
}
