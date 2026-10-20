package vn.ots.qc.web.browser;

/** Thread-bound browser target used by parallel test adapters. */
public final class ExecutionTarget {
    private static final ThreadLocal<String> CURRENT = ThreadLocal.withInitial(() -> "");

    private ExecutionTarget() {
    }

    public static String current() {
        return CURRENT.get();
    }

    public static void set(BrowserTarget target) {
        CURRENT.set(target.browserName() + (target.deviceName().isBlank() ? "" : "@" + target.deviceName()));
    }

    public static void clear() {
        CURRENT.remove();
    }
}
