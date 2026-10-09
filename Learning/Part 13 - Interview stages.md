## Files created

Java paths below are under `src/main/java/com/ashraf/jobapptracker/`.

| File | What changed |
| --- | --- |
| `interview/InterviewStage.java`, `InterviewOutcome.java` | Added a stage entity belonging to an application, with name, optional scheduled time, outcome, and notes. Added constructor, getters, and `updateDetails()`. |
| `interview/CreateInterviewRequest.java`, `UpdateInterviewRequest.java`, `InterviewResponse.java` | Added validated input records and output DTO; `from()` maps a stage to its response. |
| `interview/InterviewRepository.java`, `InterviewNotFoundException.java` | Added parent-scoped lookup/list queries and a missing-stage exception. |
| `interview/InterviewService.java` | Added `create()`, `findAll()`, `findById()`, `update()`, and `delete()`. Requires the parent application, checks both IDs, defaults new outcomes to PENDING, and preserves an omitted outcome on update. |
| `interview/InterviewController.java` | Added nested POST/GET/PUT/DELETE endpoints under `/applications/{applicationId}/interviews`. |
| `web/InterviewForm.java`, `InterviewPageController.java` | Added validated date/time, UTC-offset, outcome, and notes inputs; add/edit/confirmation routes; validation feedback and redirects. |
| `src/main/resources/templates/tracker/interview-form.html`, `interview-delete.html` | Added a shared add/edit form and a deletion-confirmation page. |
| `src/test/java/com/ashraf/jobapptracker/interview/InterviewControllerTests.java`, `web/InterviewPageTests.java` | Verified CRUD, defaults, input errors, wrong-parent URLs, escaping, confirmation behavior, and application deletion without orphan stages. |

## Files edited

| File | What changed |
| --- | --- |
| `application/JobApplication.java` | Added the stage relationship with removal cascading: deleting an application removes its stages. |
| `common/ApiExceptionHandler.java` | Added missing-stage JSON 404 handling and a numeric interview-ID error. |
| `web/TrackerPageController.java` | Supplies interview stages to the application detail page. |
| `src/main/resources/templates/tracker/detail.html`, `delete.html`, `not-found.html` | Show stage cards/actions and scheduled times; warn about related-stage deletion; support interview 404 pages. |
| `src/main/resources/static/css/tracker.css`, `messages.properties` | Added responsive stage styling and friendly date/outcome form errors. |
| `src/test/java/com/ashraf/jobapptracker/application/PostgresPersistenceTests.java` | Verifies two stages, scheduled/unscheduled times, outcomes, and notes survive a Spring-context restart. |
| `README.md`, `Project Outline.md`, `AGENTS.md` | Recorded step 7 behavior and verification. All 49 tests and JAR packaging pass; desktop/phone pages checked. |

## New content

| Use | Purpose |
| --- | --- |
| `@ManyToOne` / `@JoinColumn(name = "application_id")` | Connect each interview row to its parent application through a database foreign key. |
| `FetchType.LAZY` | Load the related application only when needed rather than whenever a stage is loaded. |
| `@OneToMany(mappedBy = "application", cascade = CascadeType.REMOVE)` | Describe the application's stages using the existing relationship; remove those stages when the application is deleted. |
| `findByIdAndApplication_Id(...)` / `findByApplication_IdOrderByIdAsc(...)` | Let Spring Data derive queries from method names: match both IDs, or list one parent's stages in creation order. |
| `.map(InterviewResponse::from)` | Convert each stage into a response DTO. `InterviewResponse::from` refers to the mapping method to call for each item. |
| `OffsetDateTime` | Store a scheduled instant with an explicit offset. PostgreSQL normalizes it to UTC; `14:30-05:00` and `19:30Z` represent the same instant. |
| `LocalDateTime` + `ZoneOffset` | Combine the HTML form's date/time with its explicit UTC offset before saving. A local time alone does not identify an instant. |
| `@DateTimeFormat(iso = ISO.DATE_TIME)` | Bind the browser's date/time field to the form's Java time value. |
| `@Pattern` | Reject a UTC-offset input that does not match the allowed format before converting it. |
