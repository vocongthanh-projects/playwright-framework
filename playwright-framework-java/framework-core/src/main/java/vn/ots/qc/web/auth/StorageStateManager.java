package vn.ots.qc.web.auth;

import com.microsoft.playwright.BrowserContext;
import vn.ots.qc.web.config.FrameworkConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/** Loads and persists Playwright authentication state with configurable expiry. */
public final class StorageStateManager {
    private final FrameworkConfig config;

    public StorageStateManager(FrameworkConfig config) {
        this.config = config;
    }

    public Optional<Path> freshState() {
        if (!config.getBoolean("storage-state.enabled")) {
            return Optional.empty();
        }
        Path path = config.requiredPath("storage-state.path");
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        try {
            Instant expiresAt = Files.getLastModifiedTime(path).toInstant()
                    .plus(Duration.ofMinutes(config.getLong("storage-state.ttl-minutes", 60)));
            return expiresAt.isAfter(Instant.now()) ? Optional.of(path) : Optional.empty();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot inspect storage state: " + path, exception);
        }
    }

    public void persist(BrowserContext context) {
        if (!config.getBoolean("storage-state.enabled") || !config.getBoolean("storage-state.persist")) {
            return;
        }
        Path path = config.requiredPath("storage-state.path");
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            context.storageState(new BrowserContext.StorageStateOptions().setIndexedDB(true).setPath(path));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot create storage state directory for: " + path, exception);
        }
    }
}
