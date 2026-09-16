package vn.ots.qc.web.browser;

import com.microsoft.playwright.Browser;
import vn.ots.qc.web.auth.StorageStateManager;
import vn.ots.qc.web.config.FrameworkConfig;

import java.nio.file.Files;

/** Builds isolated browser context options entirely from framework configuration. */
public final class BrowserContextOptionsFactory {
    public Browser.NewContextOptions create(FrameworkConfig config, BrowserTarget target) {
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(config.getBoolean("context.ignore-https-errors"));

        if (target.deviceName().isBlank()) {
            options.setViewportSize(
                    config.getInt("context.viewport.width"),
                    config.getInt("context.viewport.height"));
        } else {
            DeviceProfile profile = DeviceProfile.from(config, target);
            options.setViewportSize(profile.viewportWidth(), profile.viewportHeight())
                    .setDeviceScaleFactor(profile.deviceScaleFactor())
                    .setIsMobile(profile.mobile())
                    .setHasTouch(profile.touch())
                    .setUserAgent(profile.userAgent());
        }

        setIfPresent(config.get("context.locale"), options::setLocale);
        setIfPresent(config.get("context.timezone"), options::setTimezoneId);
        if (!config.getList("context.permissions").isEmpty()) {
            options.setPermissions(config.getList("context.permissions"));
        }
        if (config.getBoolean("context.geolocation.enabled")) {
            options.setGeolocation(
                    config.getDouble("context.geolocation.latitude"),
                    config.getDouble("context.geolocation.longitude"));
        }
        if (config.getBoolean("video.enabled")) {
            options.setRecordVideoDir(config.requiredPath("video.directory"))
                    .setRecordVideoSize(config.getInt("video.width"), config.getInt("video.height"));
        }

        StorageStateManager storageState = new StorageStateManager(config);
        storageState.freshState().filter(Files::isRegularFile).ifPresent(options::setStorageStatePath);
        return options;
    }

    private static void setIfPresent(String value, java.util.function.Consumer<String> setter) {
        if (!value.isBlank()) {
            setter.accept(value);
        }
    }
}
