package vn.ots.qc.web.browser;

import vn.ots.qc.web.config.ConfigurationException;
import vn.ots.qc.web.config.FrameworkConfig;

import java.util.List;

/** Resolves browser/device targets from configuration without application coupling. */
public final class BrowserTargetMatrix {
    private BrowserTargetMatrix() {
    }

    public static List<BrowserTarget> from(FrameworkConfig config) {
        List<String> configuredTargets = config.getList("browser.targets");
        if (!configuredTargets.isEmpty()) {
            return configuredTargets.stream().map(BrowserTarget::parse).distinct().toList();
        }

        String browser = config.required("browser.type");
        String device = config.get("browser.device");
        return List.of(new BrowserTarget(browser, device));
    }

    public static BrowserTarget current(FrameworkConfig config) {
        String override = ExecutionTarget.current();
        if (!override.isBlank()) {
            return BrowserTarget.parse(override);
        }
        List<BrowserTarget> targets = from(config);
        if (targets.isEmpty()) {
            throw new ConfigurationException("At least one browser target must be configured");
        }
        return targets.get(0);
    }
}
