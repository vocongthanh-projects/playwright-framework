package vn.ots.qc.web.cucumber;

import com.microsoft.playwright.Page;
import vn.ots.qc.web.config.FrameworkConfig;
import vn.ots.qc.web.context.WebTestContext;
import vn.ots.qc.web.interaction.ElementActions;
import vn.ots.qc.web.interaction.BrowserEvents;

import java.util.Optional;

/** Scenario-scoped dependency injected into application step definitions. */
public final class CucumberWorld {
    private final WebTestContext web = new WebTestContext();

    public WebTestContext web() {
        return web;
    }

    public Page page() {
        return web.page();
    }

    public ElementActions actions() {
        return web.actions();
    }

    public BrowserEvents events() { return web.events(); }

    public FrameworkConfig config() {
        return web.config();
    }

    public void put(String key, Object value) {
        web.put(key, value);
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        return web.get(key, type);
    }
}
