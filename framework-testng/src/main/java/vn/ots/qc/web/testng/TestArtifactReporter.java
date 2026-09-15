package vn.ots.qc.web.testng;

import org.testng.ITestResult;
import org.testng.Reporter;
import vn.ots.qc.web.artifact.EvidenceBundle;

import java.nio.file.Path;

/** Publishes artifact paths without coupling the core to a reporting vendor. */
public final class TestArtifactReporter {
    public static final String EVIDENCE_ATTRIBUTE = "playwright.evidence";

    private TestArtifactReporter() {
    }

    public static void publish(ITestResult result, EvidenceBundle evidence) {
        result.setAttribute(EVIDENCE_ATTRIBUTE, evidence);
        report("Screenshot", evidence.screenshot());
        report("Page source", evidence.html());
        report("Playwright trace", evidence.trace());
    }

    private static void report(String label, Path path) {
        if (path != null) {
            Reporter.log(label + ": " + path.toAbsolutePath().normalize(), true);
        }
    }
}
