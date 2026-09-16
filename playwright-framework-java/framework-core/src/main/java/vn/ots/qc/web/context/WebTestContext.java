package vn.ots.qc.web.context;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import vn.ots.qc.web.artifact.ArtifactPaths;
import vn.ots.qc.web.artifact.EvidenceBundle;
import vn.ots.qc.web.artifact.EvidenceCollector;
import vn.ots.qc.web.artifact.TraceRecorder;
import vn.ots.qc.web.auth.StorageStateManager;
import vn.ots.qc.web.browser.BrowserContextOptionsFactory;
import vn.ots.qc.web.browser.BrowserSession;
import vn.ots.qc.web.browser.BrowserTarget;
import vn.ots.qc.web.browser.BrowserTargetMatrix;
import vn.ots.qc.web.config.FrameworkConfig;
import vn.ots.qc.web.config.FrameworkConfigValidator;
import vn.ots.qc.web.interaction.ElementActions;
import vn.ots.qc.web.interaction.BrowserEvents;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Test-scoped composition root shared by runner adapters and application code. */
public final class WebTestContext implements AutoCloseable {
    private final FrameworkConfig config;
    private final BrowserTarget target;
    private final BrowserSession session;
    private final StorageStateManager storageState;
    private final EvidenceCollector evidence;
    private final TraceRecorder trace;
    private final Map<String, Object> state = new ConcurrentHashMap<>();
    private ElementActions actions;
    private BrowserEvents events;
    private boolean finished;

    public WebTestContext() {
        this(FrameworkConfig.load());
    }

    public WebTestContext(FrameworkConfig config) {
        this(config, BrowserTargetMatrix.current(config));
    }

    public WebTestContext(FrameworkConfig config, BrowserTarget target) {
        this.config = config;
        this.target = target;
        FrameworkConfigValidator.validate(config, target);
        session = new BrowserSession(config, target);
        storageState = new StorageStateManager(config);
        ArtifactPaths paths = new ArtifactPaths(config);
        evidence = new EvidenceCollector(config, paths);
        trace = new TraceRecorder(config, paths);
    }

    public Page start() {
        Page page = session.start(new BrowserContextOptionsFactory().create(config, target));
        actions = new ElementActions(page, config);
        events = new BrowserEvents(page, config);
        trace.start(session.context());
        if (config.getBoolean("navigation.enabled")) {
            page.navigate(config.required("navigation.base-url"));
        }
        return page;
    }

    public EvidenceBundle finish(String testName, boolean failed) {
        if (finished) {
            throw new IllegalStateException("Test context has already been finished");
        }
        finished = true;
        if (!session.isStarted()) {
            close();
            return new EvidenceBundle(null, null, null);
        }
        Path screenshot = null;
        Path html = null;
        try {
            if (failed) {
                EvidenceCollector.CapturedEvidence captured = evidence.capture(page(), testName);
                screenshot = captured.screenshot();
                html = captured.html();
            }
            Optional<Path> tracePath = trace.stop(browserContext(), testName, failed);
            storageState.persist(browserContext());
            return new EvidenceBundle(screenshot, html, tracePath.orElse(null));
        } finally {
            close();
        }
    }

    public Page page() {
        return session.page();
    }

    public BrowserContext browserContext() {
        return session.context();
    }

    public ElementActions actions() {
        if (actions == null) {
            throw new IllegalStateException("Test context has not been started");
        }
        return actions;
    }

    public BrowserEvents events() {
        if (events == null) throw new IllegalStateException("Test context has not been started");
        return events;
    }

    public FrameworkConfig config() {
        return config;
    }

    public BrowserTarget target() {
        return target;
    }

    public void put(String key, Object value) {
        state.put(key, value);
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = state.get(key);
        return value == null ? Optional.empty() : Optional.of(type.cast(value));
    }

    @Override
    public void close() {
        session.close();
        actions = null;
        events = null;
        state.clear();
    }
}
