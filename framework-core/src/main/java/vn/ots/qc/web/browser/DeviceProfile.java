package vn.ots.qc.web.browser;

import vn.ots.qc.web.config.FrameworkConfig;
import vn.ots.qc.web.config.ConfigurationException;

/** A custom device profile declared under device.&lt;normalized-name&gt;.* settings. */
public record DeviceProfile(
        int viewportWidth,
        int viewportHeight,
        double deviceScaleFactor,
        boolean mobile,
        boolean touch,
        String userAgent) {

    public static DeviceProfile from(FrameworkConfig config, BrowserTarget target) {
        String prefix = "device." + target.deviceKey() + ".";
        return new DeviceProfile(
                positive(config.getInt(prefix + "viewport.width"), prefix + "viewport.width"),
                positive(config.getInt(prefix + "viewport.height"), prefix + "viewport.height"),
                positive(config.getDouble(prefix + "scale-factor"), prefix + "scale-factor"),
                config.getBoolean(prefix + "mobile"),
                config.getBoolean(prefix + "touch"),
                config.required(prefix + "user-agent"));
    }

    private static int positive(int value, String key) {
        if (value <= 0) {
            throw new ConfigurationException("Setting " + key + " must be greater than zero");
        }
        return value;
    }

    private static double positive(double value, String key) {
        if (value <= 0) {
            throw new ConfigurationException("Setting " + key + " must be greater than zero");
        }
        return value;
    }
}
