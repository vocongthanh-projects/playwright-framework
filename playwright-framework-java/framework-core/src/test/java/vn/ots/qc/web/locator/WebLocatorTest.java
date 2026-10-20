package vn.ots.qc.web.locator;

import com.microsoft.playwright.options.AriaRole;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;
import static vn.ots.qc.web.locator.WebLocators.css;
import static vn.ots.qc.web.locator.WebLocators.role;
import static vn.ots.qc.web.locator.WebLocators.testId;

public final class WebLocatorTest {
    @Test
    public void keepsConsumerFallbackOrder() {
        WebLocator locator = WebLocator.of("checkout.submit",
                testId("checkout-submit"),
                role(AriaRole.BUTTON, "Submit"),
                css("button[type='submit']"));

        assertEquals(locator.candidates().get(0).strategy(), LocatorStrategy.TEST_ID);
        assertEquals(locator.candidates().get(1).strategy(), LocatorStrategy.ROLE);
        assertEquals(locator.candidates().get(2).strategy(), LocatorStrategy.CSS);
    }

    @Test
    public void rejectsInvalidDefinitionsImmediately() {
        expectThrows(IllegalArgumentException.class, () -> new WebLocator("empty", java.util.List.of()));
        expectThrows(IllegalArgumentException.class,
                () -> new LocatorCandidate(LocatorStrategy.ROLE, "Submit", null, true));
    }
}
