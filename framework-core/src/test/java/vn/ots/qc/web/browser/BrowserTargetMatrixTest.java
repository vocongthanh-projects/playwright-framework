package vn.ots.qc.web.browser;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import vn.ots.qc.web.config.FrameworkConfig;

import java.util.Map;

import static org.testng.Assert.assertEquals;

public final class BrowserTargetMatrixTest {
    @AfterMethod
    public void clearTarget() {
        ExecutionTarget.clear();
    }

    @Test
    public void buildsMultipleConfiguredTargets() {
        FrameworkConfig config = FrameworkConfig.load().withOverrides(Map.of(
                "browser.targets", "chromium, firefox@tablet, webkit"));

        assertEquals(BrowserTargetMatrix.from(config).size(), 3);
        assertEquals(BrowserTargetMatrix.from(config).get(1), new BrowserTarget("firefox", "tablet"));
    }

    @Test
    public void threadTargetOverridesConfiguredDefault() {
        FrameworkConfig config = FrameworkConfig.load();
        ExecutionTarget.set(new BrowserTarget("webkit", ""));

        assertEquals(BrowserTargetMatrix.current(config), new BrowserTarget("webkit", ""));
    }
}
