# Job Application Tracker — Project Outline

## Goal
Build a small Java/Spring Boot application with a responsive Thymeleaf frontend for tracking job applications from applying through interviews to an offer or rejection. Use the project to ease back into Java through short, working milestones, adding one concept at a time.

The finished first version will let an authenticated user create, view, update, filter, and delete their own applications; record interview stages and notes; and keep everything in PostgreSQL between restarts. Use the browser interface for everyday tracking, with JSON endpoints available for API practice and testing. Build each frontend capability alongside its service/API milestone.

## Tools
- **Java 21:** the baseline from the project idea; refresh classes, enums, collections, exceptions, and date/time types as they become useful.
- **Spring Boot and Spring Web:** start the app and expose HTTP endpoints that accept and return JSON.
- **Thymeleaf, HTML, and CSS:** render server-side pages, bind forms, and build a responsive interface; add JavaScript only when a feature needs it.
- **Maven:** manage dependencies and builds; use the generated Maven Wrapper so the project has a consistent build command.
- **Spring Data JPA and PostgreSQL:** map Java objects to database tables and persist applications and interview records.
- **Bean Validation:** check request fields and give useful errors.
- **Spring Security:** authenticate users and enforce ownership once the application features work.
- **JUnit and Spring Boot test support:** verify business rules, API behavior, rendered pages/forms, persistence, and user isolation.
- **Git:** save completed milestones. Keep source and run instructions in `E:\Codex\Job App Tracker`; keep the roadmap and concise learning notes in this vault. Changes.md is retained as history and no longer updated.

Use Spring Initializr to select Maven, Java 21, and a stable Spring Boot release compatible with Java 21. Record the chosen version in the project; let Spring Boot manage its dependency versions. Start with Spring Web and Validation, then add persistence and security at the relevant steps. Avoid preview Java features and snapshot framework versions.

## First-Version Features
- Create an application with company, position, application date, status, and optional job URL and notes.
- List all of the current user's applications or retrieve one by ID.
- Edit details and change application status.
- Filter by status or company, with both filters usable together.
- Record multiple interview stages for an application, including stage name, scheduled time, outcome, and notes.
- Delete an application and its interview records.
- Persist data in PostgreSQL and restrict access to each user's own records.
- Return clear responses for invalid input, missing records, and unauthenticated requests.

## Simple Data Model
Begin with a single application object. Company and position are text fields, so entering an application does not require creating separate company and job records.

| Record | Fields | Relationship |
| --- | --- | --- |
| JobApplication | ID, company, position, applied date, status, optional job URL and notes | Belongs to one user after authentication is added |
| InterviewStage | ID, stage name, optional scheduled time, outcome, optional notes | Belongs to one application; an application may have many stages |
| AppUser | ID, unique username, password hash | Owns many applications |

- Application statuses: `APPLIED`, `INTERVIEW`, `OFFER`, `REJECTED`, and `WITHDRAWN`.
- Interview outcomes: `PENDING`, `PASSED`, `FAILED`, and `CANCELLED`.
- Use `LocalDate` for the application date and an offset-aware time type for scheduled interviews.
- Keep the overall application status separate from individual interview outcomes. Updating an interview does not automatically reject an application or produce an offer; the user sets the overall status explicitly.
- Use request/response DTOs (small objects representing API input/output) so database entities and password fields are not exposed directly.
- Start with explicit constructors and methods rather than Lombok, so the Java refresher remains visible.

## Implementation Order
Complete and verify each step before moving to the next. Treat each numbered step as a separate milestone; short sessions are fine. Check a box only after its verification works.

### 1. Optional refresher: core Java with the application model
- [x] Confirm a JDK 21 installation and an IDE that can run Java. Java 21.0.2 and IntelliJ IDEA 2023.3.3 are already installed; Java compilation/build verified. The remaining refresher exercise is optional given prior Java/Spring Boot experience.
- [ ] Write a small plain-Java exercise with a `JobApplication` class and `ApplicationStatus` enum.
- [ ] Create a few sample applications in a list, print them, update one status, and filter the list by company or status.
- [ ] Review only the concepts this exercise uses: constructors, getters/setters, enums, generics, loops, and `LocalDate`.
- [ ] **Verify:** run the exercise and explain what each class and method does before introducing Spring.

### 2. Create and run the Spring Boot project
- [x] Generate a Maven/Java 21 project with Spring Web and Validation in the source workspace.
- [x] Add a simple `GET /health` endpoint returning a small JSON response.
- [x] Run with `.\mvnw.cmd spring-boot:run`, then request the health endpoint from an API client.
- [x] Initialize Git; ignore build output, IDE-specific files, and local secrets. Working source committed and pushed to private GitHub repository aalatraqchi/Job-App-Tracker. Main tracks origin/main; initial baseline commit 75faa18.
- [x] **Verify:** `.\mvnw.cmd test` passes and the health endpoint returns HTTP 200.

### 3. Create and list applications in memory
Step 3 behavior complete and verified: POST creates applications with generated IDs; GET lists them as response DTOs. User GET implementation reviewed and preserved. Eight tests and JAR packaging pass. Feature merged in PR #2 with user approval; [[Job Apps Tracker/Learning/Part 5 - Create and list applications over HTTP|Part 5]] is the concise endpoint reference.

- [x] Move the application model into the Spring project and define create-request and response DTOs. Six user-supplied model fields and both DTO records complete and reviewed; Service-generated ID is also implemented and verified. DTO feature merged in PR #1 with user confirmation.
- [x] Add a service holding an in-memory collection with generated IDs. Creation/listing implemented; tests verify distinct IDs and retention of identical-input applications. Feature merged in PR #2 with user approval.
- [x] Add `POST /applications` and `GET /applications` controllers; keep storage and business rules in the service. User GET implementation reviewed; both endpoints verified over HTTP.
- [x] Default new applications to `APPLIED` when status is omitted. Default verified by service test; user test verifies that explicit INTERVIEW status is preserved; reviewed and passing.
- [x] **Verify:** create two applications, receive HTTP 201 with their IDs, and list both. HTTP test verifies an empty initial list, distinct created IDs, and both response DTOs. Restarting recreates the temporary storage.

### 4. Finish the basic application API
Complete and verified: API CRUD, required-field/text-length/URL validation, and JSON 400/404 errors. Current breakdown: [[Job Apps Tracker/Learning/Part 10 - Application CRUD and edit-delete pages|Part 10]].

- [x] Add `GET /applications/{id}` and verify successful retrieval and JSON 404 for missing IDs.
- [x] Define update behavior: replace editable details, retain ID, and preserve existing status if omitted. Partial updates can wait.
- [x] Implement and test `UpdateApplicationRequest` and `ApplicationService.update`; preserve record count and ID generation.
- [x] Add `PUT /applications/{id}` returning the updated response DTO and HTTP 200; user implementation reviewed and verified over HTTP.
- [x] Add `DELETE /applications/{id}` returning HTTP 204.
- [x] Validate required company, position, and applied date; set text-length limits and validate optional URLs on the JSON API.
- [x] Add shared missing-ID JSON error handling with HTTP 404.
- [x] Add consistent JSON HTTP 400 errors for invalid requests.
- [x] **Verify:** create → retrieve → edit → delete over HTTP; invalid input, unknown statuses, and missing IDs produce expected errors.

### 4A. Build the Thymeleaf interface alongside the API
Current CRUD interface complete. User confirmed that filters, interviews, login, and persistence-dependent checks below stay with their later steps.
- [x] Add Thymeleaf and separate HTML page routes reusing ApplicationService.
- [x] Build list, summary counts, empty state, add form, detail page, and friendly HTML 404.
- [x] Add shared responsive CSS, labelled inputs, focus styles, page-form validation, and success feedback.
- [x] Verify shared UI/API data and desktop/phone layouts; format CSS for readable editing.
- [x] Add an edit form using the verified update service.
- [x] Add deletion with a confirmation step after delete behavior is implemented.
- [x] Add company/status filter controls, Apply/Clear actions, retained selections, and a no-match state.
- [x] Add interview-stage views/forms: detail cards, add/edit forms, validation, and deletion confirmation; desktop/phone layouts verified.
- [ ] Add login/logout and authenticated navigation in step 8.
- [ ] Verify the complete browser workflow after persistence and ownership are added.

### 5. Add application filters and initial automated tests
- [x] Support optional `status` and `company` query parameters on the list endpoint.
- [x] Make company matching case-insensitive and partial; combine filters with AND when both are supplied.
- [x] Use a predictable list order, such as applied date descending followed by ID descending.
- [x] Add service tests for status defaults and editing, plus HTTP tests for missing IDs.
- [x] Add tests for deletion, filter combinations, and REST validation.
- [x] **Verify:** a filter with no matches returns an empty list; invalid statuses return HTTP 400; all tests pass.

### 6. Replace in-memory storage with PostgreSQL
- [x] Install/start PostgreSQL if needed and create a local development database and database user.
- [x] Add Spring Data JPA and the PostgreSQL driver, then map the application to a table and create its repository.
- [x] Replace collection access in the service with repository operations. Keep the public endpoint behavior consistent.
- [x] Store database credentials in environment variables or ignored local configuration; document the variable names in the README.
- [x] For early local development, allow JPA to update the schema. Replace this with versioned migrations in step 10.
- [x] **Verify:** create an application, restart the backend, and retrieve it again. Existing API tests still pass after adapting their setup.

### 7. Track interview stages
- [x] Add an `InterviewStage` entity, DTOs, repository, and service linked to an application.
- [x] Add nested endpoints to create/list stages and update/delete a particular stage.
- [x] Validate stage names and outcomes; allow an unscheduled stage without a date/time.
- [x] Require a stage ID to belong to the application named in the URL; deleting an application also deletes its stages.
- [x] **Verify:** one application can retain multiple stages after restart; stages cannot be edited through another application's URL; deletion leaves no orphan stages.

### 8. Add authentication gradually
- [ ] Add Spring Security and an `AppUser` table with unique usernames and encoded passwords.
- [ ] Begin with two development accounts created through an explicit local setup procedure; no public registration endpoint is required for version one.
- [ ] Use Spring Security's `PasswordEncoder` and session-based form login/logout for the browser. Decide whether API-client practice also needs HTTP Basic support.
- [ ] Require authentication for application/interview endpoints. Keep `/health` free of private information.
- [ ] Keep CSRF protection for browser form submissions and include tokens in Thymeleaf forms. Document session/API behavior and use HTTPS before any remote use.
- [ ] **Verify:** browser login/logout works, protected pages require login, invalid credentials fail, and responses never expose password hashes. Verify JSON API authentication/error behavior separately.

### 9. Enforce ownership for every operation
- [ ] Link each application to its owner. If practice data already exists, explicitly assign it to a development account before requiring an owner.
- [ ] Determine the owner from the authenticated principal; never accept an owner ID from the request body.
- [ ] Scope list, filter, retrieve, update, and delete operations to the current user. Apply the same restriction to interview stages through their parent application.
- [ ] Return HTTP 404 when a requested record does not belong to the current user.
- [ ] **Verify:** account A creates an application and interview; account B cannot list, retrieve, edit, or delete either record, including by guessing IDs.

### 10. Make database setup repeatable
- [ ] Add Flyway versioned migrations for users, applications, interviews, foreign keys, and required constraints.
- [ ] Turn off automatic schema updates and have JPA validate the migrated schema.
- [ ] Plan the transition for the existing local database: export any wanted records, then either recreate the disposable database from migrations or deliberately baseline its matching schema.
- [ ] Keep account seeding explicit and local; keep credentials out of migrations and Git.
- [ ] **Verify:** a fresh empty database can be initialized successfully and the backend restarts without losing its saved records.

### 11. Verify the complete workflow
- [ ] Add focused PostgreSQL integration tests against a separate disposable test database for persistence, filtering, relationships, and deletion.
- [ ] Add automated authentication and two-user isolation tests for applications and interview endpoints.
- [ ] Run `.\mvnw.cmd verify`; make any test-database setup requirements explicit.
- [ ] Walk through both browser and API flows: authenticate → create application → add interview → update status → filter → restart → retrieve → delete.
- [ ] **Verify:** the complete workflow passes, failure responses are consistent, and tests never alter personal development data.

### 12. Document and finish version one
- [ ] Write a README with prerequisites, exact chosen versions, database/account setup, environment variables, run/test commands, and example requests.
- [ ] Save an API request collection or equivalent examples covering the main workflow and errors.
- [ ] Review names and responsibilities; keep controllers small and add comments where a decision needs explanation.
- [ ] Mark verified milestones complete here and keep the relevant learning breakdown and README current. Changes.md remains historical only.
- [ ] **Verify:** follow the README from a fresh checkout and database, then confirm every first-version feature works.

## Endpoint Plan
Paths use the screenshot's simple `/applications` convention. After steps 8–9, every application and interview endpoint is restricted to the authenticated user's records.

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/applications` | Create an application |
| GET | `/applications` | List applications; optional `status` and `company` filters |
| GET | `/applications/{id}` | Retrieve an application |
| PUT | `/applications/{id}` | Replace editable details, including status |
| DELETE | `/applications/{id}` | Delete application and its interviews |
| POST | `/applications/{id}/interviews` | Add an interview stage |
| GET | `/applications/{id}/interviews` | List interview stages |
| PUT | `/applications/{id}/interviews/{interviewId}` | Replace editable interview details |
| DELETE | `/applications/{id}/interviews/{interviewId}` | Delete an interview stage |

Example filters: `/applications?status=INTERVIEW`, `/applications?company=Google`, and `/applications?status=INTERVIEW&company=Google`.

## Code Organization
- `application`: application controller, service, repository, entity, status enum, and DTOs.
- `interview`: interview controller, service, repository, entity, outcome enum, and DTOs.
- `user` and `security`: user storage, authentication, and security configuration.
- `common`: shared error responses and exception handling.
- `web`: HTML page controllers and form models.
- `src/main/resources/templates/tracker`: Thymeleaf pages and shared fragments.
- `src/main/resources/static/css`: responsive page styling.
- `src/main/resources/db/migration`: versioned SQL migrations when step 10 begins.
- `src/test/java`: tests organized around the corresponding features.

Current flow: **browser/API request → page/REST controller → shared service → repository → PostgreSQL**. Page controllers provide model data to Thymeleaf; REST controllers return JSON. The controller handles HTTP input/output; the service applies business and ownership rules; the repository handles database access. Introduce each layer when its step needs it.

## First-Version Scope
Keep this a local learning application with a basic Thymeleaf frontend. Leave public registration, JWT/OAuth, password-reset email, job scraping, resume uploads, notifications, analytics, cloud deployment, and microservices for later. Separate company/position tables, pagination, and status-history auditing are optional improvements after the first version is complete.

## Current Progress
- [x] Environment, Spring Boot starter, `/health`, `/about`, and starter exercises.
- [x] Application model, DTOs, generated IDs, and in-memory create/list API.
- [x] Application API CRUD, validation, filters, and corresponding responsive Thymeleaf pages.
- [x] PostgreSQL persistence, including application/database restart verification.
- [x] Step 7 interview entity, scoped nested API CRUD, validation, scheduled/unscheduled stages, and application-deletion cascade.
- [x] Step 4A interview detail cards, add/edit forms, and deletion confirmation.
- Verification: **49 tests pass and JAR packaging succeeds**. Real PostgreSQL tests verify interview CRUD, invalid input, wrong-parent URLs, cascade deletion without orphan stages, and multiple stages surviving a Spring-context restart. Browser creation/edit/save and desktop/390px layouts checked without horizontal overflow; automated HTTP tests verify confirmed deletion. Temporary preview records were removed.
- Current note: [[Job Apps Tracker/Learning/Part 13 - Interview stages|Part 13]], using the files/changes/new-content format. Step 7 is implemented on `codex/interview-stages`; next is joint feature review and explicit merge approval. Step 8 authentication has not begun.
- Earlier feature PRs #3, #4, and #5 were merged with explicit user approval (5d9dd42, 05c25bf, and d31898c). Step 7 is not merged.
- Part 8 and Part 9 concept boxes remain user self-assessment; implementation exercises were reviewed separately.
- PostgreSQL is running through Docker Compose on localhost:5433. Start Docker Desktop and run `.\scripts\setup-database.ps1` when needed, then run the app from the project root. Assistant preview on 8081 is stopped; other application processes were left alone.
- Keep this outline current. Changes.md is historical only; do not add further entries.

## References
- [Spring Quickstart](https://spring.io/quickstart/) — Java and Initializr setup.
- [Spring Initializr](https://start.spring.io/) — generate the starter project.
- [Building an Application with Spring Boot](https://spring.io/guides/gs/spring-boot/) — initial application and build workflow.
- [Spring Security password storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html) — password encoding guidance.


## Learning Approach
- This is a portfolio project and a Java/Spring Boot refresher. Prioritize understanding and small working features over completing the roadmap quickly.
- Explain new concepts in separate numbered notes in the Learning folder (Part 1, Part 2, etc.), using current project examples and explaining the reasons behind the design. Create a new descriptive Part note when a new topic or milestone begins. Future notes contain only files created/edited, changes per file, and a Use → Purpose table for concepts not already covered. No exercises or capability checklists unless requested. [[Job Apps Tracker/Learning/Part 1 - Project structure and running the app|Part 1]] contains the completed starter lesson. [[Job Apps Tracker/Learning/Part 2 - Modeling a job application|Part 2]] covers modeling; [[Job Apps Tracker/Learning/Part 13 - Interview stages|Part 13]] is the current interview-stage breakdown.
- Implement agreed increments directly with the brief breakdown format. When the user requests an exercise or submits an attempt, review it and offer hints before replacing code.
- Starter checkpoint completed: capability checklists, request mapping, random test ports, /about endpoint, and separate endpoint tests. User attempts were reviewed; user reported two passing tests. Step 3 is complete; Part 4 explicit-status test and Part 5 GET exercise are reviewed. Part 6 lookup test and Part 9 service update exercise are also complete and reviewed.

## Version Control
- Private repository: [aalatraqchi/Job-App-Tracker](https://github.com/aalatraqchi/Job-App-Tracker).
- Main tracks origin/main; DTO PR #1 and create/list PR #2 were merged with explicit approval (0d3628c and 51830be).
- [PR #3 — Complete application CRUD and Thymeleaf tracker](https://github.com/aalatraqchi/Job-App-Tracker/pull/3) merged with explicit approval (5d9dd42). Current branch: `codex/interview-stages` for step 7. [PR #4 — Filter applications by company and status](https://github.com/aalatraqchi/Job-App-Tracker/pull/4) merged with explicit approval (05c25bf). [PR #5 — Persist job applications in PostgreSQL](https://github.com/aalatraqchi/Job-App-Tracker/pull/5) merged with explicit user approval (d31898c). All 42 tests and packaging passed before merging PR #5. Step 7 has 49 passing tests and awaits a separate review/merge approval.
- Assistant handles staging, commits, pushes, and PRs, reporting each action. Joint review and explicit user confirmation are required before each merge. No new merge approved.
- Vault notes are authoritative; source snapshots are copied deliberately. Changes.md is no longer maintained.

<details>
<summary>Earlier checkpoint history (superseded by Current Progress)</summary>

### Earlier version-control checkpoints
- Private repository: [aalatraqchi/Job-App-Tracker](https://github.com/aalatraqchi/Job-App-Tracker).
- Initial baseline 75faa18 contains the current starter, six-field model, two endpoint tests, and learning/progress snapshots. Maven verify passed with two tests and successful packaging before the push.
- Main tracks origin/main and includes merged DTO PR #1 (merge commit 0d3628c). New learning work is on codex/in-memory-applications. Future feature merges still require explicit confirmation.
- [[Job Apps Tracker/Learning/Part 3 - Git and GitHub workflow|Part 3]] covers Git/GitHub. Vault notes remain authoritative; source note snapshots are copied deliberately before commits rather than automatically synchronized.

- Git operations are delegated to the assistant, with staged/committed/pushed updates. Finished features require joint review and explicit user merge confirmation. Git practice exercises do not block Java/Spring learning.

### Create/list feature review
- [PR #2 — create and list applications in memory](https://github.com/aalatraqchi/Job-App-Tracker/pull/2) is open from codex/in-memory-applications into main. Assistant review found no issues within the current milestone scope.
- Maven verify passed with eight tests and successful packaging. Required-field validation and persistence remain future work.
- Awaiting user review and explicit merge confirmation before step 4. Nothing merged.

### Step 4 checkpoint
- PR #2 merged with explicit user approval on 10/8/26 (51830be); local main and origin/main synced. Current branch: codex/application-by-id.
- Current lesson: [[Job Apps Tracker/Learning/Part 6 - Retrieve an application by ID|Part 6]]. GET-by-ID worked example complete; missing-ID HTTP test exercise pending. Eleven tests pass. Future merges still require explicit confirmation.

Missing-ID test review: user's HTTP 404/content-type/message assertions are correct; 12 tests pass. Pending small cleanup: remove unused POST setup and rename test to describe 404, then review. No further step 4 implementation yet.

### Current learning checkpoint
- Part 6 missing-ID HTTP exercise complete and reviewed after cleanup; 12 tests pass. GET-by-ID and 404/message behavior verified. Update/delete and validation still pending.
- Paused implementation for requested [[Job Apps Tracker/Learning/Part 7 - Frontend overview|frontend overview]]. A polished responsive UI is desired; HTML/CSS/JavaScript served by Spring Boot is the suggested simple starting point, with Thymeleaf/separate frontend alternatives explained. Stack and start timing not decided; no frontend implementation or environment changes performed.

### Thymeleaf interface milestone — 10/8/26
- User chose Thymeleaf and requested a simple modern responsive interface while pausing backend development.
- [x] Add Thymeleaf and separate HTML page routes reusing ApplicationService.
- [x] Application list, summary counts, empty state, add form, detail page, and HTML 404.
- [x] Shared responsive styling and basic page-form validation/error messages.
- [x] Verify HTTP behavior and Chrome desktop/phone layouts; all 18 tests and packaging pass.
- [ ] Review Part 8 understanding checks with the user.
- [ ] Joint feature review and explicit merge approval.
- Current lesson: [[Job Apps Tracker/Learning/Part 8 - Thymeleaf pages|Part 8 — Thymeleaf pages]]. Source branch: codex/thymeleaf-ui, including the unmerged lookup milestone. REST endpoints remain available; storage remains in memory. No update/delete, REST validation, persistence, or authentication advancement.

Local preview update: the server is stopped (port 8080 free). CSS formatting improved for learning/readability; implementation progress is unchanged.

### Next checkpoint — application updates
- User requested resuming implementation. Part 8 boxes are self-checked; current lesson is [[Job Apps Tracker/Learning/Part 9 - Updating an application|Part 9]].
- Exercise pending: UpdateApplicationRequest and ApplicationService.update. Controller/edit page and update verification follow review. Step 4 update checkbox remains incomplete.
- Branch codex/update-applications includes prior unmerged UI/lookup work. No merge authorized.

Update exercise initial review: DTO correct; service needs omitted-status preservation and returning the replacement. User corrections and focused verification pending; no update endpoint yet.

Update second review: null-status preservation correct; returned-object correction still pending because Map.replace returns the previous object. No implementation advancement.

Update service checkpoint complete: user's DTO/service reviewed and preserved; three focused tests and full 21-test verification pass. PUT endpoint and edit page remain pending; step 4 is still incomplete. No merge authorized.

</details>

Learning format update: descriptive filenames are the displayed titles; redundant top headings removed. Existing lesson bodies and checkmarks preserved. Future lessons use the brief file/change/new-concept breakdown. Changes.md remains untouched.
