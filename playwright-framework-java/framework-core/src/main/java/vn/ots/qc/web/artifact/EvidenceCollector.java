package vn.ots.qc.web.artifact;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.ots.qc.web.config.FrameworkConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Captures best-effort browser evidence without exposing standard sensitive fields. */
public final class EvidenceCollector {
    private static final Logger LOG = LoggerFactory.getLogger(EvidenceCollector.class);

    private final FrameworkConfig config;
    private final ArtifactPaths paths;

    public EvidenceCollector(FrameworkConfig config, ArtifactPaths paths) {
        this.config = config;
        this.paths = paths;
    }

    public CapturedEvidence capture(Page page, String testName) {
        Path screenshot = captureScreenshot(page, testName);
        Path html = captureRedactedHtml(page, testName);
        return new CapturedEvidence(screenshot, html);
    }

    private Path captureScreenshot(Page page, String testName) {
        Path screenshot = paths.forTest(testName, "png");
        try {
            Files.createDirectories(screenshot.getParent());
            Page.ScreenshotOptions options = new Page.ScreenshotOptions()
                    .setPath(screenshot)
                    .setFullPage(config.getBoolean("evidence.screenshot.full-page"));
            String maskSelector = config.get("evidence.mask-selector");
            if (!maskSelector.isBlank()) {
                Locator sensitiveElements = page.locator(maskSelector);
                options.setMask(List.of(sensitiveElements));
            }
            page.screenshot(options);
            return screenshot;
        } catch (IOException | RuntimeException exception) {
            LOG.warn("Could not capture failure screenshot for '{}' ({})", testName,
                    exception.getClass().getSimpleName());
            LOG.debug("Screenshot capture failure", exception);
            return null;
        }
    }

    private Path captureRedactedHtml(Page page, String testName) {
        Path html = paths.forTest(testName, "html");
        try {
            Files.createDirectories(html.getParent());
            String selector = config.get("evidence.mask-selector");
            String content = (String) page.evaluate("""
                    selector => {
                      const clone = document.documentElement.cloneNode(true);
                      if (selector) {
                        clone.querySelectorAll(selector).forEach(element => {
                          if ('value' in element) element.setAttribute('value', '<redacted>');
                          if (!['INPUT', 'TEXTAREA', 'SELECT'].includes(element.tagName)) {
                            element.textContent = '<redacted>';
                          }
                        });
                      }
                      return clone.outerHTML;
                    }
                    """, selector);
            Files.writeString(html, content, StandardCharsets.UTF_8);
            return html;
        } catch (IOException | RuntimeException exception) {
            LOG.warn("Could not capture redacted page source for '{}' ({})", testName,
                    exception.getClass().getSimpleName());
            LOG.debug("Page source capture failure", exception);
            return null;
        }
    }

    public record CapturedEvidence(Path screenshot, Path html) {
    }
}
