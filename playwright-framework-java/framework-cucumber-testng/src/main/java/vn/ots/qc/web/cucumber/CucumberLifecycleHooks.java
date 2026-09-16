package vn.ots.qc.web.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import vn.ots.qc.web.artifact.EvidenceBundle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Generic browser lifecycle and failure evidence hooks; include this package in Cucumber glue. */
public final class CucumberLifecycleHooks {
    private final CucumberWorld world;

    public CucumberLifecycleHooks(CucumberWorld world) {
        this.world = world;
    }

    @Before(order = Integer.MIN_VALUE)
    public void startBrowser() {
        world.web().start();
    }

    @After(order = Integer.MIN_VALUE)
    public void finishBrowser(Scenario scenario) {
        EvidenceBundle evidence = world.web().finish(scenario.getName(), scenario.isFailed());
        if (!scenario.isFailed()) {
            return;
        }
        attachIfEnabled(scenario, "evidence.attach.screenshot", evidence.screenshot(),
                "image/png", "failure-screenshot");
        attachIfEnabled(scenario, "evidence.attach.html", evidence.html(),
                "text/html", "page-source");
        attachIfEnabled(scenario, "evidence.attach.trace", evidence.trace(),
                "application/zip", "playwright-trace");
    }

    private void attachIfEnabled(Scenario scenario, String setting, Path path, String mediaType, String name) {
        if (world.config().getBoolean(setting)) {
            attach(scenario, path, mediaType, name);
        }
    }

    private static void attach(Scenario scenario, Path path, String mediaType, String name) {
        if (path == null) {
            return;
        }
        try {
            scenario.attach(Files.readAllBytes(path), mediaType, name);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot attach evidence: " + path, exception);
        }
    }
}
