package com.ashraf.jobapptracker.interview;

import com.ashraf.jobapptracker.PostgresTestSupport;
import com.ashraf.jobapptracker.application.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.json.JsonMapper;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InterviewControllerTests extends PostgresTestSupport {
    @LocalServerPort private int port;
    @Autowired private ApplicationService applications;
    @Autowired private InterviewRepository repository;
    private final JsonMapper mapper = new JsonMapper();

    @Test
    void createListEditDeleteKeepStagesAndApplicationStatusIndependent() throws Exception {
        long applicationId = application();
        String path = path(applicationId);
        assertEquals(List.of(), list(send("GET", path, null)));
        var firstResponse = send("POST", path, fields());
        assertEquals(201, firstResponse.statusCode());
        var first = json(firstResponse);
        assertEquals("Phone screen", first.get("stageName"));
        assertEquals("PENDING", first.get("outcome"));
        assertNull(first.get("scheduledAt"));
        var second = json(send("POST", path, fields()));
        assertNotEquals(first.get("id"), second.get("id"));
        assertEquals(2, list(send("GET", path, null)).size());

        var edit = fields();
        edit.put("stageName", "Technical interview");
        edit.put("scheduledAt", "2026-11-10T14:30:00-05:00");
        edit.put("outcome", "PASSED");
        edit.put("notes", "Follow up with the team");
        String stagePath = path + "/" + first.get("id");
        var updatedResponse = send("PUT", stagePath, edit);
        assertEquals(200, updatedResponse.statusCode());
        var updated = json(updatedResponse);
        assertEquals(first.get("id"), updated.get("id"));
        assertEquals("PASSED", updated.get("outcome"));
        assertEquals("Follow up with the team", updated.get("notes"));
        assertEquals(OffsetDateTime.parse((String) edit.get("scheduledAt")).toInstant(),
                OffsetDateTime.parse((String) updated.get("scheduledAt")).toInstant());
        assertEquals(ApplicationStatus.APPLIED, applications.findById(applicationId).getStatus());

        var cleared = json(send("PUT", stagePath, fields()));
        assertEquals("PASSED", cleared.get("outcome"));
        assertNull(cleared.get("scheduledAt"));
        assertNull(cleared.get("notes"));
        var deleted = send("DELETE", stagePath, null);
        assertEquals(204, deleted.statusCode());
        assertTrue(deleted.body().isEmpty());
        var remaining = list(send("GET", path, null));
        assertEquals(1, remaining.size());
        assertEquals(second.get("id"), remaining.get(0).get("id"));
    }

    @Test
    void nestedUrlsRejectOtherParentsAndMissingRecordsWithoutChangingData() throws Exception {
        long owner = application();
        long other = application();
        var stage = json(send("POST", path(owner), fields()));
        String wrongPath = path(other) + "/" + stage.get("id");
        for (String method : List.of("PUT", "DELETE")) {
            var response = send(method, wrongPath, method.equals("PUT") ? fields() : null);
            assertEquals(404, response.statusCode());
            assertTrue((json(response).get("message").toString()).contains("was not found for application " + other));
            assertEquals(404, send(method, path(owner) + "/-1", method.equals("PUT") ? fields() : null).statusCode());
        }
        assertEquals(List.of(), list(send("GET", path(other), null)));
        assertEquals(1, list(send("GET", path(owner), null)).size());
        for (String method : List.of("GET", "POST", "PUT", "DELETE")) {
            String missing = path(-1) + (List.of("PUT", "DELETE").contains(method) ? "/1" : "");
            assertEquals(404, send(method, missing, List.of("POST", "PUT").contains(method) ? fields() : null).statusCode());
        }
        assertEquals(400, send("GET", "/applications/not-a-number/interviews", null).statusCode());
        assertEquals(400, send("DELETE", path(owner) + "/not-a-number", null).statusCode());
        assertEquals(1, repository.count());
    }

    @Test
    void validationRejectsBadNamesNotesOutcomesAndTimesForCreateAndUpdate() throws Exception {
        String path = path(application());
        var created = json(send("POST", path, fields()));
        List<Map.Entry<String, Object>> invalidFields = List.of(
                Map.entry("stageName", " "), Map.entry("stageName", "x".repeat(201)),
                Map.entry("notes", "x".repeat(5001)), Map.entry("outcome", "UNKNOWN"),
                Map.entry("scheduledAt", "2026-11-10T14:30:00"),
                Map.entry("scheduledAt", "not-a-date"));
        for (var invalid : invalidFields) {
            var fields = fields();
            fields.put(invalid.getKey(), invalid.getValue());
            for (String method : List.of("POST", "PUT")) {
                var response = send(method, method.equals("POST") ? path : path + "/" + created.get("id"), fields);
                assertEquals(400, response.statusCode(), method + " " + invalid.getKey());
                assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
                assertNotNull(json(response).get("message"));
            }
        }
        assertEquals(List.of(created), list(send("GET", path, null)));
    }

    @Test
    void deletingApplicationRemovesItsStagesAndRetainsOtherApplicationsStages() throws Exception {
        long removed = application();
        long retained = application();
        var first = json(send("POST", path(removed), fields()));
        var second = json(send("POST", path(removed), fields()));
        var other = json(send("POST", path(retained), fields()));
        assertEquals(204, send("DELETE", "/applications/" + removed, null).statusCode());
        assertTrue(repository.findById(((Number) first.get("id")).longValue()).isEmpty());
        assertTrue(repository.findById(((Number) second.get("id")).longValue()).isEmpty());
        assertEquals(List.of(other), list(send("GET", path(retained), null)));
        assertEquals(1, repository.count());
    }

    private long application() {
        return applications.create(new CreateApplicationRequest("Acme", "Developer",
                LocalDate.of(2026, 10, 8), null, null, null)).getId();
    }

    private String path(long id) { return "/applications/" + id + "/interviews"; }

    private Map<String, Object> fields() {
        var fields = new LinkedHashMap<String, Object>();
        fields.put("stageName", "Phone screen");
        return fields;
    }

    private Map<?, ?> json(HttpResponse<String> response) { return mapper.readValue(response.body(), Map.class); }

    @SuppressWarnings("unchecked")
    private List<Map<?, ?>> list(HttpResponse<String> response) { return mapper.readValue(response.body(), List.class); }

    private HttpResponse<String> send(String method, String path, Map<String, Object> body) throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                    .header("Content-Type", "application/json")
                    .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }
}
