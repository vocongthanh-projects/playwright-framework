package vn.ots.qc.web.auth;

import org.testng.annotations.Test;
import vn.ots.qc.web.config.FrameworkConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public final class StorageStateManagerTest {
    @Test
    public void acceptsFreshStateAndRejectsExpiredState() throws Exception {
        Path state = Files.createTempFile("playwright-storage-state-", ".json");
        try {
            FrameworkConfig config = FrameworkConfig.load().withOverrides(Map.of(
                    "storage-state.enabled", "true",
                    "storage-state.path", state.toString(),
                    "storage-state.ttl-minutes", "60"));
            StorageStateManager manager = new StorageStateManager(config);

            assertEquals(manager.freshState().orElseThrow(), state.toAbsolutePath().normalize());

            Files.setLastModifiedTime(state, FileTime.from(Instant.now().minus(2, ChronoUnit.HOURS)));
            assertTrue(manager.freshState().isEmpty());
        } finally {
            Files.deleteIfExists(state);
        }
    }
}
