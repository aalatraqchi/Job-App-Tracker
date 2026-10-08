# Part 5 — Create and list applications over HTTP

Builds on [[Job Apps Tracker/Learning/Part 4|Part 4]]. Your GET handler is reviewed and correct.

## What to use and when

| Use | When / result in this app |
| --- | --- |
| `@RestController` | On a class handling HTTP requests and returning JSON. |
| `@RequestMapping("/applications")` | Set the shared URL path for this controller. |
| `@PostMapping` | Run a method for POST requests: create an application. |
| `@GetMapping` | Run a method for GET requests: list applications. |
| `@RequestBody CreateApplicationRequest request` | Receive the client's JSON as a request object you can use. Send `Content-Type: application/json`. |
| Constructor parameter `ApplicationService service` | Let Spring supply the shared service. Save it in the field; do not create a new service per request. |
| `toResponse(application)` | Convert a stored model into the DTO returned to the client. |
| `ResponseEntity.status(HttpStatus.CREATED).body(...)` | Return the created DTO with HTTP 201. |
| `List<ApplicationResponse>` | Return multiple response DTOs as a JSON array with HTTP 200. An empty list becomes `[]`. |

## Your GET handler

```java
@GetMapping
public List<ApplicationResponse> listApps() {
    List<ApplicationResponse> list = new ArrayList<>();
    for (JobApplication application : service.findAll()) {
        list.add(toResponse(application));
    }
    return list;
}
```

Read it as: get stored applications → convert each to a response → return the list. The local result list is new each call; stored applications stay in the shared service. The annotations set the URL, rather than the Java method name.

## Run and try it

From the project folder, run `.\mvnw.cmd spring-boot:run`. In a second PowerShell terminal:

```powershell
$applicationBody = @{ company = 'Acme'; position = 'Java Developer'; appliedDate = '2026-10-08' } | ConvertTo-Json
Invoke-RestMethod 'http://127.0.0.1:8080/applications' -Method Post -ContentType 'application/json' -Body $applicationBody
Invoke-RestMethod 'http://127.0.0.1:8080/applications'
```

POST returns a generated ID and defaults omitted status to APPLIED. Add `status = 'INTERVIEW'` to supply a status. GET lists stored applications; order is unspecified. The browser address bar also sends GET.

Stop with Ctrl+C. Restart after Java edits. Restarting clears storage. Tests use separate storage from your manually running app. Required-field validation comes later.

## Review and verification — 10/8/26

Your `listApps()` correctly calls the shared service, maps every model, and returns the result list. Preserved your code. Added an HTTP test checking an initial empty array and then both created records with distinct IDs and their expected response fields. `@DirtiesContext` gives that test fresh application storage before it runs.

Maven verify passed: **eight tests, zero failures/errors, successful JAR packaging**. Step 3 create/list behavior is verified; feature PR review/merge is pending.

## I should be able to...

- [x] Explain how constructor injection gives the controller the shared service.
- [x] Explain how the class path and HTTP-method annotation select a handler.
- [x] Trace JSON through request conversion, service creation, and response mapping.
- [x] Explain why creation returns 201 and listing returns 200.
- [x] Distinguish a returned response DTO from a stored model.
- [x] Implement GET using service.findAll(), the mapper, and a result list; have it reviewed.

Your concept checkmarks are preserved as self-assessment; the GET implementation has been independently reviewed and tested. No new exercise or roadmap step starts during this review.
