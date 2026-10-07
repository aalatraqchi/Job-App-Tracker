## 10/5/26

### Project planning — Java Job Application Tracker
- Created [[Project Outline]] from the supplied project idea, using the System Monitor notes as the format reference.
- Defined a local Java 21/Spring Boot backend for application CRUD, company/status filters, interview stages, PostgreSQL persistence, authentication, and per-user ownership.
- Added 12 ordered milestones with individual checkboxes and verification checkpoints, starting with a small plain-Java exercise and in-memory API before persistence and security.
- Documented the data model, endpoint plan, code responsibilities, first-version boundaries, official references, and completion criteria.
- No implementation or environment setup performed; implementation checkboxes remain unchecked.
- Next: outline step 1, refresh core Java with the application model.

### Environment verification and Step 2 — Spring Boot starter
- Confirmed existing Oracle JDK 21.0.2, JAVA_HOME, Maven 3.9.6, and IntelliJ IDEA 2023.3.3 installations. No new system-wide software or environment-variable changes were needed.
- Generated a Java 21/Maven Spring Boot 4.1.1 starter with Spring Web and Validation in E:\Codex\Job App Tracker. Corrected the generated parent version suffix to the Maven Central version 4.1.1.
- Added GET /health returning application name and status UP; bound the development server to localhost.
- Added Maven Wrapper (Maven 3.9.16), source README with IntelliJ import/run instructions, and ignores for build output and local secrets. Initialized Git on main; no commit or remote created.
- Verified mvnw.cmd verify: packaged successfully and one real HTTP integration test passed (HTTP 200, JSON content type, expected response fields). Also launched the packaged app and verified the live localhost:8080/health response.
- Updated the outline: step 2 complete; plain-Java refresher optional given previous Java/Spring Boot experience. PostgreSQL remains step 6.
- Next: step 3, application model and in-memory create/list API.

### Learning mode — Starter foundations
- Added [[Part 1]], organized by concept: project structure and tools, Maven/dependencies, Java packages/imports/method declarations, Spring annotations and managed objects, startup/request flow, localhost lifecycle, and the existing integration test.
- Used the starter's actual classes, configuration, and test as examples; included start/stop/restart instructions and short "I should be able to..." checklists.
- Saved the agreed learning workflow in source AGENTS.md for future coding assistance.
- Assigned a small user exercise: trace and run /health, observe startup/shutdown behavior, then attempt /about using already explained concepts. The assistant has not implemented the exercise.
- No application code changed, and step 3 has not started. Understanding/exercise completion remains unverified until the user attempts and reviews the work.
- Next: work through the starter learning checkpoint together, then resume the application model/create-list milestone.

## 10/6/26

### Starter learning checkpoint complete
- User completed the learning capability checklists and correctly identified @GetMapping as the connection between GET /health and its Java method.
- Explained random test ports and @LocalServerPort; user confirmed that requests target the test server without competing with development port 8080.
- User implemented GET /about and a separate integration test, preserving /health coverage. Reviewed the actual source and the shared attempt without replacing the user's code.
- User reported mvnw.cmd test passed with two tests, zero failures, and zero errors. No additional test run performed during this documentation update.
- Updated Learning and Project Outline to reflect completed practice. No application code changed by the assistant.
- Next: step 3, starting with the application model and building toward in-memory create/list behavior together.

### Learning notes organized into parts
- User moved the completed starter lesson into Learning/Part 1.md.
- Updated source AGENTS.md and the outline to create separate numbered lesson notes in the Learning folder when new topics or milestones begin.
- Updated existing lesson links to point to Part 1. Preserved the lesson content; no new lesson or application code created.
- Next lesson will be Part 2 when the step 3 learning session begins.

### Step 3 started — Application model foundations
- Added application/JobApplication.java as a minimal worked example: private company field, explicit constructor, and getter. No controller/service or database wiring yet.
- Created [[Job Apps Tracker/Learning/Part 2|Part 2]] for model concepts: classes versus objects/maps, feature packages, instance fields, constructors, this, getters, and why a data object needs no Spring annotation.
- Assigned the user the position field, constructor extension, and getter using the company pattern; exercise not implemented by the assistant and not yet reviewed.
- Verified mvnw.cmd test: compilation succeeded; both existing /health and /about tests passed. Those endpoint tests do not verify the new model's assignments.
- Updated source learning instructions and outline progress. Step 3 checklist items remain unchecked while the model, DTOs, service, and endpoints are incomplete.
- Next: review the position exercise, then introduce application date/status types and complete the model incrementally.

### Part 2 — Position exercise reviewed
- Reviewed the user's JobApplication.java: private position field, two-argument constructor assigning company and position to their matching fields, and getPosition() returning position are all correct. getCompany() remains intact.
- User reported tests passed. Existing endpoint tests do not verify model assignments; reviewed those assignments directly. No additional test run or code replacement was needed.
- Marked the exercise review complete in Part 2 and updated outline progress. Full model/DTO step remains incomplete.
- Next: explain LocalDate and application-status enums before extending the model further.

## 10/7/26


### Part 2 — Date and status types introduced
- Added ApplicationStatus enum with APPLIED, INTERVIEW, OFFER, REJECTED, and WITHDRAWN as a worked example in the application package.
- Extended Part 2 with LocalDate, date-value comparison, enum constants, and why these types fit application date/status. Explained that types do not automatically enforce non-null values, defaults, or business transition rules.
- Assigned the user appliedDate/status fields, constructor arguments, and getters using the previously practiced pattern. JobApplication is unchanged; the exercise and concept understanding remain unverified.
- Verified Maven compilation with -DskipTests compile. No new tests added or existing tests rerun for this enum-only example.
- Next: user explains the type choices and attempts the model extension; review before proceeding to the remaining model fields and DTOs.

### Part 2 — Date/status exercise reviewed
- Reviewed the user's JobApplication: LocalDate import, appliedDate/status field types, four-argument constructor, and matching getters are correct. Company and position remain intact.
- Explained that the constructor parameter date can correctly initialize the field appliedDate; their names need not match.
- Marked the exercise review complete in Part 2 and updated outline progress. Preserved the user's implementation without replacement.
- Existing test report shows two passing endpoint tests; no additional test run performed in this review. Model assignments were inspected directly because those endpoint tests do not cover them.
- Next: finish remaining model fields, then introduce request/response DTOs. Step 3 remains incomplete.

### Part 2 — Optional model data exercise
- Extended Part 2 with jobUrl/notes, optional data versus required Java constructor arguments, and a short preview of request/model/response responsibilities.
- Assigned the user jobUrl/notes fields, constructor assignments, and getters using the pattern already covered. Deferred generated ID until the service's identity responsibility is introduced.
- No application code changed or tests run; this was a documentation and exercise update. Exercise and new capability checkboxes remain unverified.
- Next: review the user's text-field attempt, then introduce DTO implementation.

### Part 2 — Text-field attempt reviewed, correction pending
- Reviewed the user's url/notes fields, constructor assignments, and getters. Assignments and return values are correct; both new fields are package-private rather than private.
- Asked the user to add private for consistency with the model's encapsulation. Recommended jobUrl/getJobUrl naming to match the planned API terminology; url/getUrl itself is valid Java.
- Existing report shows two passing endpoint tests; no tests rerun or user code replaced. Endpoint tests do not detect this visibility/design issue.
- Kept exercise completion pending. Next: user corrects field visibility, then review before DTO implementation.

### Part 2 — Text-field corrections complete
- Reviewed the corrected JobApplication: jobUrl and notes are private; getJobUrl and getNotes return the correct fields. All constructor assignments remain correct, including jobUrl = url.
- Marked the exercise reviewed and updated the next checkpoint to request/response DTOs. No application code changed or tests rerun during this review.
- Next: introduce DTOs; service-generated ID, storage, and application endpoints remain pending.


### GitHub setup and version-control learning
- Created private repository https://github.com/aalatraqchi/Job-App-Tracker under the signed-in GitHub account.
- Committed the existing project as baseline 75faa18 (feat: establish Spring Boot starter and application model), added origin, and pushed main with upstream tracking. Verified the files and Private visibility in Chrome.
- Verified Maven verify before the baseline push: two HTTP integration tests passed, zero failures/errors, executable JAR packaged successfully. Build output, IDE settings, local secret files, logs, and starter archive are ignored.
- Created Part 3 for Git/GitHub concepts and a small read-only practice exercise; updated README and the outline. Understanding remains unverified until the user attempts the exercise.
- Next: user inspects status, remotes, and log; then resume DTO learning on a feature branch. No application implementation advanced during GitHub setup.
