package vn.ots.qc.web.locator;

import java.util.List;

/** Immutable logical locator with ordered fallback candidates. */
public record WebLocator(String logicalName, List<LocatorCandidate> candidates) implements LocatorDefinition {
    public WebLocator {
        if (logicalName == null || logicalName.isBlank()) {
            throw new IllegalArgumentException("Logical locator name must not be blank");
        }
        logicalName = logicalName.trim();
        candidates = List.copyOf(candidates);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("At least one locator candidate is required");
        }
    }

    public static WebLocator of(String logicalName, LocatorCandidate... candidates) {
        return new WebLocator(logicalName, List.of(candidates));
    }
}
