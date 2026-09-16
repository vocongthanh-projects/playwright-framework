package vn.ots.qc.web.browser;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public final class BrowserTargetTest {
    @Test
    public void parsesBrowserAndDeviceProfile() {
        BrowserTarget target = BrowserTarget.parse("chromium@iPhone 13");

        assertEquals(target.browserName(), "chromium");
        assertEquals(target.deviceKey(), "iphone-13");
        assertEquals(target.displayName(), "chromium | iPhone 13");
    }

    @Test
    public void rejectsUnsupportedBrowser() {
        expectThrows(IllegalArgumentException.class, () -> BrowserTarget.parse("edge"));
    }

    @Test
    public void rejectsBlankBrowser() {
        expectThrows(IllegalArgumentException.class, () -> BrowserTarget.parse(" "));
    }

    @Test
    public void rejectsNullTargetWithDomainError() {
        expectThrows(IllegalArgumentException.class, () -> BrowserTarget.parse(null));
    }
}
