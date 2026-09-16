package vn.ots.qc.web.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import vn.ots.qc.web.config.FrameworkConfig;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public final class CucumberAdapterContractTest {
    @Test
    public void lifecycleHooksRunAtFrameworkBoundary() throws Exception {
        Before before = CucumberLifecycleHooks.class.getMethod("startBrowser").getAnnotation(Before.class);
        After after = CucumberLifecycleHooks.class
                .getMethod("finishBrowser", io.cucumber.java.Scenario.class)
                .getAnnotation(After.class);

        assertEquals(before.order(), Integer.MIN_VALUE);
        assertEquals(after.order(), Integer.MIN_VALUE);
    }

    @Test
    public void matrixProviderIsParallelAndTraceAttachmentIsSecureByDefault() throws Exception {
        DataProvider provider = AbstractMatrixCucumberTest.class.getMethod("scenarios")
                .getAnnotation(DataProvider.class);

        assertTrue(provider.parallel());
        assertFalse(FrameworkConfig.load().getBoolean("evidence.attach.trace"));
    }
}
