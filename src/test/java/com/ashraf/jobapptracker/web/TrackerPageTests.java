package com.ashraf.jobapptracker.web;

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
class TrackerPageTests {
    private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationService service;

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
        String body = Map.of("company", company, "position", "Java Developer", "appliedDate", date,
                        "status", "INTERVIEW", "jobUrl", "", "notes", "UI test notes")
                .entrySet().stream().map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        try (var client = HttpClient.newBuilder().cookieHandler(cookies).build()) {
            return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/tracker"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
