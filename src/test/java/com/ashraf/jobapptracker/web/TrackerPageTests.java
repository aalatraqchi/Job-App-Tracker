package com.ashraf.jobapptracker.web;

import com.ashraf.jobapptracker.PostgresTestSupport;

import com.ashraf.jobapptracker.application.ApplicationService;
import com.ashraf.jobapptracker.application.CreateApplicationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrackerPageTests extends PostgresTestSupport {
    private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationService service;

    @Test
    void editPrefillsValuesAndSavesIntoDetailListAndApi() throws Exception {
        var application = service.create(new CreateApplicationRequest(
                "Original Company", "Developer", LocalDate.of(2026, 10, 8),
                com.ashraf.jobapptracker.application.ApplicationStatus.APPLIED, null, "Original notes"));
        long id = application.getId();
        int before = service.findAll().size();
        var edit = get("/tracker/" + id + "/edit");
        assertEquals(200, edit.statusCode());
        assertTrue(edit.body().contains("Edit application"));
        assertTrue(edit.body().contains("value=\"Original Company\""));
        assertTrue(edit.body().contains("action=\"/tracker/" + id + "/edit\""));

        var saved = postFields("/tracker/" + id + "/edit", Map.of(
                "company", "Updated Company", "position", "Senior Developer",
                "appliedDate", "2026-10-09", "status", "OFFER", "jobUrl", "", "notes", "New notes"));
        assertEquals(302, saved.statusCode());
        assertEquals("/tracker/" + id,
                URI.create(saved.headers().firstValue("Location").orElseThrow()).getPath().split(";")[0]);
        var detail = get("/tracker/" + id);
        assertTrue(detail.body().contains("Application updated."));
        assertTrue(detail.body().contains("Senior Developer"));
        assertTrue(get("/tracker").body().contains("Updated Company"));
        var data = new JsonMapper().readValue(get("/applications/" + id).body(), Map.class);
        assertEquals("OFFER", data.get("status"));
        assertEquals("New notes", data.get("notes"));
        assertEquals(before, service.findAll().size());
    }

    @Test
    void invalidEditKeepsSubmittedValuesAndDoesNotOverwriteRecord() throws Exception {
        var application = service.create(new CreateApplicationRequest(
                "Keep Company", "Developer", LocalDate.of(2026, 10, 8), null, null, null));
        var response = postFields("/tracker/" + application.getId() + "/edit", Map.of(
                "company", " ", "position", "Attempted change", "appliedDate", "not-a-date",
                "status", "UNKNOWN", "jobUrl", "http://", "notes", ""));
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Edit application"));
        assertTrue(response.body().contains("Choose a valid application date."));
        assertTrue(response.body().contains("Choose one of the available statuses."));
        assertTrue(response.body().contains("Attempted change"));
        var fieldErrors = postFields("/tracker/" + application.getId() + "/edit", Map.of(
                "company", " ", "position", "Attempted change", "appliedDate", "2026-10-08",
                "status", "APPLIED", "jobUrl", "http://", "notes", ""));
        assertEquals(200, fieldErrors.statusCode());
        assertTrue(fieldErrors.body().contains("Enter a company name."));
        assertTrue(fieldErrors.body().contains("Use a valid link"));
        assertEquals(application.getCompany(), service.findById(application.getId()).getCompany());
    }

    @Test
    void deleteRequiresConfirmationAndReturnsToListWithFeedback() throws Exception {
        var application = service.create(new CreateApplicationRequest(
                "Delete Company", "Developer", LocalDate.of(2026, 10, 8), null, null, null));
        String path = "/tracker/" + application.getId();
        assertTrue(get(path).body().contains(path + "/delete"));
        var confirmation = get(path + "/delete");
        assertEquals(200, confirmation.statusCode());
        assertTrue(confirmation.body().contains("Delete this application?"));
        assertTrue(confirmation.body().contains("Keep application"));
        assertEquals(application.getCompany(), service.findById(application.getId()).getCompany());
        assertEquals(302, postFields(path + "/delete", Map.of()).statusCode());
        assertEquals(application.getCompany(), service.findById(application.getId()).getCompany());
        var deleted = postFields(path + "/delete", Map.of("confirmed", "true"));
        assertEquals(302, deleted.statusCode());
        assertEquals("/tracker",
                URI.create(deleted.headers().firstValue("Location").orElseThrow()).getPath().split(";")[0]);
        assertTrue(get("/tracker").body().contains("Application deleted."));
        assertEquals(404, get(path).statusCode());
        assertEquals(404, get("/applications/" + application.getId()).statusCode());
        assertEquals(404, postFields(path + "/delete", Map.of("confirmed", "true")).statusCode());
    }

    @Test
    void missingEditAndDeleteUseHtml404AndBadIdsUseHtml400() throws Exception {
        for (String suffix : new String[]{"/edit", "/delete"}) {
            var missing = get("/tracker/-1" + suffix);
            assertEquals(404, missing.statusCode());
            assertTrue(missing.headers().firstValue("Content-Type").orElse("").contains("text/html"));
            assertEquals(404, postFields("/tracker/-1" + suffix, Map.of()).statusCode());
            var invalid = get("/tracker/not-a-number" + suffix);
            assertEquals(400, invalid.statusCode());
            assertTrue(invalid.body().contains("Invalid application link"));
        }
    }

    @Test
    void listAndFormRenderHtmlAndStyles() throws Exception {
        var list = get("/");
        assertEquals(200, list.statusCode());
        assertTrue(list.headers().firstValue("Content-Type").orElse("").contains("text/html"));
        assertTrue(list.body().contains("Your applications"));
        assertTrue(list.body().contains("/css/tracker.css"));
        var form = get("/tracker/new");
        assertEquals(200, form.statusCode());
        assertTrue(form.body().contains("name=\"company\""));
        assertTrue(form.body().contains("INTERVIEW"));
        assertEquals(200, get("/css/tracker.css").statusCode());
    }

    @Test
    void formCreatesRecordVisibleInDetailListAndExistingApi() throws Exception {
        int before = service.findAll().size();
        var response = postForm("UI Test Company", "2026-10-08");
        assertEquals(302, response.statusCode());
        String location = response.headers().firstValue("Location").orElseThrow();
        assertTrue(location.contains("/tracker/"));
        var detail = get(location);
        assertEquals(200, detail.statusCode());
        assertTrue(detail.body().contains("UI Test Company"));
        assertTrue(detail.body().contains("Application added."));
        assertTrue(get("/tracker").body().contains("UI Test Company"));
        assertEquals(before + 1, service.findAll().size());
        String id = location.substring(location.lastIndexOf('/') + 1);
        var api = get("/applications/" + id);
        assertEquals(200, api.statusCode());
        var data = new JsonMapper().readValue(api.body(), Map.class);
        assertEquals("UI Test Company", data.get("company"));
        assertEquals("INTERVIEW", data.get("status"));
    }

    @Test
    void blankCompanyShowsErrorAndDoesNotCreateRecord() throws Exception {
        int before = service.findAll().size();
        var response = postForm(" ", "2026-10-08");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Enter a company name."));
        assertTrue(response.body().contains("Java Developer"));
        assertEquals(before, service.findAll().size());
    }

    @Test
    void invalidDateShowsFriendlyErrorAndDoesNotCreateRecord() throws Exception {
        int before = service.findAll().size();
        var response = postForm("Acme", "not-a-date");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Choose a valid application date."));
        assertEquals(before, service.findAll().size());
    }

    @Test
    void missingPageShowsHtml404WhileApiKeepsJson() throws Exception {
        var page = get("/tracker/-1");
        assertEquals(404, page.statusCode());
        assertTrue(page.body().contains("Application not found"));
        assertTrue(page.headers().firstValue("Content-Type").orElse("").contains("text/html"));
        var api = get("/applications/-1");
        assertEquals(404, api.statusCode());
        assertTrue(api.headers().firstValue("Content-Type").orElse("").contains("application/json"));
    }

    @Test
    void detailsEscapeUserTextAndDoNotLinkUnsafeSchemes() throws Exception {
        var application = service.create(new CreateApplicationRequest(
                "<script>alert(1)</script>", "Java Developer", LocalDate.of(2026, 10, 8),
                null, "javascript:alert(1)", "<b>literal notes</b>"
        ));
        var page = get("/tracker/" + application.getId());
        assertEquals(200, page.statusCode());
        assertTrue(page.body().contains("&lt;script&gt;"));
        assertFalse(page.body().contains("<script>alert(1)</script>"));
        assertFalse(page.body().contains("href=\"javascript:"));
        assertTrue(page.body().contains("&lt;b&gt;literal notes&lt;/b&gt;"));
    }

    private HttpResponse<String> get(String path) throws Exception {
        URI uri = path.startsWith("http") ? URI.create(path) : URI.create("http://127.0.0.1:" + port + path);
        try (var client = HttpClient.newBuilder().cookieHandler(cookies).build()) {
            return client.send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString());
        }
    }

    private HttpResponse<String> postForm(String company, String date) throws Exception {
        return postFields("/tracker", Map.of("company", company, "position", "Java Developer", "appliedDate", date,
                "status", "INTERVIEW", "jobUrl", "", "notes", "UI test notes"));
    }

    private HttpResponse<String> postFields(String path, Map<String, String> fields) throws Exception {
        String body = fields
                .entrySet().stream().map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        try (var client = HttpClient.newBuilder().cookieHandler(cookies).build()) {
            return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
