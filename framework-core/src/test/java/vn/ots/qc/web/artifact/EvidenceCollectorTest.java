package vn.ots.qc.web.artifact;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.testng.annotations.Test;
import vn.ots.qc.web.config.FrameworkConfig;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public final class EvidenceCollectorTest {
    @Test
    public void pageSourceStillPersistsWhenScreenshotFails() throws Exception {
        Path directory = Files.createTempDirectory("playwright-evidence-");
        Locator mask = (Locator) Proxy.newProxyInstance(Locator.class.getClassLoader(), new Class<?>[]{Locator.class},
                (proxy, method, arguments) -> null);
        Page page = (Page) Proxy.newProxyInstance(Page.class.getClassLoader(), new Class<?>[]{Page.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "locator" -> mask;
                    case "screenshot" -> throw new RuntimeException("capture unavailable");
                    case "evaluate" -> "<html><input type='password' value='<redacted>'></html>";
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        try {
            FrameworkConfig config = FrameworkConfig.load().withOverrides(Map.of(
                    "evidence.directory", directory.toString()));
            EvidenceCollector.CapturedEvidence evidence = new EvidenceCollector(config, new ArtifactPaths(config))
                    .capture(page, "secure login");

            assertNull(evidence.screenshot());
            assertTrue(Files.isRegularFile(evidence.html()));
            String html = Files.readString(evidence.html());
            assertTrue(html.contains("&lt;redacted&gt;") || html.contains("<redacted>"));
            assertFalse(html.contains("secret-value"));
        } finally {
            try (var files = Files.list(directory)) {
                files.forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (java.io.IOException exception) {
                        throw new RuntimeException(exception);
                    }
                });
            }
            Files.deleteIfExists(directory);
        }
    }
}
