package vn.ots.qc.web.config;

import vn.ots.qc.web.browser.BrowserTarget;
import vn.ots.qc.web.browser.DeviceProfile;

import java.nio.file.Files;

/** Validates a complete execution configuration before Playwright is started. */
public final class FrameworkConfigValidator {
    private FrameworkConfigValidator() {
    }

    public static void validate(FrameworkConfig config, BrowserTarget target) {
        positive(config.defaultTimeout().toMillis(), "timeout.default");
        positive(config.navigationTimeout().toMillis(), "timeout.navigation");
        positive(config.getInt("locator.resolve.timeout"), "locator.resolve.timeout");
        nonNegative(config.getDouble("browser.slow-motion", 0), "browser.slow-motion");
        config.optionalPath("browser.executable-path").ifPresent(path -> {
            if (!Files.isExecutable(path)) {
                throw new ConfigurationException("Browser executable is not executable: " + path);
            }
        });
        if (target.deviceName().isBlank()) {
            positive(config.getInt("context.viewport.width"), "context.viewport.width");
            positive(config.getInt("context.viewport.height"), "context.viewport.height");
        } else {
            DeviceProfile.from(config, target);
        }
        if (config.getBoolean("navigation.enabled")) {
            config.required("navigation.base-url");
        }
        if (config.getBoolean("storage-state.enabled")) {
            config.required("storage-state.path");
            nonNegative(config.getLong("storage-state.ttl-minutes", 60), "storage-state.ttl-minutes");
        }
        if (config.getBoolean("video.enabled")) {
            config.required("video.directory");
            positive(config.getInt("video.width"), "video.width");
            positive(config.getInt("video.height"), "video.height");
        }
    }

    private static void positive(long value, String key) {
        if (value <= 0) {
            throw new ConfigurationException("Setting " + key + " must be greater than zero");
        }
    }

    private static void nonNegative(double value, String key) {
        if (value < 0) {
            throw new ConfigurationException("Setting " + key + " must not be negative");
        }
    }
}
