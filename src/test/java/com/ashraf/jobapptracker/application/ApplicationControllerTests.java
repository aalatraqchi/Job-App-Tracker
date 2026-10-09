package com.ashraf.jobapptracker.application;

import com.ashraf.jobapptracker.PostgresTestSupport;
import com.ashraf.jobapptracker.CsrfTestClient;

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
class ApplicationControllerTests extends PostgresTestSupport {
    @LocalServerPort
    private int port;

    @Test
    void putReturnsUpdatedDetailsAndLaterGetSeesSameRecord() throws Exception {
        Map<?, ?> created = postApplication("""
                {"company":"Acme","position":"Java Developer",
                 "appliedDate":"2026-10-08","status":"APPLIED"}
                """);
        int countBefore = getApplications().size();
        String path = "/applications/" + created.get("id");

        var response = putApplication(path, """
                {"company":"New Company","position":"Backend Developer",
                 "appliedDate":"2026-10-09","status":"INTERVIEW",
                 "jobUrl":"https://example.com/jobs/2","notes":"Interview scheduled"}
                """);

        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        Map<?, ?> updated = new JsonMapper().readValue(response.body(), Map.class);
        assertEquals(Map.of(
                "id", created.get("id"), "company", "New Company",
                "position", "Backend Developer", "appliedDate", "2026-10-09",
                "status", "INTERVIEW", "jobUrl", "https://example.com/jobs/2",
                "notes", "Interview scheduled"
        ), updated);
        assertEquals(countBefore, getApplications().size());

        try (var client = HttpClient.newHttpClient()) {
            var get = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                    .GET().build();
            var retrieved = client.send(get, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, retrieved.statusCode());
            assertEquals(updated, new JsonMapper().readValue(retrieved.body(), Map.class));
        }
    }

    @Test
    void putPreservesStatusWhenOmitted() throws Exception {
        Map<?, ?> created = postApplication("""
                {"company":"Acme","position":"Java Developer",
                 "appliedDate":"2026-10-08","status":"INTERVIEW"}
                """);

        var response = putApplication("/applications/" + created.get("id"), """
                {"company":"Acme","position":"Senior Java Developer",
                 "appliedDate":"2026-10-08"}
                """);

        assertEquals(200, response.statusCode());
        Map<?, ?> updated = new JsonMapper().readValue(response.body(), Map.class);
        assertEquals(created.get("id"), updated.get("id"));
        assertEquals("INTERVIEW", updated.get("status"));
        assertEquals("Senior Java Developer", updated.get("position"));
    }

    @Test
    void putForMissingIdReturnsJson404WithoutCreatingRecord() throws Exception {
        int countBefore = getApplications().size();
        var response = putApplication("/applications/-1", """
                {"company":"Acme","position":"Java Developer",
                 "appliedDate":"2026-10-08","status":"APPLIED"}
                """);

        assertEquals(404, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        assertEquals("Application -1 was not found",
                new JsonMapper().readValue(response.body(), Map.class).get("message"));
        assertEquals(countBefore, getApplications().size());
    }

    private HttpResponse<String> putApplication(String path, String json) throws Exception {
        try (var client = CsrfTestClient.newClient()) {
            var request = CsrfTestClient.withCsrf(client,
                    HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)), port)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json)).build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

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
        try (var client = CsrfTestClient.newClient()) {
            var request = CsrfTestClient.withCsrf(client,
                    HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/applications")), port)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(201, response.statusCode());
            assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
            return new JsonMapper().readValue(response.body(), Map.class);
        }
    }

    @Test
    void getByMissingIdReturns404() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(URI.create(
                            "http://127.0.0.1:" + port + "/applications/-1"))
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
            assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
            assertEquals("Application -1 was not found", new JsonMapper().readValue(response.body(), Map.class).get("message"));
        }
    }
}
