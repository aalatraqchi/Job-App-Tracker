## Files created

| File | What changed |
| --- | --- |
| `common/HttpUrl.java` | Defines a shared `@HttpUrl` validation rule for optional job links. |
| `common/HttpUrlValidator.java` | `isValid()` accepts null/empty links or HTTP(S) URLs with a valid host. Used by both API DTOs and the HTML form. |
| `templates/tracker/delete.html` | Shows the selected application, a cancel link, and an explicit delete button. |
| `templates/tracker/bad-request.html` | Displays a friendly HTML page for invalid application IDs. |
| `application/ApplicationCrudTests.java` | Six HTTP tests cover full CRUD, field/length/URL validation, malformed JSON/dates/statuses, invalid IDs, and allowed boundary values. |

## Files edited

Paths below are under `src/main/java/com/ashraf/jobapptracker`, `src/main/resources`, or their corresponding test packages.

| File | What changed |
| --- | --- |
| `application/ApplicationService.java` | Added `delete(id)`: removes an existing application or throws the existing not-found exception. |
| `application/ApplicationController.java` | Added `deleteById()` with HTTP 204. Enabled validation on POST and PUT. Preserved your PUT method. |
| `application/CreateApplicationRequest.java`, `UpdateApplicationRequest.java` | Added required company/position/date, text-length limits, and job-link validation. Null status still defaults on create and is preserved on update. |
| `common/ApiExceptionHandler.java` | Added JSON 400 handlers for field validation, unreadable JSON/typed values, and invalid IDs. Scoped advice to REST controllers so pages retain HTML errors. |
| `web/ApplicationForm.java` | Replaced the URL pattern with the shared `@HttpUrl` rule. |
| `web/TrackerPageController.java` | Added GET/POST edit routes, GET delete confirmation, POST confirmed deletion, shared form setup, and HTML invalid-ID handling. |
| `templates/tracker/form.html` | Reused the add form for editing: prefilled values, dynamic action/title/button, and a cancel link to details. |
| `templates/tracker/detail.html`, `list.html` | Added edit/delete links and deletion success feedback. |
| `static/css/tracker.css` | Added wrapping action buttons and delete-button styling; retained readable formatting. |
| `application/ApplicationServiceTests.java` | Added deletion verification: only the requested record is removed; missing IDs fail. |
| `web/TrackerPageTests.java` | Added four tests for edit/save, rejected edits without overwriting data, delete confirmation, and HTML error pages. |
| `README.md`, `Project Outline.md`, `AGENTS.md` | Updated supported functionality, progress, and next checkpoint. Full verification: 35 tests pass and packaging succeeds. |

## New content

| Use | Purpose |
| --- | --- |
| `@DeleteMapping("/{id}")` | Route HTTP DELETE requests to the delete method. |
| `@ResponseStatus(HttpStatus.NO_CONTENT)` | Return HTTP 204 with no response body after deletion. |
| `Map.remove(id)` | Remove a stored entry; a null return means the ID was absent. |
| `@NotNull`, `@Size(max = ...)` | Require a value and limit text length, respectively. Existing `@NotBlank` and `@Valid` were covered in Part 8. |
| `@Constraint` + `ConstraintValidator` | Define and implement one custom rule, such as `@HttpUrl`, reusable across forms and DTOs. |
| `MethodArgumentNotValidException` | Read failed request-field checks and return field-specific JSON errors. |
| `HttpMessageNotReadableException` | Return JSON 400 when the body cannot become a request DTO, such as malformed JSON or an unknown enum/date. |
| `MethodArgumentTypeMismatchException` | Handle an invalid URL parameter, such as a nonnumeric application ID. |
| `@RestControllerAdvice(annotations = RestController.class)` | Limit JSON error advice to API controllers. |
| `@RequestParam` | Receive the delete form's `confirmed` value. A GET only shows confirmation; the confirmed POST performs deletion. |
