package vn.ots.qc.web.artifact;

import vn.ots.qc.web.config.FrameworkConfig;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** Produces safe, collision-resistant artifact paths for a test execution. */
public final class ArtifactPaths {
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").withZone(ZoneOffset.UTC);

    private final Path root;
    private final Clock clock;

    public ArtifactPaths(FrameworkConfig config) {
        this(config.requiredPath("evidence.directory"), Clock.systemUTC());
    }

    ArtifactPaths(Path root, Clock clock) {
        this.root = root;
        this.clock = clock;
    }

    public Path forTest(String testName, String extension) {
        String safeName = testName == null ? "unnamed-test" : testName.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9._-]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (safeName.isBlank()) {
            safeName = "unnamed-test";
        }
        String safeExtension = extension.replaceAll("[^a-zA-Z0-9]", "");
        return root.resolve(safeName + "-" + TIMESTAMP.format(Instant.now(clock)) + "." + safeExtension);
    }
}
