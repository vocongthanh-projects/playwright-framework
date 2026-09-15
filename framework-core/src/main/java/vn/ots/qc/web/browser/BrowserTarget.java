package vn.ots.qc.web.browser;

import java.util.Locale;

public record BrowserTarget(String browserName, String deviceName) {
    public BrowserTarget {
        if (browserName == null || browserName.isBlank()) {
            throw new IllegalArgumentException("Browser name must not be blank");
        }
        browserName = browserName.trim().toLowerCase(Locale.ROOT);
        deviceName = deviceName == null ? "" : deviceName.trim();
        if (!browserName.equals("chromium") && !browserName.equals("firefox") && !browserName.equals("webkit")) {
            throw new IllegalArgumentException("Unsupported browser: " + browserName);
        }
    }

    public static BrowserTarget parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Browser target must not be blank");
        }
        String[] parts = value.trim().split("@", 2);
        return new BrowserTarget(parts[0], parts.length == 2 ? parts[1] : "");
    }

    public String deviceKey() {
        return deviceName.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    public String displayName() {
        return deviceName.isBlank() ? browserName : browserName + " | " + deviceName;
    }
}
