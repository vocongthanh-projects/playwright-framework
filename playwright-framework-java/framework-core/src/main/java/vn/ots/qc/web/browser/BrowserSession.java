package vn.ots.qc.web.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import vn.ots.qc.web.config.FrameworkConfig;

import java.util.Objects;

/** Scenario-scoped owner of all Playwright resources. */
public final class BrowserSession implements AutoCloseable {
    private final FrameworkConfig config;
    private final BrowserTarget target;
    private final BrowserFactory browserFactory;
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    public BrowserSession(FrameworkConfig config, BrowserTarget target) {
        this(config, target, new BrowserFactory());
    }

    BrowserSession(FrameworkConfig config, BrowserTarget target, BrowserFactory browserFactory) {
        this.config = Objects.requireNonNull(config);
        this.target = Objects.requireNonNull(target);
        this.browserFactory = Objects.requireNonNull(browserFactory);
    }

    public synchronized Page start(Browser.NewContextOptions options) {
        if (page != null) {
            return page;
        }
        playwright = Playwright.create();
        browser = browserFactory.create(playwright, target, config);
        context = browser.newContext(options);
        page = context.newPage();
        page.setDefaultTimeout(config.defaultTimeout().toMillis());
        page.setDefaultNavigationTimeout(config.navigationTimeout().toMillis());
        return page;
    }

    public Page page() {
        return Objects.requireNonNull(page, "Browser session has not been started");
    }

    public BrowserContext context() {
        return Objects.requireNonNull(context, "Browser session has not been started");
    }

    public FrameworkConfig config() {
        return config;
    }

    public BrowserTarget target() {
        return target;
    }

    public boolean isStarted() {
        return page != null;
    }

    @Override
    public synchronized void close() {
        try {
            if (context != null) {
                context.close();
            }
        } finally {
            try {
                if (browser != null) {
                    browser.close();
                }
            } finally {
                if (playwright != null) {
                    playwright.close();
                }
                page = null;
                context = null;
                browser = null;
                playwright = null;
            }
        }
    }
}
