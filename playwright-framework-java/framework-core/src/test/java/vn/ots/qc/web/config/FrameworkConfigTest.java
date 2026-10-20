package vn.ots.qc.web.config;

import org.testng.annotations.Test;

import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public final class FrameworkConfigTest {
    @Test
    public void systemPropertyOverridesEnvironmentAndDefaults() {
        Properties system = new Properties();
        system.setProperty("browser.type", "webkit");

        FrameworkConfig config = FrameworkConfig.load(system, Map.of("BROWSER_TYPE", "firefox"));

        assertEquals(config.required("browser.type"), "webkit");
    }

    @Test
    public void environmentOverridesDefaults() {
        FrameworkConfig config = FrameworkConfig.load(new Properties(), Map.of("BROWSER_TYPE", "firefox"));

        assertEquals(config.required("browser.type"), "firefox");
    }

    @Test
    public void requiredSettingFailsFast() {
        FrameworkConfig config = FrameworkConfig.load(new Properties(), Map.of());

        expectThrows(ConfigurationException.class, () -> config.required("application.missing"));
    }

    @Test
    public void invalidTypedSettingFailsFast() {
        Properties system = new Properties();
        system.setProperty("timeout.default", "fast");
        FrameworkConfig config = FrameworkConfig.load(system, Map.of());

        expectThrows(ConfigurationException.class, config::defaultTimeout);
    }

    @Test
    public void coreDoesNotContainApplicationSettings() {
        FrameworkConfig config = FrameworkConfig.load(new Properties(), Map.of());

        assertTrue(config.get("application.selector").isBlank());
        assertTrue(config.get("application.base-url").isBlank());
        assertTrue(config.get("application.credential").isBlank());
    }
}
