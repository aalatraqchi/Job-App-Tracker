# Job Application Tracker — Project Outline

## Goal
Build a small Java backend for tracking job applications from applying through interviews to an offer or rejection. Use the project to ease back into Java through short, working milestones, adding one concept at a time.

The finished first version will let an authenticated user create, view, update, filter, and delete their own applications; record interview stages and notes; and keep everything in PostgreSQL between restarts. Interact with the backend using an API client such as Postman or curl. A graphical interface can come later.

## Tools
- **Java 21:** the baseline from the project idea; refresh classes, enums, collections, exceptions, and date/time types as they become useful.
- **Spring Boot and Spring Web:** start the app and expose HTTP endpoints that accept and return JSON.
- **Maven:** manage dependencies and builds; use the generated Maven Wrapper so the project has a consistent build command.
- **Spring Data JPA and PostgreSQL:** map Java objects to database tables and persist applications and interview records.
- **Bean Validation:** check request fields and give useful errors.
- **Spring Security:** authenticate users and enforce ownership once the application features work.
- **JUnit and Spring Boot test support:** verify business rules, API behavior, persistence, and user isolation.
- **Git:** save completed milestones. Keep source and run instructions in `E:\Codex\Job App Tracker`; keep the roadmap and dated progress in this vault.

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
In progress: [[Job Apps Tracker/Learning/Part 2|Part 2]] begins with a company-only JobApplication model. Position exercise complete and reviewed: both fields, constructor assignments, and getters are correct; user reported passing tests. ApplicationStatus enum added and compiled; Part 2 now explains LocalDate and enums. Date/status model exercise complete and reviewed: fields, constructor assignments, and getters are correct. Remaining model fields and DTOs are still pending. The combined model/DTO checklist item stays unchecked until all of it is complete.

- [ ] Move the application model into the Spring project and define create-request and response DTOs.
- [ ] Add a service holding an in-memory collection with generated IDs.
- [ ] Add `POST /applications` and `GET /applications` controllers; keep storage and business rules in the service.
- [ ] Default new applications to `APPLIED` when status is omitted.
- [ ] **Verify:** create two applications, receive HTTP 201 with their IDs, and list both. Restarting clears them at this temporary stage.

### 4. Finish the basic application API
- [ ] Add `GET /applications/{id}`, `PUT /applications/{id}`, and `DELETE /applications/{id}`.
- [ ] Define PUT as replacing the editable fields; keep the ID unchanged. Partial updates can wait.
- [ ] Validate required company, position, and applied date; set sensible text-length limits and validate optional URLs.
- [ ] Add one shared exception handler for consistent JSON errors: HTTP 400 for invalid requests and HTTP 404 for missing IDs.
- [ ] **Verify:** create → retrieve → edit → delete works; blank required fields, unknown statuses, and missing IDs produce the expected errors. Successful deletion returns HTTP 204.

### 5. Add application filters and initial automated tests
- [ ] Support optional `status` and `company` query parameters on the list endpoint.
- [ ] Make company matching case-insensitive and partial; combine filters with AND when both are supplied.
- [ ] Use a predictable list order, such as applied date descending followed by ID descending.
- [ ] Add JUnit service tests for status defaults, editing, deleting, and filter combinations; add controller tests for validation and missing IDs.
- [ ] **Verify:** a filter with no matches returns an empty list; invalid statuses return HTTP 400; all tests pass.

### 6. Replace in-memory storage with PostgreSQL
- [ ] Install/start PostgreSQL if needed and create a local development database and database user.
- [ ] Add Spring Data JPA and the PostgreSQL driver, then map the application to a table and create its repository.
- [ ] Replace collection access in the service with repository operations. Keep the public endpoint behavior consistent.
- [ ] Store database credentials in environment variables or ignored local configuration; document the variable names in the README.
- [ ] For early local development, allow JPA to update the schema. Replace this with versioned migrations in step 10.
- [ ] **Verify:** create an application, restart the backend, and retrieve it again. Existing API tests still pass after adapting their setup.

### 7. Track interview stages
- [ ] Add an `InterviewStage` entity, DTOs, repository, and service linked to an application.
- [ ] Add nested endpoints to create/list stages and update/delete a particular stage.
- [ ] Validate stage names and outcomes; allow an unscheduled stage without a date/time.
- [ ] Require a stage ID to belong to the application named in the URL; deleting an application also deletes its stages.
- [ ] **Verify:** one application can retain multiple stages after restart; stages cannot be edited through another application's URL; deletion leaves no orphan stages.

### 8. Add authentication gradually
- [ ] Add Spring Security and an `AppUser` table with unique usernames and encoded passwords.
- [ ] Begin with two development accounts created through an explicit local setup procedure; no public registration endpoint is required for version one.
- [ ] Use Spring Security's `PasswordEncoder` and HTTP Basic authentication for the local API-client workflow.
- [ ] Require authentication for application/interview endpoints. Keep `/health` free of private information.
- [ ] Document the chosen stateless security configuration and CSRF decision for this API-client-only version. Revisit them before adding a browser UI; use HTTPS before any remote use.
- [ ] **Verify:** correct credentials authenticate, incorrect or missing credentials return HTTP 401, and responses never contain password hashes.

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
- [ ] Walk through: authenticate → create application → add interview → update status → filter → restart → retrieve → delete.
- [ ] **Verify:** the complete workflow passes, failure responses are consistent, and tests never alter personal development data.

### 12. Document and finish version one
- [ ] Write a README with prerequisites, exact chosen versions, database/account setup, environment variables, run/test commands, and example requests.
- [ ] Save an API request collection or equivalent examples covering the main workflow and errors.
- [ ] Review names and responsibilities; keep controllers small and add comments where a decision needs explanation.
- [ ] Mark verified milestones complete here and summarize the work under the appropriate date in [[Changes]].
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
- `src/main/resources/db/migration`: versioned SQL migrations when step 10 begins.
- `src/test/java`: tests organized around the corresponding features.

Request flow: **HTTP request → controller → service → repository → PostgreSQL**. The controller handles HTTP input/output; the service applies business and ownership rules; the repository handles database access. Introduce each layer when its step needs it.

## First-Version Scope
Keep this a local learning backend. Leave a frontend, public registration, JWT/OAuth, password-reset email, job scraping, resume uploads, notifications, analytics, cloud deployment, and microservices for later. Separate company/position tables, pagination, and status-history auditing are optional improvements after the first version is complete.

## Current Progress
- Completed: project outline, environment verification, step 2 Spring Boot starter (4.1.1 with Java 21), and starter learning checkpoint. User implemented /about and its own integration test.
- Verified: Maven Wrapper build/package passed; the user reported both /health and /about HTTP integration tests passing on 10/6/26; live localhost:8080/health returned HTTP 200 and status UP. Existing JDK 21.0.2, Maven 3.9.6, and IntelliJ installations reused. Wrapper downloads Maven 3.9.16. No database or application-tracking endpoints yet.
- In progress: step 3, application model foundations. JobApplication now has company and position, constructor assignments, and getters; user exercise reviewed; compilation and both existing endpoint tests pass. Model behavior is not covered by those endpoint tests. No application storage or application endpoints yet. Step 1 remains an optional refresher.
- Next: practice the read-only Git commands in [[Job Apps Tracker/Learning/Part 3|Part 3]], then resume request/response DTOs in [[Job Apps Tracker/Learning/Part 2|Part 2]] on a feature branch. All six user-supplied model fields and getters are complete and reviewed. ID remains deferred until service-generated identity. The combined model/DTO checklist item remains incomplete.
- This note tracks milestones; [[Changes]] tracks dated work and verification results. Record actual completed changes and any limitations, then name the next step.

## References
- [Spring Quickstart](https://spring.io/quickstart/) — Java and Initializr setup.
- [Spring Initializr](https://start.spring.io/) — generate the starter project.
- [Building an Application with Spring Boot](https://spring.io/guides/gs/spring-boot/) — initial application and build workflow.
- [Spring Security password storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html) — password encoding guidance.


## Learning Approach
- This is a portfolio project and a Java/Spring Boot refresher. Prioritize understanding and small working features over completing the roadmap quickly.
- Explain new concepts in separate numbered notes in the Learning folder (Part 1, Part 2, etc.), using current project examples and explaining the reasons behind the design. Create a new part when a new topic or milestone begins; keep each part concise and organized by concept, with short capability checklists. [[Job Apps Tracker/Learning/Part 1|Part 1]] contains the completed starter lesson. [[Job Apps Tracker/Learning/Part 2|Part 2]] is the current application-model lesson.
- For tasks based on covered concepts, the user attempts the code first; review and offer hints before supplying a complete solution.
- Starter checkpoint completed: capability checklists, request mapping, random test ports, /about endpoint, and separate endpoint tests. User attempts were reviewed; user reported two passing tests. Step 3 is in progress; current checkpoint is the Part 2 model exercise.










## Version Control
- Private repository: [aalatraqchi/Job-App-Tracker](https://github.com/aalatraqchi/Job-App-Tracker).
- Initial baseline 75faa18 contains the current starter, six-field model, two endpoint tests, and learning/progress snapshots. Maven verify passed with two tests and successful packaging before the push.
- Main tracks origin/main. No feature branch or pull request created yet. Future features will use small commits on a branch, testing, and pull-request review before merging.
- [[Job Apps Tracker/Learning/Part 3|Part 3]] covers Git/GitHub. Vault notes remain authoritative; source note snapshots are copied deliberately before commits rather than automatically synchronized.
