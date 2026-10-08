package com.ashraf.jobapptracker.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationCrudTests {
    @LocalServerPort
    private int port;
    private final JsonMapper mapper = new JsonMapper();

    @Test
    void completeCreateRetrieveEditDeleteWorkflow() throws Exception {
        var created = send("POST", "/applications", mapper.writeValueAsString(validFields()));
        assertEquals(201, created.statusCode());
        Map<?, ?> initial = json(created);
        String path = "/applications/" + initial.get("id");
        assertEquals(initial, json(send("GET", path, null)));

        var fields = validFields();
        fields.put("position", "Senior Developer");
        fields.put("status", "OFFER");
        var updated = send("PUT", path, mapper.writeValueAsString(fields));
        assertEquals(200, updated.statusCode());
        assertEquals(initial.get("id"), json(updated).get("id"));
        assertEquals("Senior Developer", json(updated).get("position"));
        assertEquals("OFFER", json(updated).get("status"));

        var deleted = send("DELETE", path, null);
        assertEquals(204, deleted.statusCode());
        assertEquals("", deleted.body());
        assertEquals(404, send("GET", path, null).statusCode());
        assertEquals(404, send("PUT", path, mapper.writeValueAsString(fields)).statusCode());
        assertEquals(404, send("DELETE", path, null).statusCode());
    }

    @Test
    void fieldValidationRejectsCreateAndUpdateWithoutChangingStorage() throws Exception {
        var created = json(send("POST", "/applications", mapper.writeValueAsString(validFields())));
        String path = "/applications/" + created.get("id");
        int count = listSize();
        Map<String, Object> invalidValues = Map.of(
                "company", " ", "position", "", "appliedDate", "MISSING",
                "jobUrl", "javascript:alert(1)", "notes", "n".repeat(5001));
        for (var invalid : invalidValues.entrySet()) {
            var fields = validFields();
            fields.put(invalid.getKey(), invalid.getKey().equals("appliedDate") ? null : invalid.getValue());
            String body = mapper.writeValueAsString(fields);
            for (String method : List.of("POST", "PUT")) {
                var response = send(method, method.equals("POST") ? "/applications" : path, body);
                assertEquals(400, response.statusCode(), method + " " + invalid.getKey());
                Map<?, ?> error = json(response);
                assertEquals("Check the submitted fields.", error.get("message"));
                assertTrue(((Map<?, ?>) error.get("errors")).containsKey(invalid.getKey()));
            }
        }
        assertEquals(count, listSize());
        assertEquals(created, json(send("GET", path, null)));
    }

    @Test
    void textLengthAndUrlRulesApplyToBothRequestDtos() throws Exception {
        var created = json(send("POST", "/applications", mapper.writeValueAsString(validFields())));
        String path = "/applications/" + created.get("id");
        List<Map.Entry<String, String>> cases = List.of(
                Map.entry("company", "x".repeat(201)),
                Map.entry("position", "x".repeat(201)),
                Map.entry("jobUrl", "https://example.com/" + "x".repeat(2000)),
                Map.entry("jobUrl", "http://"),
                Map.entry("jobUrl", "https://bad host/path"),
                Map.entry("jobUrl", "ftp://example.com/job"));
        for (var invalid : cases) {
            var fields = validFields();
            fields.put(invalid.getKey(), invalid.getValue());
            for (String method : List.of("POST", "PUT")) {
                var response = send(method, method.equals("POST") ? "/applications" : path,
                        mapper.writeValueAsString(fields));
                assertEquals(400, response.statusCode(), method + " " + invalid.getKey());
                assertTrue(((Map<?, ?>) json(response).get("errors")).containsKey(invalid.getKey()));
            }
        }
        assertEquals(created, json(send("GET", path, null)));
    }

    @Test
    void malformedJsonDatesAndStatusesReturnConsistentJson400() throws Exception {
        var created = json(send("POST", "/applications", mapper.writeValueAsString(validFields())));
        String path = "/applications/" + created.get("id");
        var invalidStatus = validFields();
        invalidStatus.put("status", "INTERVEIW");
        var invalidDate = validFields();
        invalidDate.put("appliedDate", "2026-02-30");
        for (String body : List.of("{broken", "null", "",
                mapper.writeValueAsString(invalidStatus), mapper.writeValueAsString(invalidDate))) {
            for (String method : List.of("POST", "PUT")) {
                var response = send(method, method.equals("POST") ? "/applications" : path, body);
                assertEquals(400, response.statusCode());
                assertTrue(json(response).get("message") instanceof String);
            }
        }
        assertEquals(created, json(send("GET", path, null)));
    }

    @Test
    void invalidIdReturnsJson400ForAllIdRoutes() throws Exception {
        for (String method : List.of("GET", "PUT", "DELETE")) {
            var response = send(method, "/applications/not-a-number",
                    method.equals("PUT") ? mapper.writeValueAsString(validFields()) : null);
            assertEquals(400, response.statusCode());
            assertEquals("Provide a valid numeric application ID.", json(response).get("message"));
        }
    }

    @Test
    void optionalValuesAndBoundaryLengthsAreAccepted() throws Exception {
        var fields = validFields();
        fields.put("company", "c".repeat(200));
        fields.put("position", "p".repeat(200));
        fields.put("notes", "n".repeat(5000));
        fields.put("jobUrl", "https://example.com/" + "x".repeat(1980));
        var response = send("POST", "/applications", mapper.writeValueAsString(fields));
        assertEquals(201, response.statusCode());
        String path = "/applications/" + json(response).get("id");
        fields.put("jobUrl", "");
        fields.put("notes", null);
        assertEquals(200, send("PUT", path, mapper.writeValueAsString(fields)).statusCode());
    }

    private LinkedHashMap<String, Object> validFields() {
        return new LinkedHashMap<>(Map.of("company", "CRUD Company", "position", "Java Developer",
                "appliedDate", "2026-10-08", "status", "APPLIED"));
    }

    private Map<?, ?> json(HttpResponse<String> response) throws Exception {
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        return mapper.readValue(response.body(), Map.class);
    }

    private int listSize() throws Exception {
        return mapper.readValue(send("GET", "/applications", null).body(), List.class).size();
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
            if (body != null) {
                builder.header("Content-Type", "application/json");
            }
            return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
