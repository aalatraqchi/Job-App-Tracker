package com.ashraf.jobapptracker.security;

import com.ashraf.jobapptracker.CsrfTestClient;
import com.ashraf.jobapptracker.PostgresTestSupport;
import com.ashraf.jobapptracker.application.ApplicationService;
import com.ashraf.jobapptracker.application.CreateApplicationRequest;
import com.ashraf.jobapptracker.interview.CreateInterviewRequest;
import com.ashraf.jobapptracker.interview.InterviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import tools.jackson.databind.json.JsonMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LocalSecurityTests extends PostgresTestSupport {
    @LocalServerPort private int port;
    @Autowired private ApplicationService applications;
    @Autowired private InterviewService interviews;
    @Autowired private ApplicationContext context;
    private final JsonMapper mapper = new JsonMapper();

    @Test
    void readingPagesApiAndTokenNeedsNoAccountOrLogin() throws Exception {
        try (var client = CsrfTestClient.newClient()) {
            for (String path : List.of("/tracker", "/tracker/new", "/applications", "/health", "/about", "/css/tracker.css")) {
                var response = client.send(request(path).GET().build(), HttpResponse.BodyHandlers.ofString());
                assertEquals(200, response.statusCode(), path);
                assertTrue(response.headers().firstValue("WWW-Authenticate").isEmpty());
            }
            var response = client.send(request("/csrf").GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertTrue(response.headers().firstValue("Cache-Control").orElse("").contains("no-store"));
            var token = mapper.readValue(response.body(), Map.class);
            assertFalse(((String) token.get("token")).isBlank());
            assertEquals("X-CSRF-TOKEN", token.get("headerName"));
            assertEquals("_csrf", token.get("parameterName"));
            assertEquals(404, client.send(request("/login").GET().build(), HttpResponse.BodyHandlers.ofString()).statusCode());
            assertTrue(context.getBeansOfType(UserDetailsService.class).isEmpty());
        }
    }

    @Test
    void apiWritesRejectMissingAndInvalidTokensWithoutChangingRecords() throws Exception {
        long id = application();
        long stageId = interviews.create(id, new CreateInterviewRequest("Keep stage", null, null, null)).getId();
        String app = "/applications/" + id;
        String stages = app + "/interviews";
        for (String path : List.of("/applications", app, stages, stages + "/" + stageId)) {
            for (String method : path.equals(app) || path.endsWith("/" + stageId)
                    ? List.of("PUT", "DELETE") : List.of("POST")) {
                for (boolean invalidToken : List.of(false, true)) {
                    try (var client = CsrfTestClient.newClient()) {
                        var builder = request(path).header("Content-Type", "application/json")
                                .method(method, HttpRequest.BodyPublishers.ofString("{}"));
                        if (invalidToken) { builder.header("X-CSRF-TOKEN", "invalid-token"); }
                        var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                        assertEquals(403, response.statusCode(), method + " " + path);
                        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
                        assertTrue(mapper.readValue(response.body(), Map.class).get("message").toString()
                                .contains("Fetch /csrf"));
                    }
                }
            }
        }
        assertEquals(1, applications.findAll().size());
        assertEquals("Keep company", applications.findById(id).getCompany());
        assertEquals("Keep stage", interviews.findById(id, stageId).getStageName());
    }

    @Test
    void browserWritesRejectMissingAndInvalidTokensWithHelpfulHtml() throws Exception {
        long id = application();
        long stageId = interviews.create(id, new CreateInterviewRequest("Keep stage", null, null, null)).getId();
        String app = "/tracker/" + id;
        String stages = app + "/interviews";
        for (String path : List.of("/tracker", app + "/edit", app + "/delete", stages,
                stages + "/" + stageId + "/edit", stages + "/" + stageId + "/delete")) {
            for (boolean invalidToken : List.of(false, true)) {
                try (var client = CsrfTestClient.newClient()) {
                    var builder = request(path).header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(HttpRequest.BodyPublishers.ofString("confirmed=true&company=Changed"));
                    if (invalidToken) { builder.header("X-CSRF-TOKEN", "invalid-token"); }
                    var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                    assertEquals(403, response.statusCode(), path);
                    assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("text/html"));
                    assertTrue(response.body().contains("Please reopen the form"));
                }
            }
        }
        assertEquals(1, applications.findAll().size());
        assertEquals("Keep company", applications.findById(id).getCompany());
        assertEquals("Keep stage", interviews.findById(id, stageId).getStageName());
    }

    @Test
    void tokenWorksOnlyWithItsOwnSessionAndAllowsApiPractice() throws Exception {
        try (var first = CsrfTestClient.newClient(); var other = CsrfTestClient.newClient()) {
            var tokenResponse = first.send(request("/csrf").GET().build(), HttpResponse.BodyHandlers.ofString());
            var token = mapper.readValue(tokenResponse.body(), Map.class);
            var create = request("/applications").header("Content-Type", "application/json")
                    .header((String) token.get("headerName"), (String) token.get("token"))
                    .POST(HttpRequest.BodyPublishers.ofString("""
                            {"company":"Token test","position":"Developer","appliedDate":"2026-10-08"}
                            """)).build();
            assertEquals(403, other.send(create, HttpResponse.BodyHandlers.ofString()).statusCode());
            assertTrue(applications.findAll().isEmpty());
            assertEquals(201, first.send(create, HttpResponse.BodyHandlers.ofString()).statusCode());
            assertEquals(1, applications.findAll().size());
        }
    }

    @Test
    void allBrowserWriteFormsIncludeHiddenCsrfTokens() throws Exception {
        long id = application();
        long stageId = interviews.create(id, new CreateInterviewRequest("Stage", null, null, null)).getId();
        String app = "/tracker/" + id;
        String stages = app + "/interviews";
        try (var client = CsrfTestClient.newClient()) {
            for (String path : List.of("/tracker/new", app + "/edit", app + "/delete", stages + "/new",
                    stages + "/" + stageId + "/edit", stages + "/" + stageId + "/delete")) {
                var page = client.send(request(path).GET().build(), HttpResponse.BodyHandlers.ofString());
                assertEquals(200, page.statusCode(), path);
                assertTrue(page.body().matches("(?s).*type=\"hidden\"[^>]*name=\"_csrf\"[^>]*value=\"[^\"]+\".*"), path);
            }
        }
    }

    private long application() {
        return applications.create(new CreateApplicationRequest("Keep company", "Developer",
                LocalDate.of(2026, 10, 8), null, null, null)).getId();
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
    }
}
