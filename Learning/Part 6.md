# Part 6 — Retrieve an application by ID

Step 4 starts here. [[Job Apps Tracker/Learning/Part 5|Create/list]] is merged in [PR #2](https://github.com/aalatraqchi/Job-App-Tracker/pull/2). Update, delete, and validation follow in later increments.

## What to use and when

| Use | Purpose |
| --- | --- |
| `@GetMapping("/{id}")` | Handle URLs such as `/applications/3`. The controller already sets `/applications`. |
| `@PathVariable("id") long id` | Read the ID from the URL into a method parameter. Use this for a URL value; use `@RequestBody` for a JSON body. |
| `applications.get(id)` | Find the model stored under this ID. Returns null when absent. |
| `throw new ApplicationNotFoundException(id)` | Stop the normal operation when no application matches. |
| `@RestControllerAdvice` | Put shared HTTP error handling in one class. |
| `@ExceptionHandler(ApplicationNotFoundException.class)` | Return HTTP 404 and a message when this exception occurs. |

## Actual endpoint

```java
@GetMapping("/{id}")
public ApplicationResponse getById(@PathVariable("id") long id) {
    return toResponse(service.findById(id));
}
```

Read it as: take the URL ID → find that application → return its response DTO.

`ApplicationService.findById()` checks the map. Found: return the model. Missing: throw ApplicationNotFoundException. `common/ApiExceptionHandler.java` turns that exception into **404** with JSON such as:

```json
{"message":"Application 999 was not found"}
```

This lets the service report a missing record without handling HTTP itself. The same handler can later serve update/delete operations. It currently handles missing applications only; validation errors come later.

## Try it

Run the app as before. POST an application, then use its returned ID:

```powershell
Invoke-RestMethod 'http://127.0.0.1:8080/applications/1'
```

Use the actual returned ID, not necessarily 1. A missing ID returns 404; PowerShell reports that as an error.

## Your exercise — test the 404 response

In `ApplicationControllerTests.java`, use `getByIdReturnsTheCreatedResponse()` as your pattern:

1. Add a new `@Test` method.
2. Send GET to `/applications/-1`. Our generated IDs are positive, so this is absent.
3. Assert status **404**.
4. Parse the JSON with the existing JsonMapper pattern and assert `message` equals `Application -1 was not found`.
5. Run `.\mvnw.cmd test` and ask for review. Expect **12 tests** after adding yours.

Do not use `postApplication()` or create a record for this test. The point is to request something absent. Keep your test independent of test execution order.

## I should be able to...

- [ ] Use `@PathVariable` to read a URL ID.
- [ ] Distinguish successful lookup (200) from missing record (404).
- [ ] Explain why the service throws an exception and the shared handler chooses the HTTP response.
- [ ] Add the missing-ID HTTP test and have it reviewed.

Verified: **11 tests pass**, including successful GET-by-ID over HTTP and service-level missing-ID behavior. Your pending HTTP test will verify the 404 status and error JSON together. These new checklist items remain pending; no update/delete or validation implemented yet.
