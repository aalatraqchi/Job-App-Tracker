package com.ashraf.jobapptracker;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Random port to not conflict with our main port
class JobAppTrackerApplicationTests extends PostgresTestSupport {
	@LocalServerPort
	private int port;

	@Test
	void healthEndpointRespondsOverHttp() throws Exception {
		try (var client = HttpClient.newHttpClient()) {
			var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/health")).build();
			var response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode());
			assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
			assertTrue(response.body().contains("\"status\":\"UP\""));
			assertTrue(response.body().contains("\"application\":\"job-app-tracker\""));
		}
	}

	@Test
	void aboutEndpointRespondsOverHttp() throws Exception {
		try (var client = HttpClient.newHttpClient()) {
			var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/about")).build();
			var response = client.send(request, HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode());
			assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
			assertTrue(response.body().contains("\"project\":\"Job Application Tracker\""));
			assertTrue(response.body().contains("\"purpose\":\"Track job applications and interview stages\""));
		}
	}

}
