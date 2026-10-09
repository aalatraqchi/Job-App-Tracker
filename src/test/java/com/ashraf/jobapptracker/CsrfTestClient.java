package com.ashraf.jobapptracker;

import tools.jackson.databind.json.JsonMapper;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public final class CsrfTestClient {
    private CsrfTestClient() { }

    public static HttpClient newClient() {
        return HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    }

    public static HttpRequest.Builder withCsrf(HttpClient client, HttpRequest.Builder request, int port)
            throws Exception {
        var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/csrf"))
                .GET().build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) { throw new AssertionError("Could not obtain a CSRF token."); }
        var token = new JsonMapper().readValue(response.body(), Map.class);
        return request.header((String) token.get("headerName"), (String) token.get("token"));
    }
}
