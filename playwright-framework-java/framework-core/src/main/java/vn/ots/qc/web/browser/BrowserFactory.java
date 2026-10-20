package vn.ots.qc.web.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import vn.ots.qc.web.config.ConfigurationException;
import vn.ots.qc.web.config.FrameworkConfig;

import java.nio.file.Files;
import java.nio.file.Path;

public final class BrowserFactory {
    public Browser create(Playwright playwright, BrowserTarget target, FrameworkConfig config) {
        BrowserType browserType = switch (target.browserName()) {
            case "chromium" -> playwright.chromium();
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> throw new IllegalArgumentException("Unsupported browser: " + target.browserName());
        };
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(config.getBoolean("browser.headless"))
                .setSlowMo(config.getDouble("browser.slow-motion", 0));
        config.optionalPath("browser.executable-path").ifPresent(path -> options.setExecutablePath(executable(path)));
        return browserType.launch(options);
    }

    private static Path executable(Path path) {
        if (!Files.isExecutable(path)) {
            throw new ConfigurationException("Browser executable is not executable: " + path);
        }
        return path;
    }
}
