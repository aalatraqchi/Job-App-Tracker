## Files created

Java paths below are under `src/main/java/com/ashraf/jobapptracker/`.

| File | What changed |
| --- | --- |
| `security/LocalSecurityConfig.java` | Added `localSecurity()`: allows account-free access, keeps CSRF protection for writes, disables login/Basic/logout, and returns JSON or a helpful HTML 403 when verification fails. |
| `web/RequestProtectionController.java` | Added `csrf()` for API-client tokens with no-store caching, plus `requestExpired()` for the HTML error page. |
| `src/main/resources/templates/tracker/request-expired.html` | Added a 403 page explaining that nothing changed and linking back to reopen the form. |
| `src/test/java/com/ashraf/jobapptracker/CsrfTestClient.java` | Added `newClient()` and `withCsrf()` to obtain a real token and keep its session cookie in ordinary HTTP tests. |
| `src/test/java/com/ashraf/jobapptracker/security/LocalSecurityTests.java` | Covers account-free reads, missing/invalid tokens on all write routes, unchanged records after rejection, session binding, API token use, and hidden tokens in all browser forms. |

## Files edited

| File | What changed |
| --- | --- |
| `pom.xml` | Added Spring Security using Spring Boot's managed dependency version. |
| `JobAppTrackerApplication.java` | Excluded automatic creation of Spring Boot's default user/password; this local version has no accounts. |
| `ApplicationControllerTests`, `ApplicationCrudTests`, `InterviewControllerTests` | Existing API write helpers now fetch a token and submit it with the same client's cookie. Original feature assertions remain. |
| `TrackerPageTests`, `InterviewPageTests` | Existing form POST helpers now send real tokens using their existing session cookies. |
| `README.md`, `Project Outline.md`, `AGENTS.md` | Recorded the approved step 7 merge, token flow, and next backup/restore checkpoint. Verification: 54 tests and packaging pass; Chrome create/edit/add-interview submissions work; the 403 page fits desktop and 390px screens. |

## New content

| Use | Purpose |
| --- | --- |
| `@Configuration` / `@Bean` | Register the `SecurityFilterChain` returned by `localSecurity()` as Spring's request-security configuration. |
| `SecurityFilterChain` / `anyRequest().permitAll()` | Apply request protection while allowing access without a username or login. |
| CSRF token + session cookie | Require proof from the app's own session before accepting a write. Browser forms include the token automatically; API clients fetch `/csrf` and send the returned header plus the same cookie. |
| `formLogin`, `httpBasic`, `logout` disabled | Keep account/login behavior out of this personal version while retaining CSRF checks. |
| `UserDetailsServiceAutoConfiguration` exclusion | Prevent Boot from creating an unwanted default account or printing a generated password. |
| `@ConditionalOnWebApplication(Type.SERVLET)` | Only build HTTP security when running a web server; database-only restart tests do not need it. |
| `CacheControl.noStore()` | Keep the token response out of HTTP caches. |
| `RequestDispatcher.forward()` | Render the friendly HTML error inside the rejected request while retaining HTTP 403. |
