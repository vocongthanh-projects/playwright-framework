package vn.ots.qc.web.cucumber;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public final class CucumberWorldTest {
    @Test
    public void providesScenarioScopedTypedStateWithoutStartingABrowser() {
        CucumberWorld world = new CucumberWorld();
        try {
            world.put("order-id", 42L);
            assertEquals(world.get("order-id", Long.class).orElseThrow(), 42L);
        } finally {
            world.web().close();
        }
    }
}
