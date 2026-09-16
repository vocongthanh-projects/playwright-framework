package vn.ots.qc.web.page;

import com.microsoft.playwright.Page;
import vn.ots.qc.web.config.FrameworkConfig;
import vn.ots.qc.web.interaction.ElementActions;
import vn.ots.qc.web.locator.LocatorDefinition;

/** Base for components whose child locators must stay inside a resolved root. */
public abstract class BaseComponent {
    protected final ElementActions actions;

    protected BaseComponent(Page page, FrameworkConfig config, LocatorDefinition root) {
        actions = new ElementActions(page, config).within(root);
    }
}
