# Job Application Tracker — Project Outline

## Goal
Build a small Java/Spring Boot application with a responsive Thymeleaf frontend for tracking job applications from applying through interviews to an offer or rejection. Use the project to ease back into Java through short, working milestones, adding one concept at a time.

The current release target is personal use on Ashraf's own computer, without accounts or a login. It will create, view, update, filter, and delete applications; record interview stages and notes; and keep everything in PostgreSQL between restarts. Use the browser interface for everyday tracking, with JSON endpoints available for API practice and testing. The future multi-account version will add authentication and ownership through steps 8–9; those steps remain below, deferred rather than removed or marked complete.

## Tools
- **Java 21:** the baseline from the project idea; refresh classes, enums, collections, exceptions, and date/time types as they become useful.
- **Spring Boot and Spring Web:** start the app and expose HTTP endpoints that accept and return JSON.
- **Thymeleaf, HTML, and CSS:** render server-side pages, bind forms, and build a responsive interface; add JavaScript only when a feature needs it.
- **Maven:** manage dependencies and builds; use the generated Maven Wrapper so the project has a consistent build command.
- **Spring Data JPA and PostgreSQL:** map Java objects to database tables and persist applications and interview records.
- **Bean Validation:** check request fields and give useful errors.
- **Spring Security:** browser request protection for the personal release; authentication and ownership are deferred to the future multi-account release.
- **JUnit and Spring Boot test support:** verify business rules, API behavior, rendered pages/forms, persistence, and user isolation.
- **Git:** save completed milestones. Keep source and run instructions in `E:\Codex\Job App Tracker`; keep the roadmap and concise learning notes in this vault. Changes.md is retained as history and no longer updated.

Use Spring Initializr to select Maven, Java 21, and a stable Spring Boot release compatible with Java 21. Record the chosen version in the project; let Spring Boot manage its dependency versions. Start with Spring Web and Validation, then add persistence and security at the relevant steps. Avoid preview Java features and snapshot framework versions.

## Tracking Features and Future Account Features
- Create an application with company, position, application date, status, and optional job URL and notes.
- List all personal applications or retrieve one by ID; scope to the current user when future ownership is added.
- Edit details and change application status.
- Filter by status or company, with both filters usable together.
- Record multiple interview stages for an application, including stage name, scheduled time, outcome, and notes.
- Delete an application and its interview records.
- Persist data in PostgreSQL. Restrict records to individual owners in the future multi-account version.
- Return clear responses for invalid input and missing records; add unauthenticated-request handling with future authentication.

## Simple Data Model
Begin with a single application object. Company and position are text fields, so entering an application does not require creating separate company and job records.

| Record | Fields | Relationship |
| --- | --- | --- |
| JobApplication | ID, company, position, applied date, status, optional job URL and notes | Belongs to one user after authentication is added |
| InterviewStage | ID, stage name, optional scheduled time, outcome, optional notes | Belongs to one application; an application may have many stages |
| AppUser (deferred) | ID, unique username, password hash | Owns many applications in the future multi-account version |

- Application statuses: `APPLIED`, `INTERVIEW`, `OFFER`, `REJECTED`, and `WITHDRAWN`.
- Interview outcomes: `PENDING`, `PASSED`, `FAILED`, and `CANCELLED`.
- Use `LocalDate` for the application date and an offset-aware time type for scheduled interviews.
- Keep the overall application status separate from individual interview outcomes. Updating an interview does not automatically reject an application or produce an offer; the user sets the overall status explicitly.
- Use request/response DTOs (small objects representing API input/output) so database entities and password fields are not exposed directly.
- Start with explicit constructors and methods rather than Lombok, so the Java refresher remains visible.

## Personal Local Release Path — agreed 10/8/26

Use one personal dataset without usernames, passwords, login/logout, user rows, or owner IDs. Keep steps 8–9 and their related UI/tests/account migrations as future work; unchecked deferred boxes are not blockers for this release. PR #6 and PR #7 were subsequently merged with explicit approval (19ecfed, a4e822e); each future feature merge still needs fresh approval.

| Roadmap area | Personal-release decision |
| --- | --- |
| Steps 1–7 | Tracking features through step 7 are implemented and merged with explicit approval. The optional step 1 refresher does not block use. |
| Step 4A login/navigation and step 8 accounts | Defer accounts, password handling, authentication, login/logout, and authentication tests. Retain browser form-request protection as a small account-free finish task. |
| Step 9 ownership | Defer owner relationships, principal-based queries, and two-user isolation tests. Interview-to-application scoping remains implemented and required. |
| Step 10 migrations | Defer Flyway for the initial personal release. Continue the current automatic schema updates; back up wanted data before later schema changes. Add user/owner migrations when those features are implemented. |
| Steps 11–12 | Keep final local workflow checks, run/stop/test instructions, backup/restore, and concise documentation. Account-related parts stay deferred. |

### Personal-use finish checklist
- [x] Bind the app and database to this computer: `server.address=127.0.0.1` and Docker's `127.0.0.1:5433` mapping are configured.
- [x] Jointly review step 7 and merge PR #6 after explicit approval; main pushed at 19ecfed and GitHub confirms the merge.
- [x] Add/test account-free CSRF protection: browser forms include tokens automatically; API writes use GET /csrf plus its session cookie/header. Missing, invalid, and wrong-session tokens are rejected; 54 tests and packaging pass. PR #7 merged with explicit approval (a4e822e).
- [x] Implement/document and verify PostgreSQL backup/restore: custom archives plus checksums, separate restore databases, overwrite/corruption rejection, real-data comparison, and fixture field/relationship/ID-sequence checks. PowerShell 7 and Windows PowerShell 5.1 pass; packaged app reads a restored clone. PR #8 merged with explicit approval (3eeb553). A persistent Docker volume is not a separate backup.
- [x] Verify the personal browser/API workflow: create → add/edit interview → update status → combine filters → restart app and database → retrieve unchanged fields → confirmed deletion of disposable records. API CRUD, 400/403/404 errors, wrong-parent rejection, and no orphan interviews also verified in an isolated release database.
- [x] Finalize daily start/stop and JAR/IntelliJ instructions; verify the packaged app from a clean exported project root with fresh local credentials/database. Clean verify passes all 54 tests; final documentation PR #9 merged with explicit approval (bd10cba).

The personal-use checklist is now verified. The application is ready for local tracking; the final documentation PR #9 is merged with explicit approval. Future account/migration checklists below remain available and unchecked until implemented. Local scope means use from this computer; revisit accounts and access controls before offering access to other people or machines.

## Implementation Order
Follow the personal local release path above for the current target. The numbered roadmap preserves the fuller future version; explicitly deferred steps may be skipped for personal use. Treat each implemented step as a separate milestone and check a box only after verification works.

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
- [ ] Add login/logout and authenticated navigation in step 8. **Deferred for personal use.**
- [ ] Verify the complete browser workflow after persistence and ownership are added. **Future ownership version; verify the account-free workflow in the personal checklist now.**

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
**Deferred for the personal release.** Preserve all account work below for later. Browser form-request protection is tracked separately in the personal finish checklist and does not require login.

- [ ] Add Spring Security and an `AppUser` table with unique usernames and encoded passwords.
- [ ] Begin with two development accounts created through an explicit local setup procedure; no public registration endpoint is required for version one.
- [ ] Use Spring Security's `PasswordEncoder` and session-based form login/logout for the browser. Decide whether API-client practice also needs HTTP Basic support.
- [ ] Require authentication for application/interview endpoints. Keep `/health` free of private information.
- [ ] Keep CSRF protection for browser form submissions and include tokens in Thymeleaf forms. Document session/API behavior and use HTTPS before any remote use.
- [ ] **Verify:** browser login/logout works, protected pages require login, invalid credentials fail, and responses never expose password hashes. Verify JSON API authentication/error behavior separately.

### 9. Enforce ownership for every operation
**Deferred for the personal release.** Keep the current single dataset; implement these checks when accounts are added.

- [ ] Link each application to its owner. If practice data already exists, explicitly assign it to a development account before requiring an owner.
- [ ] Determine the owner from the authenticated principal; never accept an owner ID from the request body.
- [ ] Scope list, filter, retrieve, update, and delete operations to the current user. Apply the same restriction to interview stages through their parent application.
- [ ] Return HTTP 404 when a requested record does not belong to the current user.
- [ ] **Verify:** account A creates an application and interview; account B cannot list, retrieve, edit, or delete either record, including by guessing IDs.

### 10. Make database setup repeatable
**Deferred for the initial personal release.** Flyway is still useful for later schema changes but is not required to start personal tracking. Preserve this checklist; user-table/account-seeding work depends on deferred steps 8–9.

- [ ] Add Flyway versioned migrations for users, applications, interviews, foreign keys, and required constraints.
- [ ] Turn off automatic schema updates and have JPA validate the migrated schema.
- [ ] Plan the transition for the existing local database: export any wanted records, then either recreate the disposable database from migrations or deliberately baseline its matching schema.
- [ ] Keep account seeding explicit and local; keep credentials out of migrations and Git.
- [ ] **Verify:** a fresh empty database can be initialized successfully and the backend restarts without losing its saved records.

### 11. Verify the complete workflow
For personal use, run the account-free workflow in the personal checklist. Authentication and two-user checks below remain deferred until steps 8–9.

- [x] Add focused PostgreSQL integration tests against a separate disposable test database for persistence, filtering, relationships, and deletion.
- [ ] Add automated authentication and two-user isolation tests for applications and interview endpoints.
- [x] Run `.\mvnw.cmd clean verify` from a clean source export: 54 tests and packaging pass. Docker/Testcontainers requirements are documented.
- [x] Walk through the personal browser/API flow: create application → add/edit interview → update status → filter → restart app/database → retrieve → confirmed deletion. No authentication is needed for this release.
- [ ] Repeat the flow with authentication when deferred steps 8–9 are implemented.
- [x] **Verify personal workflow:** all steps pass, expected JSON/HTML failures are consistent, and original development data is unchanged.
- [ ] **Verify future account workflow:** authentication and two-user isolation, after steps 8–9.

### 12. Document and finish version one
Apply the run/test/documentation tasks to the personal release now. Account setup and the full multi-account first-version verification remain future work.

- [x] Write the personal README with prerequisites, chosen versions, database setup, environment variables, start/stop/build/test commands, and example requests.
- [ ] Add account-setup instructions when deferred authentication is implemented.
- [x] Save `docs/API Examples.md` covering token/cookie setup, application/interview CRUD, filters, errors, and guarded demo deletion; commands verified against the isolated app.
- [x] Review names/responsibilities: controllers handle HTTP and mapping; shared services handle tracking rules; repositories handle storage. No release-blocking issues found within personal scope.
- [x] Mark verified milestones complete here and keep Part 16 and the README current. Changes.md remains historical only.
- [x] **Verify personal release:** clean Git export, fresh isolated database/setup credentials, 54 tests, packaged app, documented API examples, browser flow, both restarts, and cleanup.
- [ ] **Verify future account release:** repeat from a fresh checkout/database after deferred accounts, ownership, and migrations are implemented.

## Personal UI Improvements — 10/9/26

- [x] Add a labeled Theme selector to the shared header on every tracker page.
- [x] Default to Dark and provide Light, Ocean, Forest, and Sunset palettes.
- [x] Save the choice in browser storage for the same address/port, with a safe Dark fallback.
- [x] Theme cards, forms, badges, notices, buttons, borders, and focus outlines.
- [x] Verify switching, reload/navigation persistence, keyboard selection, shared detail/interview/confirmation/error pages, and 390px/320px layouts without horizontal overflow. All 54 tests and JAR packaging pass; browser checks did not modify saved records.
- [x] Jointly reviewed and merged PR #10 with explicit approval on 10/9/26; main pushed at 200d4bf and GitHub confirms the merge.

## Easy Daily Use — agreed 10/9/26

- [x] Stop the tracker app and project database before beginning this milestone.
- [x] Add double-click Start/Stop command wrappers and create desktop shortcuts.
- [x] Start Docker Desktop when needed, prepare the database, detect JDK 21, build changed source cleanly, launch the app in the background, and open the website when ready.
- [x] Reuse an existing managed app; reject simultaneous operations, occupied ports, and mismatched process identity.
- [x] Stop the managed app before stopping the database; preserve the named volume and saved records.
- [x] Document daily use, logs, IntelliJ/manual alternatives, and the need to finish active saves before stopping.
- [x] Verify lifecycle safeguards, changed/deleted resource rebuilds, exact Start/Stop wrappers, repeatable desktop shortcuts, Chrome rendering, and unchanged records. All 54 Java tests pass. App/database stopped and ports 8080/8081/5433 closed. Docker cold-launch path is implemented but not exercised; verification used the existing running engine.
- [x] Joint review and explicit user merge approval; merged into main and pushed at 635ff41 on 10/9/26.

## Endpoint Plan
Paths use the screenshot's simple `/applications` convention. After steps 8–9, every application and interview endpoint is restricted to the authenticated user's records.

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/csrf` | Obtain an API write token; retain its session cookie and send the returned header |
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
- `security`: account-free request protection now; `user` storage and authentication are deferred.
- `common`: shared error responses and exception handling.
- `web`: HTML page controllers and form models.
- `src/main/resources/templates/tracker`: Thymeleaf pages and shared fragments.
- `src/main/resources/static/css`: responsive page styling and theme palettes.
- `src/main/resources/static/js`: browser theme selection and saved preferences.
- `src/main/resources/db/migration`: versioned SQL migrations when step 10 begins.
- `src/test/java`: tests organized around the corresponding features.

Current flow: **browser/API request → page/REST controller → shared service → repository → PostgreSQL**. Page controllers provide model data to Thymeleaf; REST controllers return JSON. The controller handles HTTP input/output; the service applies business and ownership rules; the repository handles database access. Introduce each layer when its step needs it.

## First-Version Scope
Keep this a local learning application with a basic Thymeleaf frontend. Leave public registration, JWT/OAuth, password-reset email, job scraping, resume uploads, notifications, analytics, cloud deployment, and microservices for later. Separate company/position tables, pagination, and status-history auditing are optional improvements after the first version is complete.

## Current Progress
- Current release target: personal localhost use without accounts. Steps 8–9 and account-dependent UI/tests are deferred; step 10 migrations may wait. Follow the personal finish checklist above before treating the release as complete.
- [x] Environment, Spring Boot starter, `/health`, `/about`, and starter exercises.
- [x] Application model, DTOs, generated IDs, and in-memory create/list API.
- [x] Application API CRUD, validation, filters, and corresponding responsive Thymeleaf pages.
- [x] PostgreSQL persistence, including application/database restart verification.
- [x] Step 7 interview entity, scoped nested API CRUD, validation, scheduled/unscheduled stages, and application-deletion cascade.
- [x] Step 4A interview detail cards, add/edit forms, and deletion confirmation.
- Verification: **54 tests pass and JAR packaging succeeds**. Existing PostgreSQL, CRUD, validation, filter, interview-scoping/cascade, and restart tests remain passing. Five new real-HTTP tests cover account-free reads, all write routes rejecting missing/invalid tokens, unchanged records after rejection, session binding, API token use, and hidden tokens in every form. Chrome create/edit/add-interview submissions work; the friendly 403 page fits desktop and 390px layouts without overflow. Temporary preview records removed.
- [PR #8 — Add verified PostgreSQL backup and separate-database restore](https://github.com/aalatraqchi/Job-App-Tracker/pull/8) merged with explicit approval; main pushed at 3eeb553 and GitHub confirms the merge.
- [PR #9 — Verify personal release and document daily use](https://github.com/aalatraqchi/Job-App-Tracker/pull/9) merged with explicit approval on 10/9/26; main pushed at bd10cba and GitHub confirms the merge.
- [PR #10 — Add Dark, Light, Ocean, Forest, and Sunset themes](https://github.com/aalatraqchi/Job-App-Tracker/pull/10) merged with explicit approval on 10/9/26; main pushed at 200d4bf and GitHub confirms the merge. All 54 tests and Chrome theme checks passed before merging.
- [PR #11 — Add desktop Start and Stop shortcuts for daily use](https://github.com/aalatraqchi/Job-App-Tracker/pull/11) merged into main and pushed with explicit user approval on 10/9/26 (635ff41). Assistant review found no issues within scope; 54 Java tests, launcher lifecycle checks, exact command wrappers, and Chrome rendering passed. Chrome disconnected during merge verification, so the GitHub page was not rechecked; the remote main push succeeded.
- Current note: [[Job Apps Tracker/Learning/Part 18 - Easy startup and shutdown|Part 18]], using the brief files/changes/new-content format. Branch `main`; the personal local-use app, page themes, and easy daily launch/stop workflow are complete and ready to use. Future accounts/ownership/migrations remain deferred. Automatic Docker Desktop cold launch is implemented but remains untested; launch Docker Desktop manually if needed.
- Earlier feature PRs #3, #4, and #5 were merged with explicit user approval (5d9dd42, 05c25bf, d31898c).
- [PR #6 — Track interview stages for each application](https://github.com/aalatraqchi/Job-App-Tracker/pull/6) merged with explicit approval; local and remote main updated at 19ecfed. [PR #7 — Protect local browser and API writes without accounts](https://github.com/aalatraqchi/Job-App-Tracker/pull/7) merged with explicit approval; main pushed at a4e822e and GitHub confirms the merge.
- Backup verification: real archive restored matching records; fixture preserved Unicode, date/time, nulls, relationships, and identity sequences; bad checksums, invalid archives, existing destinations, and the working database name rejected. Both PowerShell versions pass. Packaged app validates the restored schema and serves health, tracker HTML, and API data. Original data unchanged; all disposable databases removed; real backups retained in ignored `backups/`. Java source unchanged in this milestone; prior 54-test build applies.
- Final release checks: clean exported source passes 54 tests and packages successfully; setup works with fresh credentials and an isolated Docker project/volume. Browser/API creation, interview editing, status, combined filters, both-process restart persistence, confirmed deletion, error responses, and orphan cleanup pass. Temporary app/container/volume removed; development data fingerprint unchanged. Tested credential-free JAR retained in the main workspace target folder.
- Part 8 and Part 9 concept boxes remain user self-assessment; implementation exercises were reviewed separately.
- App and project PostgreSQL are stopped after launcher verification; ports 8080/8081/5433 are closed. For daily use, double-click the desktop Start Job Application Tracker shortcut, then Stop Job Application Tracker when finished. Docker Desktop remains available for other projects; records are preserved.
- Keep this outline current. Changes.md is historical only; do not add further entries.

## References
- [Spring Quickstart](https://spring.io/quickstart/) — Java and Initializr setup.
- [Spring Initializr](https://start.spring.io/) — generate the starter project.
- [Building an Application with Spring Boot](https://spring.io/guides/gs/spring-boot/) — initial application and build workflow.
- [Spring Security password storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html) — password encoding guidance.


## Learning Approach
- This is a portfolio project and a Java/Spring Boot refresher. Prioritize understanding and small working features over completing the roadmap quickly.
- Explain new concepts in separate numbered notes in the Learning folder (Part 1, Part 2, etc.), using current project examples and explaining the reasons behind the design. Create a new descriptive Part note when a new topic or milestone begins. Future notes contain only files created/edited, changes per file, and a Use → Purpose table for concepts not already covered. No exercises or capability checklists unless requested. [[Job Apps Tracker/Learning/Part 1 - Project structure and running the app|Part 1]] contains the completed starter lesson. [[Job Apps Tracker/Learning/Part 2 - Modeling a job application|Part 2]] covers modeling; [[Job Apps Tracker/Learning/Part 13 - Interview stages|Part 13]] covers interviews; [[Job Apps Tracker/Learning/Part 14 - Local browser request protection|Part 14]] covers request protection; [[Job Apps Tracker/Learning/Part 15 - Database backup and restore|Part 15]] covers backups; [[Job Apps Tracker/Learning/Part 16 - Personal release verification and daily use|Part 16]] covers the completed release; [[Job Apps Tracker/Learning/Part 17 - Page themes|Part 17]] covers themes; [[Job Apps Tracker/Learning/Part 18 - Easy startup and shutdown|Part 18]] is the current launcher breakdown.
- Implement agreed increments directly with the brief breakdown format. When the user requests an exercise or submits an attempt, review it and offer hints before replacing code.
- Starter checkpoint completed: capability checklists, request mapping, random test ports, /about endpoint, and separate endpoint tests. User attempts were reviewed; user reported two passing tests. Step 3 is complete; Part 4 explicit-status test and Part 5 GET exercise are reviewed. Part 6 lookup test and Part 9 service update exercise are also complete and reviewed.

## Version Control
- Private repository: [aalatraqchi/Job-App-Tracker](https://github.com/aalatraqchi/Job-App-Tracker).
- Main tracks origin/main; DTO PR #1 and create/list PR #2 were merged with explicit approval (0d3628c and 51830be).
- [PR #3 — Complete application CRUD and Thymeleaf tracker](https://github.com/aalatraqchi/Job-App-Tracker/pull/3) merged with explicit approval (5d9dd42). Current branch: `main`; page themes and the daily-use launcher milestone are merged. [PR #4 — Filter applications by company and status](https://github.com/aalatraqchi/Job-App-Tracker/pull/4) merged with explicit approval (05c25bf). [PR #5 — Persist job applications in PostgreSQL](https://github.com/aalatraqchi/Job-App-Tracker/pull/5) merged with explicit user approval (d31898c). All 42 tests and packaging passed before merging PR #5. PR #6 merged with fresh explicit approval (19ecfed). PR #7 merged with fresh explicit approval (a4e822e); its 54-test build passed. PR #8 merged with fresh explicit approval (3eeb553). The personal-release checklist is verified and PR #9 merged with fresh explicit approval (bd10cba). PR #10 themes are also merged with fresh explicit approval (200d4bf). PR #11 daily startup/shutdown is merged with fresh explicit approval (635ff41). Personal-use scope is complete; future account work remains deferred.
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
