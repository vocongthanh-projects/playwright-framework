package vn.ots.qc.web.interaction;

import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;
import vn.ots.qc.web.config.FrameworkConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/** Page-level events that must be registered before the triggering action. */
public final class BrowserEvents {
    private final Page page;
    private final FrameworkConfig config;

    public BrowserEvents(Page page, FrameworkConfig config) {
        this.page = Objects.requireNonNull(page);
        this.config = Objects.requireNonNull(config);
    }

    public Path downloadTo(Runnable trigger, Path destination) {
        Objects.requireNonNull(trigger, "trigger");
        Path target = Objects.requireNonNull(destination, "destination").toAbsolutePath().normalize();
        if (Files.exists(target) && Files.isDirectory(target)) {
            throw new IllegalArgumentException("Download destination must be a file: " + target);
        }
        try {
            if (target.getParent() != null) Files.createDirectories(target.getParent());
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Cannot create download directory for " + target, exception);
        }
        Download download = page.waitForDownload(trigger);
        download.saveAs(target);
        return target;
    }

    public Page popup(Runnable trigger) {
        Page popup = page.waitForPopup(Objects.requireNonNull(trigger, "trigger"));
        popup.setDefaultTimeout(config.defaultTimeout().toMillis());
        popup.setDefaultNavigationTimeout(config.navigationTimeout().toMillis());
        return popup;
    }

    public DialogResult acceptNextDialog(Runnable trigger) {
        return handleDialog(trigger, Dialog::accept);
    }

    public DialogResult acceptNextPrompt(String value, Runnable trigger) {
        Objects.requireNonNull(value, "prompt value");
        return handleDialog(trigger, dialog -> dialog.accept(value));
    }

    public DialogResult dismissNextDialog(Runnable trigger) {
        return handleDialog(trigger, Dialog::dismiss);
    }

    private DialogResult handleDialog(Runnable trigger, Consumer<Dialog> response) {
        Objects.requireNonNull(trigger, "trigger");
        AtomicReference<DialogResult> result = new AtomicReference<>();
        Consumer<Dialog> handler = dialog -> {
            result.set(new DialogResult(dialog.type(), dialog.message(), dialog.defaultValue()));
            response.accept(dialog);
        };
        page.onDialog(handler);
        try { trigger.run(); }
        finally { page.offDialog(handler); }
        if (result.get() == null) throw new IllegalStateException("Trigger completed without opening a dialog");
        return result.get();
    }

    public record DialogResult(String type, String message, String defaultValue) {}
}
