package com.ashraf.jobapptracker.web;

import com.ashraf.jobapptracker.PostgresTestSupport;
import com.ashraf.jobapptracker.CsrfTestClient;
import com.ashraf.jobapptracker.application.*;
import com.ashraf.jobapptracker.interview.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InterviewPageTests extends PostgresTestSupport {
    @LocalServerPort private int port;
    @Autowired private ApplicationService applications;
    @Autowired private InterviewService interviews;
    private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    @Test
    void browserCreateEditAndConfirmedDeleteWorkWithOffsetAwareTime() throws Exception {
        long id = application();
        String path = "/tracker/" + id + "/interviews";
        assertTrue(get("/tracker/" + id).body().contains("No interview stages yet"));
        assertEquals(200, get(path + "/new").statusCode());
        assertEquals(302, post(path, fields()).statusCode());
        var stage = interviews.findAll(id).getFirst();
        assertNull(stage.getScheduledAt());
        var edited = fields();
        edited.put("stageName", "Technical interview");
        edited.put("scheduledAt", "2026-11-10T14:30:00");
        edited.put("utcOffset", "-05:00");
        edited.put("outcome", "PASSED");
        String stagePath = path + "/" + stage.getId();
        assertEquals(302, post(stagePath + "/edit", edited).statusCode());
        var stored = interviews.findById(id, stage.getId());
        assertEquals(OffsetDateTime.parse("2026-11-10T14:30:00-05:00").toInstant(), stored.getScheduledAt().toInstant());
        var editPage = get(stagePath + "/edit");
        assertEquals(200, editPage.statusCode());
        assertTrue(editPage.body().contains("Technical interview"));
        var details = get("/tracker/" + id);
        assertEquals(200, details.statusCode());
        assertTrue(details.body().contains("PASSED"));
        assertTrue(details.body().contains("<time"));
        assertEquals(ApplicationStatus.APPLIED, applications.findById(id).getStatus());
        assertEquals(200, get(stagePath + "/delete").statusCode());
        assertEquals(1, interviews.findAll(id).size());
        assertEquals(302, post(stagePath + "/delete", Map.of()).statusCode());
        assertEquals(1, interviews.findAll(id).size());
        assertEquals(302, post(stagePath + "/delete", Map.of("confirmed", "true")).statusCode());
        assertTrue(interviews.findAll(id).isEmpty());
        assertNotNull(applications.findById(id));
    }

    @Test
    void invalidFormValuesAreRetainedAndDoNotCreateOrChangeStages() throws Exception {
        long id = application();
        String path = "/tracker/" + id + "/interviews";
        post(path, fields());
        long stageId = interviews.findAll(id).getFirst().getId();
        Map<String, String> badValues = Map.of("stageName", " ", "scheduledAt", "invalid-date",
                "utcOffset", "+19:00", "outcome", "UNKNOWN", "notes", "x".repeat(5001));
        for (var invalid : badValues.entrySet()) {
            var fields = fields();
            fields.put(invalid.getKey(), invalid.getValue());
            for (String route : new String[]{path, path + "/" + stageId + "/edit"}) {
                var response = post(route, fields);
                assertEquals(200, response.statusCode(), invalid.getKey());
                assertTrue(response.body().contains("Check the highlighted fields"));
                assertTrue(response.body().contains(fields.get("notes")), "Submitted notes should remain visible");
            }
        }
        assertEquals(1, interviews.findAll(id).size());
        assertEquals("Phone screen", interviews.findById(id, stageId).getStageName());
        assertEquals("Keep these notes", interviews.findById(id, stageId).getNotes());
    }

    @Test
    void wrongParentPagesAndPostsReturnHtml404AndUserTextIsEscaped() throws Exception {
        long owner = application();
        long other = application();
        var stage = interviews.create(owner, new CreateInterviewRequest("<script>unsafe()</script>",
                null, null, "<b>literal notes</b>"));
        String path = "/tracker/" + other + "/interviews/" + stage.getId();
        for (String suffix : new String[]{"/edit", "/delete"}) {
            for (var response : List.of(get(path + suffix), post(path + suffix, fields()))) {
                assertEquals(404, response.statusCode());
                assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("text/html"));
            }
        }
        assertEquals(404, get("/tracker/-1/interviews/new").statusCode());
        assertEquals(400, get("/tracker/" + owner + "/interviews/wrong/edit").statusCode());
        var page = get("/tracker/" + owner);
        assertTrue(page.body().contains("&lt;script&gt;unsafe()&lt;/script&gt;"));
        assertTrue(page.body().contains("&lt;b&gt;literal notes&lt;/b&gt;"));
        assertFalse(page.body().contains("<script>unsafe()"));
        assertEquals(1, interviews.findAll(owner).size());
    }

    private long application() {
        return applications.create(new CreateApplicationRequest("Acme", "Developer",
                LocalDate.of(2026, 10, 8), null, null, null)).getId();
    }

    private Map<String, String> fields() {
        return new LinkedHashMap<>(Map.of("stageName", "Phone screen", "scheduledAt", "",
                "utcOffset", "+00:00", "outcome", "PENDING", "notes", "Keep these notes"));
    }

    private HttpResponse<String> get(String path) throws Exception { return send(path, null); }
    private HttpResponse<String> post(String path, Map<String, String> fields) throws Exception { return send(path, fields); }

    private HttpResponse<String> send(String path, Map<String, String> fields) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
        if (fields != null) {
            String body = fields.entrySet().stream().map(entry ->
                    URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                            + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
            request.header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
        }
        try (var client = HttpClient.newBuilder().cookieHandler(cookies).build()) {
            if (fields != null) { CsrfTestClient.withCsrf(client, request, port); }
            return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
