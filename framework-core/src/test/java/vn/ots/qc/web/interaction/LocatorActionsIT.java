package vn.ots.qc.web.interaction;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.testng.annotations.Test;
import vn.ots.qc.web.config.FrameworkConfig;
import vn.ots.qc.web.browser.BrowserFactory;
import vn.ots.qc.web.browser.BrowserTarget;
import vn.ots.qc.web.locator.WebLocator;

import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static vn.ots.qc.web.locator.WebLocators.label;
import static vn.ots.qc.web.locator.WebLocators.role;
import static vn.ots.qc.web.locator.WebLocators.testId;

public final class LocatorActionsIT {
    @Test(timeOut = 30_000)
    public void exercisesLogicalActionsAndPageEvents() throws Exception {
        FrameworkConfig config = FrameworkConfig.load().withOverrides(Map.of("locator.resolve.timeout", "50"));
        try (Playwright playwright = Playwright.create();
             Browser browser = new BrowserFactory().create(playwright, new BrowserTarget("chromium", ""), config)) {
            Page page = browser.newPage();
            page.setContent("""
                    <form data-testid="account-form">
                      <label>Email <input aria-label="Email"></label>
                      <button type="button" onclick="document.querySelector('output').textContent='saved'">Save</button>
                      <output></output>
                    </form>
                    <input type="file" data-testid="upload"
                           onchange="document.querySelector('#filename').textContent=this.files[0].name">
                    <span id="filename"></span>
                    <a data-testid="download" download="report.txt" href="data:text/plain,download-contract">Download</a>
                    <button data-testid="dialog" onclick="alert('confirm-contract')">Dialog</button>
                    <button data-testid="popup" onclick="window.open('about:blank', '_blank')">Popup</button>
                    """);

            ElementActions form = new ElementActions(page, config).within(
                    WebLocator.of("account.form", testId("account-form")));
            form.fill(WebLocator.of("account.email", testId("missing-email"), label("Email")), "qa@example.test");
            form.click(WebLocator.of("account.save", role(AriaRole.BUTTON, "Save")));

            assertEquals(page.locator("input[aria-label='Email']").inputValue(), "qa@example.test");
            assertEquals(page.locator("output").textContent(), "saved");

            ElementActions actions = new ElementActions(page, config);
            Path directory = Files.createTempDirectory("playwright-transfer-contract");
            Path upload = directory.resolve("input.txt");
            Path download = directory.resolve("result.txt");
            Files.writeString(upload, "upload-contract");
            try {
                actions.upload(WebLocator.of("transfer.upload", testId("upload")), upload);
                assertEquals(page.locator("#filename").textContent(), "input.txt");

                BrowserEvents events = new BrowserEvents(page, config);
                events.downloadTo(() -> actions.click(WebLocator.of("transfer.download", testId("download"))), download);
                assertEquals(Files.readString(download), "download-contract");
                assertEquals(events.acceptNextDialog(
                        () -> actions.click(WebLocator.of("events.dialog", testId("dialog")))).message(),
                        "confirm-contract");
                Page popup = events.popup(() -> actions.click(WebLocator.of("events.popup", testId("popup"))));
                assertTrue(popup.url().startsWith("about:blank"));
                popup.close();
            } finally {
                Files.deleteIfExists(download);
                Files.deleteIfExists(upload);
                Files.deleteIfExists(directory);
            }
        }
    }
}
