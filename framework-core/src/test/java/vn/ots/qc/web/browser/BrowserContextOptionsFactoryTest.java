package vn.ots.qc.web.browser;

import com.microsoft.playwright.Browser;
import org.testng.annotations.Test;
import vn.ots.qc.web.config.FrameworkConfig;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public final class BrowserContextOptionsFactoryTest {
    @Test
    public void createsDesktopContextFromConfiguration() {
        FrameworkConfig config = FrameworkConfig.load().withOverrides(Map.of(
                "context.viewport.width", "1600",
                "context.viewport.height", "1000",
                "context.locale", "vi-VN",
                "context.permissions", "geolocation, clipboard-read"));

        Browser.NewContextOptions options = new BrowserContextOptionsFactory()
                .create(config, new BrowserTarget("chromium", ""));

        assertEquals(options.viewportSize.get().width, 1600);
        assertEquals(options.viewportSize.get().height, 1000);
        assertEquals(options.locale, "vi-VN");
        assertTrue(options.permissions.contains("geolocation"));
    }

    @Test
    public void createsCustomDeviceWithoutDeviceNamesInSourceCode() {
        FrameworkConfig config = FrameworkConfig.load().withOverrides(Map.of(
                "device.qa-phone.viewport.width", "390",
                "device.qa-phone.viewport.height", "844",
                "device.qa-phone.scale-factor", "3",
                "device.qa-phone.mobile", "true",
                "device.qa-phone.touch", "true",
                "device.qa-phone.user-agent", "QA mobile agent"));

        Browser.NewContextOptions options = new BrowserContextOptionsFactory()
                .create(config, new BrowserTarget("webkit", "QA Phone"));

        assertEquals(options.viewportSize.get().width, 390);
        assertEquals(options.deviceScaleFactor, 3.0);
        assertTrue(options.isMobile);
        assertEquals(options.userAgent, "QA mobile agent");
    }
}
