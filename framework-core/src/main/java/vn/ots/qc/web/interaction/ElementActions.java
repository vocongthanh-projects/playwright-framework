package vn.ots.qc.web.interaction;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.ots.qc.web.config.FrameworkConfig;
import vn.ots.qc.web.locator.LocatorDefinition;
import vn.ots.qc.web.locator.LocatorResolutionException;
import vn.ots.qc.web.locator.LocatorResolver;
import vn.ots.qc.web.locator.ResolvedLocator;

import java.util.Objects;
import java.nio.file.Path;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Web-first actions over logical locators; values are never written to logs. */
public final class ElementActions {
    private static final Logger LOG = LoggerFactory.getLogger(ElementActions.class);

    private final LocatorResolver resolver;
    private final FrameworkConfig config;
    private final int timeout;

    public ElementActions(Page page, FrameworkConfig config) {
        this(new LocatorResolver(page, config), config);
    }

    private ElementActions(LocatorResolver resolver, FrameworkConfig config) {
        this.resolver = resolver;
        this.config = config;
        timeout = Math.toIntExact(config.defaultTimeout().toMillis());
    }

    public ElementActions within(LocatorDefinition container) {
        return new ElementActions(resolver.within(container), config);
    }

    public ResolvedLocator resolve(LocatorDefinition definition) {
        return resolver.resolveVisible(definition);
    }

    public void click(LocatorDefinition definition) {
        LOG.debug("Click logical locator '{}'", definition.logicalName());
        resolve(definition).locator().click();
    }

    public void fill(LocatorDefinition definition, String value) {
        fillInternal(definition, value, false);
    }

    public void fillSensitive(LocatorDefinition definition, String value) {
        fillInternal(definition, value, true);
    }

    public void clear(LocatorDefinition definition) {
        LOG.debug("Clear logical locator '{}'", definition.logicalName());
        resolve(definition).locator().clear();
    }

    public void check(LocatorDefinition definition) {
        LOG.debug("Check logical locator '{}'", definition.logicalName());
        resolve(definition).locator().check();
    }

    public void uncheck(LocatorDefinition definition) {
        LOG.debug("Uncheck logical locator '{}'", definition.logicalName());
        resolve(definition).locator().uncheck();
    }

    public void select(LocatorDefinition definition, String value) {
        LOG.debug("Select option in logical locator '{}'", definition.logicalName());
        resolve(definition).locator().selectOption(value);
    }

    public void hover(LocatorDefinition definition) {
        LOG.debug("Hover logical locator '{}'", definition.logicalName());
        resolve(definition).locator().hover();
    }

    public void press(LocatorDefinition definition, String key) {
        LOG.debug("Press key on logical locator '{}'", definition.logicalName());
        resolve(definition).locator().press(key);
    }

    public void upload(LocatorDefinition definition, Path... files) {
        if (files == null || files.length == 0) throw new IllegalArgumentException("At least one file is required");
        Path[] normalized = java.util.Arrays.stream(files)
                .map(path -> Objects.requireNonNull(path, "file").toAbsolutePath().normalize())
                .peek(path -> {
                    if (!java.nio.file.Files.isRegularFile(path)) {
                        throw new IllegalArgumentException("Upload file does not exist: " + path);
                    }
                }).toArray(Path[]::new);
        LOG.debug("Upload {} file(s) through logical locator '{}'", normalized.length, definition.logicalName());
        resolve(definition).locator().setInputFiles(normalized);
    }

    public void scrollIntoView(LocatorDefinition definition) {
        resolve(definition).locator().scrollIntoViewIfNeeded();
    }

    public void dragTo(LocatorDefinition source, LocatorDefinition target) {
        LOG.debug("Drag logical locator '{}' to '{}'", source.logicalName(), target.logicalName());
        resolve(source).locator().dragTo(resolve(target).locator());
    }

    public String textOf(LocatorDefinition definition) {
        return resolve(definition).locator().innerText();
    }

    public String attributeOf(LocatorDefinition definition, String attribute) {
        return resolve(definition).locator().getAttribute(attribute);
    }

    public String valueOf(LocatorDefinition definition) { return resolve(definition).locator().inputValue(); }
    public boolean isEnabled(LocatorDefinition definition) { return resolve(definition).locator().isEnabled(); }
    public boolean isChecked(LocatorDefinition definition) { return resolve(definition).locator().isChecked(); }

    public boolean isVisible(LocatorDefinition definition) {
        try {
            resolve(definition);
            return true;
        } catch (LocatorResolutionException exception) {
            return false;
        }
    }

    public void shouldBeVisible(LocatorDefinition definition) {
        assertThat(resolve(definition).locator())
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(timeout));
    }

    public void shouldBeEnabled(LocatorDefinition definition) {
        assertThat(resolve(definition).locator())
                .isEnabled(new LocatorAssertions.IsEnabledOptions().setTimeout(timeout));
    }

    public void shouldBeDisabled(LocatorDefinition definition) {
        assertThat(resolve(definition).locator())
                .isDisabled(new LocatorAssertions.IsDisabledOptions().setTimeout(timeout));
    }

    public void shouldHaveText(LocatorDefinition definition, String expected) {
        assertThat(resolve(definition).locator())
                .hasText(expected, new LocatorAssertions.HasTextOptions().setTimeout(timeout));
    }

    public void shouldNotBeVisible(LocatorDefinition definition) {
        for (Locator locator : resolver.candidates(definition)) {
            assertThat(locator.first()).isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(timeout));
        }
    }

    public Locator waitForVisible(Locator locator, int timeoutMillis) {
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMillis));
        return locator;
    }

    public void click(Locator locator) {
        waitForVisible(locator, timeout).click();
    }

    private void fillInternal(LocatorDefinition definition, String value, boolean sensitive) {
        Objects.requireNonNull(value, "value");
        LOG.debug("Fill logical locator '{}'{}", definition.logicalName(), sensitive ? " with sensitive value" : "");
        resolve(definition).locator().fill(value);
    }

}
