package vn.ots.qc.web.page;

import com.microsoft.playwright.Page;
import vn.ots.qc.web.config.FrameworkConfig;
import vn.ots.qc.web.interaction.ElementActions;
import vn.ots.qc.web.locator.LocatorDefinition;
import vn.ots.qc.web.locator.ResolvedLocator;

/** Shared base for application-owned page objects. */
public abstract class BasePage {

    protected final Page page;
    protected final ElementActions actions;
    protected final FrameworkConfig config;

    protected BasePage(Page page, FrameworkConfig config) {
        this.page = page;
        this.config = config;
        this.actions = new ElementActions(page, config);
    }

    protected ResolvedLocator locate(LocatorDefinition definition) {
        return actions.resolve(definition);
    }
}
