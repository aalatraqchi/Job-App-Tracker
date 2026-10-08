package com.ashraf.jobapptracker.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ApplicationFilterTests {
    @LocalServerPort
    private int port;
    @Autowired
    private ApplicationService service;

    @Test
    void apiFiltersIndividuallyTogetherAndReturnsOrderedEmptyMatches() throws Exception {
        long applied = create("Acme Labs", ApplicationStatus.APPLIED, 7);
        long first = create("ACME Group", ApplicationStatus.INTERVIEW, 9);
        long second = create("Other Company", ApplicationStatus.INTERVIEW, 9);
        assertEquals(List.of(second, first, applied), ids("/applications"));
        assertEquals(List.of(first, applied), ids("/applications?company=%20cMe%20"));
        assertEquals(List.of(second, first), ids("/applications?status=INTERVIEW"));
        assertEquals(List.of(first), ids("/applications?status=INTERVIEW&company=acme"));
        assertEquals(List.of(), ids("/applications?status=OFFER&company=acme"));
        assertEquals(List.of(), ids("/applications?company=missing"));
        assertEquals(List.of(second, first, applied), ids("/applications?status=&company=%20%20"));
    }

    @Test
    void unknownStatusReturnsJson400InsteadOfAnUnfilteredList() throws Exception {
        create("Acme", ApplicationStatus.APPLIED, 8);
        var response = get("/applications?status=INTERVEIW");
        assertEquals(400, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        var error = new JsonMapper().readValue(response.body(), Map.class);
        assertTrue(error.get("message").toString().contains("Choose a valid status"));
        assertEquals(1, service.findAll().size());
    }

    @Test
    void pageKeepsFiltersShowsOnlyMatchesAndProvidesClearAndEmptyStates() throws Exception {
        create("Acme Labs", ApplicationStatus.APPLIED, 7);
        create("ACME Group", ApplicationStatus.INTERVIEW, 9);
        create("Other Company", ApplicationStatus.INTERVIEW, 9);
        var page = get("/tracker?status=INTERVIEW&company=acme");
        assertEquals(200, page.statusCode());
        assertTrue(page.body().contains("ACME Group"));
        assertFalse(page.body().contains("Acme Labs"));
        assertFalse(page.body().contains("Other Company"));
        assertTrue(page.body().contains("value=\"acme\""));
        assertTrue(java.util.regex.Pattern.compile(
                "<option(?=[^>]*value=\"INTERVIEW\")(?=[^>]*selected=\"selected\")[^>]*>")
                .matcher(page.body()).find());
        assertTrue(page.body().contains("Clear filters"));
        assertTrue(page.body().contains("1 application shown"));
        assertTrue(page.body().contains("Total applications</span><strong>3</strong>"));
        var empty = get("/tracker?company=missing");
        assertEquals(200, empty.statusCode());
        assertTrue(empty.body().contains("No applications match"));
        assertFalse(empty.body().contains("Add your first application"));
        assertTrue(get("/tracker").body().contains("Other Company"));
    }

    @Test
    void pageRejectsInvalidStatusWithHtml400() throws Exception {
        var response = get("/tracker?status=UNKNOWN");
        assertEquals(400, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("text/html"));
        assertTrue(response.body().contains("Invalid status filter"));
    }

    private long create(String company, ApplicationStatus status, int day) {
        return service.create(new CreateApplicationRequest(company, "Developer",
                LocalDate.of(2026, 10, day), status, null, null)).getId();
    }

    private List<Long> ids(String path) throws Exception {
        var response = get(path);
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        List<?> data = new JsonMapper().readValue(response.body(), List.class);
        return data.stream().map(item -> ((Number) ((Map<?, ?>) item).get("id")).longValue()).toList();
    }

    private HttpResponse<String> get(String path) throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                    .GET().build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
