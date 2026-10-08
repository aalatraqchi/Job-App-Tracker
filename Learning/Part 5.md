# Part 5 — Connecting HTTP, services, and response DTOs

Builds on [[Job Apps Tracker/Learning/Part 4|Part 4]]. Current worked example: POST /applications. Current exercise: GET /applications. This is still a local in-memory backend; restarting loses applications.

## Constructor injection: using Spring's shared service

Actual code in `application/ApplicationController.java`:

```java
@RestController
@RequestMapping("/applications")
public class ApplicationController {
    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }
    // Endpoint and mapping methods follow below in the source file.
}
```

Spring discovers the controller, sees its single constructor, and supplies the managed ApplicationService instance. No explicit `@Autowired` is needed on this single constructor. This is **constructor dependency injection**: the controller declares what it needs rather than constructing a fresh service for each request. The stored service reference lets successive requests reach the same map. `final` prevents replacing that reference; the service can still update its contents.

## Request mapping and JSON input

Actual create handler:

```java
@PostMapping
public ResponseEntity<ApplicationResponse> create(@RequestBody CreateApplicationRequest request) {
    JobApplication application = service.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(application));
}
```

The class-level `@RequestMapping("/applications")` supplies the shared path. The method's `@PostMapping` selects POST at that path. A future `@GetMapping` without an additional path will select GET at the same URL. Java method names such as create or findAll are not URL definitions.

`@RequestBody` asks Spring's JSON conversion support to construct CreateApplicationRequest from the HTTP body. JSON property names match record components; a date such as "2026-10-08" becomes LocalDate and "INTERVIEW" becomes the enum value. An omitted status arrives as null, which our service defaults to APPLIED. Send Content-Type: application/json. Malformed dates or unknown enum values cannot be converted and normally produce HTTP 400 before our method runs. Required-field validation and a consistent custom error format come later; this handler has no `@Valid` yet.

## Mapping and the HTTP response

`toResponse(application)` explicitly constructs ApplicationResponse using the model's seven getters. It is a private helper in the controller: plain Java mapping, not an annotation-driven operation. It defines the outgoing contract rather than automatically exposing every future model field. The service returns models; the controller handles this API boundary.

`ResponseEntity<ApplicationResponse>` carries both the HTTP status and the response body. `HttpStatus.CREATED` means 201, appropriate for successful creation. Spring serializes the response record to JSON because the class is a RestController. A normal GET returning a list will use HTTP 200 and serialize the list as a JSON array.

Flow now working:

```text
POST JSON → CreateApplicationRequest → controller → shared service
           → new JobApplication + map storage
           → controller toResponse → ApplicationResponse → HTTP 201 JSON
```

## Run and try POST yourself

Start the app from the project folder in a terminal:

```powershell
.\mvnw.cmd spring-boot:run
```

Leave it running; use a second PowerShell terminal for the request:

```powershell
$applicationBody = @{
    company = 'Acme'
    position = 'Java Developer'
    appliedDate = '2026-10-08'
} | ConvertTo-Json

Invoke-RestMethod -Uri 'http://127.0.0.1:8080/applications' `
    -Method Post -ContentType 'application/json' -Body $applicationBody
```

Expect a positive id, the supplied details, and status APPLIED. PowerShell displays the JSON as a converted object. Adding status = 'INTERVIEW' to the input keeps that status instead. A browser address bar sends GET, so it will not invoke this POST handler. Before the GET exercise is implemented, GET /applications returns 405 (method not allowed).

Stop the server with Ctrl+C in its terminal. Restart after changing Java code; browser refresh alone does not reload compiled code. Tests use their own random-port server and storage; they do not populate your manually running app.

## HTTP tests: exercising the whole boundary

`ApplicationControllerTests.java` uses the same HttpClient pattern as your health/about tests, with `.POST(BodyPublishers.ofString(json))` and a JSON content-type header. Its two tests verify HTTP 201, JSON response details including date/ID/default status, and preservation of explicit INTERVIEW status. This covers request conversion and response mapping that service-only tests cannot cover.

The test helper parses the response using Jackson's JsonMapper into a Map so assertions compare property values independently of JSON whitespace or property order. This map is only a test inspection tool; the API still uses DTOs. `tools.jackson` is the Jackson 3 package bundled with our Spring Boot version. No extra dependency was added.

Verification: Maven test passes with seven tests (three service, two application POST HTTP, and two health/about HTTP tests), zero failures/errors. GET is your pending exercise.

## Your exercise — list applications over HTTP

In ApplicationController, add a public method with `@GetMapping` and return type `List<ApplicationResponse>`:

1. Import GetMapping, java.util.List, and java.util.ArrayList.
2. Create an empty `ArrayList<ApplicationResponse>` inside the method. A local result list is appropriate: each response needs its own list, while stored models remain in the shared service.
3. Call `service.findAll()` to obtain the stored models.
4. For each model, call the existing `toResponse(...)` helper and add that response to your result list.
5. Return the result list. Spring will serialize it to a JSON array; return an empty list when storage is empty.

An ArrayList is a growable list; `add(value)` appends an element. Use the enhanced for-loop shape:

```java
for (JobApplication application : service.findAll()) {
    // Convert this application and add its response to your result list.
}
```

The loop visits each model once; it does not create a new service or remove entries. Keep storage in the service and reuse the existing mapper. No streams, repository, filters, or database are necessary.

Run `.\mvnw.cmd test`, restart the app, POST two applications, and request GET /applications with the browser or `Invoke-RestMethod`. Expect both records with distinct IDs; list order is not guaranteed. Ask for review when ready. The existing seven tests do not assert your new GET behavior yet; we will verify that and introduce its focused test after reviewing the handler.

## I should be able to...

- [ ] Explain how constructor injection gives the controller the shared service.
- [ ] Explain how the class path and HTTP-method annotation select a handler.
- [ ] Trace JSON through request conversion, service creation, and response mapping.
- [ ] Explain why creation returns 201 and listing returns 200.
- [ ] Distinguish a returned response DTO from a stored model.
- [ ] Implement GET using service.findAll(), the mapper, and a result list; have it reviewed.

These concepts and the GET exercise remain pending demonstration/review. The worked POST handler is verified; the combined POST/GET roadmap item remains incomplete.
