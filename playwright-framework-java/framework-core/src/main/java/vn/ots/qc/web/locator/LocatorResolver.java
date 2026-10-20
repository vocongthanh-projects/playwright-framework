package vn.ots.qc.web.locator;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.ots.qc.web.config.FrameworkConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Resolves ordered semantic locator candidates against a page or component scope. */
public final class LocatorResolver {
    private static final Logger LOG = LoggerFactory.getLogger(LocatorResolver.class);

    private final Page page;
    private final Locator scope;
    private final FrameworkConfig config;
    private final double probeTimeout;

    public LocatorResolver(Page page, FrameworkConfig config) {
        this(page, null, config);
    }

    private LocatorResolver(Page page, Locator scope, FrameworkConfig config) {
        this.page = Objects.requireNonNull(page, "page");
        this.scope = scope;
        this.config = Objects.requireNonNull(config, "config");
        probeTimeout = config.getInt("locator.resolve.timeout");
    }

    public ResolvedLocator resolveVisible(LocatorDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        List<String> attempted = new ArrayList<>();
        for (LocatorCandidate candidate : definition.candidates()) {
            Locator locator = locator(candidate).first();
            attempted.add(candidate.diagnostic());
            try {
                locator.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(probeTimeout));
                LOG.debug("Resolved logical locator '{}' with {}", definition.logicalName(), candidate.diagnostic());
                return new ResolvedLocator(locator, candidate);
            } catch (TimeoutError ignored) {
                LOG.debug("Locator candidate {} did not resolve for '{}'", candidate.diagnostic(),
                        definition.logicalName());
            }
        }
        throw new LocatorResolutionException("Cannot resolve logical locator '" + definition.logicalName()
                + "'; attempted strategies: " + String.join(" -> ", attempted));
    }

    public List<Locator> candidates(LocatorDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        return definition.candidates().stream().map(this::locator).toList();
    }

    public LocatorResolver within(LocatorDefinition container) {
        return new LocatorResolver(page, resolveVisible(container).locator(), config);
    }

    Locator locator(LocatorCandidate candidate) {
        return scope == null ? fromPage(candidate) : fromScope(candidate);
    }

    private Locator fromPage(LocatorCandidate candidate) {
        return switch (candidate.strategy()) {
            case ROLE -> page.getByRole(candidate.role(),
                    new Page.GetByRoleOptions().setName(candidate.value()).setExact(candidate.exact()));
            case LABEL -> page.getByLabel(candidate.value(),
                    new Page.GetByLabelOptions().setExact(candidate.exact()));
            case TEST_ID -> page.getByTestId(candidate.value());
            case PLACEHOLDER -> page.getByPlaceholder(candidate.value(),
                    new Page.GetByPlaceholderOptions().setExact(candidate.exact()));
            case ALT_TEXT -> page.getByAltText(candidate.value(),
                    new Page.GetByAltTextOptions().setExact(candidate.exact()));
            case TITLE -> page.getByTitle(candidate.value(),
                    new Page.GetByTitleOptions().setExact(candidate.exact()));
            case TEXT -> page.getByText(candidate.value(),
                    new Page.GetByTextOptions().setExact(candidate.exact()));
            case CSS -> page.locator(candidate.value());
            case XPATH -> page.locator("xpath=" + candidate.value());
        };
    }

    private Locator fromScope(LocatorCandidate candidate) {
        return switch (candidate.strategy()) {
            case ROLE -> scope.getByRole(candidate.role(),
                    new Locator.GetByRoleOptions().setName(candidate.value()).setExact(candidate.exact()));
            case LABEL -> scope.getByLabel(candidate.value(),
                    new Locator.GetByLabelOptions().setExact(candidate.exact()));
            case TEST_ID -> scope.getByTestId(candidate.value());
            case PLACEHOLDER -> scope.getByPlaceholder(candidate.value(),
                    new Locator.GetByPlaceholderOptions().setExact(candidate.exact()));
            case ALT_TEXT -> scope.getByAltText(candidate.value(),
                    new Locator.GetByAltTextOptions().setExact(candidate.exact()));
            case TITLE -> scope.getByTitle(candidate.value(),
                    new Locator.GetByTitleOptions().setExact(candidate.exact()));
            case TEXT -> scope.getByText(candidate.value(),
                    new Locator.GetByTextOptions().setExact(candidate.exact()));
            case CSS -> scope.locator(candidate.value());
            case XPATH -> scope.locator("xpath=" + candidate.value());
        };
    }

}
