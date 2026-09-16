package vn.ots.qc.web.locator;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import org.testng.annotations.Test;
import vn.ots.qc.web.config.FrameworkConfig;

import java.lang.reflect.Proxy;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static vn.ots.qc.web.locator.WebLocators.label;
import static vn.ots.qc.web.locator.WebLocators.testId;

public final class LocatorResolverTest {
    @Test
    public void fallsBackInDeclaredOrder() {
        Locator missing = locatorProxy(true, Map.of());
        Locator visible = locatorProxy(false, Map.of());
        Page page = pageProxy(Map.of("getByTestId", missing, "getByLabel", visible));
        FrameworkConfig config = FrameworkConfig.load().withOverrides(Map.of("locator.resolve.timeout", "1"));

        ResolvedLocator resolved = new LocatorResolver(page, config).resolveVisible(
                WebLocator.of("login.username", testId("username"), label("Username")));

        assertSame(resolved.locator(), visible);
        assertEquals(resolved.candidate().strategy(), LocatorStrategy.LABEL);
    }

    @Test
    public void resolvesChildrenInsideComponentScope() {
        Locator child = locatorProxy(false, Map.of());
        Locator root = locatorProxy(false, Map.of("getByLabel", child));
        Page page = pageProxy(Map.of("getByTestId", root));
        FrameworkConfig config = FrameworkConfig.load().withOverrides(Map.of("locator.resolve.timeout", "1"));

        LocatorResolver scoped = new LocatorResolver(page, config)
                .within(WebLocator.of("dialog", testId("dialog")));

        assertSame(scoped.resolveVisible(WebLocator.of("email", label("Email"))).locator(), child);
    }

    private static Page pageProxy(Map<String, Locator> results) {
        return (Page) Proxy.newProxyInstance(Page.class.getClassLoader(), new Class<?>[]{Page.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("toString")) {
                        return "page-proxy";
                    }
                    Locator result = results.get(method.getName());
                    if (result != null) {
                        return result;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static Locator locatorProxy(boolean timesOut, Map<String, Locator> children) {
        final Locator[] self = new Locator[1];
        self[0] = (Locator) Proxy.newProxyInstance(Locator.class.getClassLoader(), new Class<?>[]{Locator.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "first" -> self[0];
                    case "waitFor" -> {
                        if (timesOut) {
                            throw new TimeoutError("not visible");
                        }
                        yield null;
                    }
                    case "toString" -> "locator-proxy";
                    default -> {
                        Locator child = children.get(method.getName());
                        if (child != null) {
                            yield child;
                        }
                        throw new UnsupportedOperationException(method.getName());
                    }
                });
        return self[0];
    }
}
