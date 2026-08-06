package vn.ots.qc.web.testng;

import org.testng.ITestResult;
import org.testng.annotations.Test;
import vn.ots.qc.web.artifact.EvidenceBundle;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertSame;

public final class TestArtifactReporterTest {
    @Test
    public void publishesEvidenceAsAStableTestResultAttribute() {
        Map<String, Object> attributes = new HashMap<>();
        ITestResult result = (ITestResult) Proxy.newProxyInstance(
                ITestResult.class.getClassLoader(),
                new Class<?>[]{ITestResult.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("setAttribute")) {
                        attributes.put((String) arguments[0], arguments[1]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        EvidenceBundle evidence = new EvidenceBundle(null, null, null);

        TestArtifactReporter.publish(result, evidence);

        assertSame(attributes.get(TestArtifactReporter.EVIDENCE_ATTRIBUTE), evidence);
    }
}
