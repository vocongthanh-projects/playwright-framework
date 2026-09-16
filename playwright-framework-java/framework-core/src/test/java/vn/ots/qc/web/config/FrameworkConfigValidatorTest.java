package vn.ots.qc.web.config;

import org.testng.annotations.Test;
import vn.ots.qc.web.browser.BrowserTarget;

import java.util.Map;

import static org.testng.Assert.expectThrows;

public final class FrameworkConfigValidatorTest {
    @Test
    public void rejectsMissingNavigationUrlBeforeBrowserStarts() {
        FrameworkConfig config = FrameworkConfig.load().withOverrides(Map.of(
                "navigation.enabled", "true",
                "navigation.base-url", ""));

        expectThrows(ConfigurationException.class,
                () -> FrameworkConfigValidator.validate(config, new BrowserTarget("chromium", "")));
    }

    @Test
    public void rejectsInvalidViewportBeforeBrowserStarts() {
        FrameworkConfig config = FrameworkConfig.load().withOverrides(Map.of("context.viewport.width", "0"));

        expectThrows(ConfigurationException.class,
                () -> FrameworkConfigValidator.validate(config, new BrowserTarget("chromium", "")));
    }

    @Test
    public void rejectsMissingCustomBrowserExecutableBeforePlaywrightStarts() {
        FrameworkConfig config = FrameworkConfig.load().withOverrides(Map.of(
                "browser.executable-path", "target/browser-that-does-not-exist"));

        expectThrows(ConfigurationException.class,
                () -> FrameworkConfigValidator.validate(config, new BrowserTarget("chromium", "")));
    }
}
