package vn.ots.qc.web.artifact;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Tracing;
import vn.ots.qc.web.config.FrameworkConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** Owns Playwright tracing for one browser context. */
public final class TraceRecorder {
    private final FrameworkConfig config;
    private final ArtifactPaths paths;
    private boolean started;

    public TraceRecorder(FrameworkConfig config, ArtifactPaths paths) {
        this.config = config;
        this.paths = paths;
    }

    public void start(BrowserContext context) {
        if (!config.getBoolean("trace.enabled") || started) {
            return;
        }
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(config.getBoolean("trace.screenshots"))
                .setSnapshots(config.getBoolean("trace.snapshots"))
                .setSources(config.getBoolean("trace.sources")));
        started = true;
    }

    public Optional<Path> stop(BrowserContext context, String testName, boolean keep) {
        if (!started) {
            return Optional.empty();
        }
        started = false;
        if (!keep) {
            context.tracing().stop();
            return Optional.empty();
        }
        Path trace = paths.forTest(testName, "zip");
        try {
            Files.createDirectories(trace.getParent());
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot create trace directory: " + trace.getParent(), exception);
        }
        context.tracing().stop(new Tracing.StopOptions().setPath(trace));
        return Optional.of(trace);
    }
}
