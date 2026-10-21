package vn.ots.qc.consumer.template.portals.sample.modules.home.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import vn.ots.qc.web.config.FrameworkConfig;
import vn.ots.qc.web.locator.WebLocator;
import vn.ots.qc.web.locator.WebLocators;
import vn.ots.qc.web.page.BasePage;

/** Neutral example page; replace its locators with application-owned identifiers. */
public final class SamplePage extends BasePage {
    private static final WebLocator HEADING = WebLocator.of(
            "sample.heading", WebLocators.role(AriaRole.HEADING, "Consumer template ready"));
    private static final WebLocator CONTINUE = WebLocator.of(
            "sample.continue", WebLocators.role(AriaRole.BUTTON, "Continue"));
    private static final WebLocator STATUS = WebLocator.of(
            "sample.status", WebLocators.testId("sample-status"));

    public SamplePage(Page page, FrameworkConfig config) {
        super(page, config);
    }

    public boolean isReady() {
        return actions.isVisible(HEADING);
    }

    public void continueFlow() {
        actions.click(CONTINUE);
    }

    public void shouldShowContinuedStatus() {
        actions.shouldHaveText(STATUS, "continued");
    }
}
