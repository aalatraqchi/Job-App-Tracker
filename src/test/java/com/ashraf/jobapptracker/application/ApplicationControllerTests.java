package com.ashraf.jobapptracker.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationControllerTests {
    @LocalServerPort
    private int port;

    @Test
    void postCreatesApplicationAndDefaultsOmittedStatus() throws Exception {
        String json = """
                {
                  "company": "Acme",
                  "position": "Java Developer",
                  "appliedDate": "2026-10-08",
                  "jobUrl": "https://example.com/jobs/1",
                  "notes": "Applied online"
                }
                """;

        Map<?, ?> body = postApplication(json);

        assertTrue(((Number) body.get("id")).longValue() > 0);
        assertEquals("Acme", body.get("company"));
        assertEquals("Java Developer", body.get("position"));
        assertEquals("2026-10-08", body.get("appliedDate"));
        assertEquals("APPLIED", body.get("status"));
        assertEquals("https://example.com/jobs/1", body.get("jobUrl"));
        assertEquals("Applied online", body.get("notes"));
        assertEquals(7, body.size());
    }

    @Test
    void postPreservesExplicitStatus() throws Exception {
        String json = """
                {
                  "company": "Acme",
                  "position": "Java Developer",
                  "appliedDate": "2026-10-08",
                  "status": "INTERVIEW"
                }
                """;

        Map<?, ?> body = postApplication(json);

        assertEquals("INTERVIEW", body.get("status"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getListsCreatedApplicationsAndInitiallyReturnsEmptyArray() throws Exception {
        assertTrue(getApplications().isEmpty());

        String json = """
                {
                  "company": "Acme",
                  "position": "Java Developer",
                  "appliedDate": "2026-10-08"
                }
                """;
        Map<?, ?> first = postApplication(json);
        Map<?, ?> second = postApplication(json);

        List<?> applications = getApplications();

        assertNotEquals(first.get("id"), second.get("id"));
        assertEquals(2, applications.size());
        assertTrue(applications.contains(first));
        assertTrue(applications.contains(second));
    }

    private List<?> getApplications() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/applications"))
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
            return new JsonMapper().readValue(response.body(), List.class);
        }
    }

    @Test
    void getByIdReturnsTheCreatedResponse() throws Exception {
        Map<?, ?> created = postApplication("""
                {
                  "company": "Acme",
                  "position": "Java Developer",
                  "appliedDate": "2026-10-08",
                  "status": "INTERVIEW",
                  "jobUrl": "https://example.com/jobs/1",
                  "notes": "Interview scheduled"
                }
                """);

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(URI.create(
                    "http://127.0.0.1:" + port + "/applications/" + created.get("id")))
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
            assertEquals(created, new JsonMapper().readValue(response.body(), Map.class));
        }
    }

    private Map<?, ?> postApplication(String json) throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/applications"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(201, response.statusCode());
            assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
            return new JsonMapper().readValue(response.body(), Map.class);
        }
    }
}
