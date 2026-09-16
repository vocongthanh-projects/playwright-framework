package vn.ots.qc.web.testng;

import com.microsoft.playwright.Page;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import vn.ots.qc.web.artifact.EvidenceBundle;
import vn.ots.qc.web.browser.BrowserTarget;
import vn.ots.qc.web.browser.BrowserTargetMatrix;
import vn.ots.qc.web.config.FrameworkConfig;
import vn.ots.qc.web.context.WebTestContext;
import vn.ots.qc.web.interaction.ElementActions;
import vn.ots.qc.web.interaction.BrowserEvents;

import java.lang.reflect.Method;
import java.util.Arrays;

/** Standalone TestNG lifecycle for projects that do not use Cucumber. */
public abstract class BaseWebTest {
    private final ThreadLocal<WebTestContext> current = new ThreadLocal<>();

    @DataProvider(name = "browserTargets", parallel = true)
    public final Object[][] browserTargets() {
        return BrowserTargetMatrix.from(FrameworkConfig.load()).stream()
                .map(target -> new Object[]{target})
                .toArray(Object[][]::new);
    }

    @BeforeMethod(alwaysRun = true)
    public final void startWebTest(Method method, Object[] parameters) {
        FrameworkConfig config = FrameworkConfig.load();
        BrowserTarget target = Arrays.stream(parameters)
                .filter(BrowserTarget.class::isInstance)
                .map(BrowserTarget.class::cast)
                .findFirst()
                .orElseGet(() -> BrowserTargetMatrix.current(config));
        WebTestContext context = new WebTestContext(config, target);
        current.set(context);
        try {
            context.start();
        } catch (RuntimeException exception) {
            context.close();
            current.remove();
            throw exception;
        }
    }

    @AfterMethod(alwaysRun = true)
    public final void finishWebTest(ITestResult result) {
        WebTestContext context = current.get();
        if (context == null) {
            return;
        }
        try {
            boolean failed = result.getStatus() != ITestResult.SUCCESS;
            EvidenceBundle evidence = context.finish(result.getMethod().getQualifiedName(), failed);
            TestArtifactReporter.publish(result, evidence);
        } finally {
            current.remove();
        }
    }

    protected final WebTestContext web() {
        WebTestContext context = current.get();
        if (context == null) {
            throw new IllegalStateException("No active web test context");
        }
        return context;
    }

    protected final Page page() {
        return web().page();
    }

    protected final ElementActions actions() {
        return web().actions();
    }

    protected final BrowserEvents events() { return web().events(); }

    protected final FrameworkConfig config() {
        return web().config();
    }
}
