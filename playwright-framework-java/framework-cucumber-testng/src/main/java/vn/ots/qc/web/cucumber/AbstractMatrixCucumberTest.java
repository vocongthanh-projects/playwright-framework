package vn.ots.qc.web.cucumber;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import vn.ots.qc.web.browser.BrowserTarget;
import vn.ots.qc.web.browser.BrowserTargetMatrix;
import vn.ots.qc.web.browser.ExecutionTarget;
import vn.ots.qc.web.config.FrameworkConfig;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Base Cucumber runner that executes every scenario against configured browser targets. */
public abstract class AbstractMatrixCucumberTest extends AbstractTestNGCucumberTests {
    private final Map<String, Deque<BrowserTarget>> targetsByScenario = new LinkedHashMap<>();

    @Override
    @DataProvider(parallel = true)
    public synchronized Object[][] scenarios() {
        Object[][] original = super.scenarios();
        List<BrowserTarget> targets = BrowserTargetMatrix.from(FrameworkConfig.load());
        Object[][] expanded = new Object[original.length * targets.size()][2];
        int row = 0;
        for (Object[] scenario : original) {
            PickleWrapper pickle = (PickleWrapper) scenario[0];
            FeatureWrapper feature = (FeatureWrapper) scenario[1];
            String key = key(pickle, feature);
            Deque<BrowserTarget> queue = targetsByScenario.computeIfAbsent(key, ignored -> new ArrayDeque<>());
            for (BrowserTarget target : targets) {
                expanded[row++] = new Object[]{pickle, feature};
                queue.addLast(target);
            }
        }
        return expanded;
    }

    @Override
    @Test(groups = "cucumber", description = "Runs Cucumber Scenarios", dataProvider = "scenarios")
    public void runScenario(PickleWrapper pickle, FeatureWrapper feature) {
        BrowserTarget target;
        synchronized (this) {
            Deque<BrowserTarget> queue = targetsByScenario.get(key(pickle, feature));
            if (queue == null || queue.isEmpty()) {
                throw new IllegalStateException("No browser target assigned to scenario: " + pickle.getPickle().getName());
            }
            target = queue.removeFirst();
        }
        ExecutionTarget.set(target);
        try {
            super.runScenario(pickle, feature);
        } finally {
            ExecutionTarget.clear();
        }
    }

    private static String key(PickleWrapper pickle, FeatureWrapper feature) {
        return feature.toString() + "::" + pickle.getPickle().getUri() + "::" + pickle.getPickle().getName();
    }
}
