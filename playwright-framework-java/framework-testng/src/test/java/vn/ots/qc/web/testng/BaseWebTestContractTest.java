package vn.ots.qc.web.testng;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public final class BaseWebTestContractTest {
    @Test
    public void exposesAlwaysRunLifecycleAndParallelTargetProvider() throws Exception {
        BeforeMethod before = BaseWebTest.class
                .getMethod("startWebTest", java.lang.reflect.Method.class, Object[].class)
                .getAnnotation(BeforeMethod.class);
        AfterMethod after = BaseWebTest.class
                .getMethod("finishWebTest", org.testng.ITestResult.class)
                .getAnnotation(AfterMethod.class);
        DataProvider provider = BaseWebTest.class.getMethod("browserTargets").getAnnotation(DataProvider.class);

        assertTrue(before.alwaysRun());
        assertTrue(after.alwaysRun());
        assertEquals(provider.name(), "browserTargets");
        assertTrue(provider.parallel());
    }
}
