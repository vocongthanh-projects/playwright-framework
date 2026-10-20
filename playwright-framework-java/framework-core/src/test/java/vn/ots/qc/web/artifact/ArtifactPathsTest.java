package vn.ots.qc.web.artifact;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.testng.Assert.assertEquals;

public final class ArtifactPathsTest {
    @Test
    public void sanitizesExternalScenarioNames() {
        ArtifactPaths paths = new ArtifactPaths(
                Path.of("target/evidence"),
                Clock.fixed(Instant.parse("2026-08-06T03:00:00Z"), ZoneOffset.UTC));

        assertEquals(paths.forTest("User / can: login?", "png"),
                Path.of("target/evidence/user-can-login-20260806-030000-000.png"));
    }
}
