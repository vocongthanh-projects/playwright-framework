package vn.ots.qc.consumer.template.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import vn.ots.qc.consumer.template.pages.SamplePage;
import vn.ots.qc.web.testng.BaseWebTest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Offline runner contract; it does not navigate to a product site. */
public final class SmokeTest extends BaseWebTest {
    @Test
    public void startsBrowserAndUsesConsumerPageObject() {
        String html = "<main><h1>Consumer template ready</h1>"
                + "<button onclick=\"document.querySelector('[data-testid=sample-status]').textContent='continued'\">"
                + "Continue</button><p data-testid='sample-status'></p></main>";
        String document = "data:text/html;base64," + Base64.getEncoder()
                .encodeToString(html.getBytes(StandardCharsets.UTF_8));
        page().navigate(document);

        SamplePage samplePage = new SamplePage(page(), config());
        Assert.assertTrue(samplePage.isReady());
        samplePage.continueFlow();
        samplePage.shouldShowContinuedStatus();
    }
}
