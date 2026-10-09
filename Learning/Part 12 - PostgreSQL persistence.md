## Files created

| File                                                                               | What changed                                                                                                                   |
| ---------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| `src/main/java/com/ashraf/jobapptracker/application/ApplicationRepository.java`    | Extends `JpaRepository<JobApplication, Long>` to provide database operations without writing SQL for basic CRUD.               |
| `compose.yaml`                                                                     | Runs PostgreSQL 17 on localhost port 5433, checks readiness, and keeps data in a named volume.                                 |
| `scripts/setup-database.ps1`                                                       | Generates ignored local credentials once, starts the database, and waits for readiness. Run it after starting Docker Desktop.  |
| `src/test/java/com/ashraf/jobapptracker/PostgresTestSupport.java`                  | Starts a disposable PostgreSQL test database, supplies its connection settings, and clears only test records before each test. |
| `src/test/java/com/ashraf/jobapptracker/application/PostgresPersistenceTests.java` | Creates an application, closes the Spring context, restarts it, and verifies all fields, filters, and continued ID generation. |

## Files edited

Java paths below are under `src/main/java/com/ashraf/jobapptracker/`.

| File | What changed |
| --- | --- |
| `application/JobApplication.java` | Maps existing fields to the `job_applications` table. PostgreSQL generates IDs; status is stored by name. Added a protected no-argument constructor, a constructor for new records without an ID, and `updateDetails()` for editing an existing entity. |
| `application/ApplicationService.java` | Constructor receives the repository. `create()`, `findById()`, `update()`, and `delete()` now use database operations; removed the map and `AtomicLong`. Existing defaults, filters, ordering, and missing-ID behavior remain. Updates change the existing entity and retain its ID. |
| `pom.xml` | Added Spring Data JPA, the PostgreSQL driver, and Testcontainers PostgreSQL support. Excluded local credentials from packaged resources. |
| `src/main/resources/application.properties` | Added datasource settings, optional external local configuration, automatic schema updates for this milestone, and disabled database sessions during page rendering. |
| `src/main/resources/templates/tracker/fragments.html` | Footer now says records are saved to PostgreSQL. |
| Existing test classes | Extend `PostgresTestSupport`. Service tests now use the Spring-managed service; assertions compare IDs/field values because reading a row can produce a different Java object. Removed the invalid null-date ordering fixture because the database requires a date. |
| `README.md`, `Project Outline.md`, `AGENTS.md` | Added database/run instructions and completed step 6. All 42 tests and packaging pass. A manual check also retained a record after restarting both the app and database; the temporary record was then removed. |

## New content

| Use                                                           | Purpose                                                                                                                                                               |
| ------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `@Entity` / `@Table(name = "job_applications")`               | Map `JobApplication` objects to rows in the chosen database table.                                                                                                    |
| `@Id` / `@GeneratedValue(strategy = GenerationType.IDENTITY)` | Mark the row's identity and let PostgreSQL generate it when saved. New objects start with ID `0`; callers still never supply IDs in create requests.                  |
| `@Column(nullable = false, length = 200)`                     | Require a stored value and limit its length. Request validation still gives useful errors before saving.                                                              |
| `@Enumerated(EnumType.STRING)`                                | Store readable status names such as `INTERVIEW` rather than enum positions that could change if reordered.                                                            |
| `protected JobApplication()`                                  | Provide the no-argument constructor JPA needs when loading stored records.                                                                                            |
| `JpaRepository<JobApplication, Long>`                         | Supply CRUD methods for this entity and its ID type. `save()` persists changes, `findById()` returns an optional result, and `delete()` removes a row.                |
| `@Transactional`                                              | Keep an operation's database changes together: commit when it succeeds or roll back on failure. Read methods use `readOnly = true`; create/update/delete override it. |
| `spring.datasource.*` / `${DB_PASSWORD}`                      | Configure the database connection using local configuration or environment variables without committing a password.                                                   |
| `spring.jpa.hibernate.ddl-auto=update`                        | Create/update tables while learning. Versioned migrations replace it in step 10.                                                                                      |
| `spring.jpa.open-in-view=false`                               | Finish database work in the service rather than leaving a database session available while rendering HTML.                                                            |
| Docker Compose named volume                                   | Keep PostgreSQL's files independently of the running app/container so records survive restarts.                                                                       |
| Testcontainers / `@DynamicPropertySource`                     | Run tests against a separate real PostgreSQL instance and supply its temporary URL/credentials automatically. Docker Desktop must be running.                         |
| Docker Desktop | Runs the Docker engine that manages PostgreSQL's container. Start it before starting the database. |
| Independent app/database processes | IntelliJ starts/stops Spring Boot on port 8080. Docker separately starts/stops PostgreSQL, reached by the app on localhost:5433. Stopping one does not stop the other. |
| `docker compose up -d --wait database` | Start the database in the background (`-d`) and wait until it is ready (`--wait`). Run from the project root before starting the app. The setup script also runs this command. |
| `docker compose stop database` | Stop PostgreSQL when finished for the day. The named volume keeps saved records. Leaving it running between app restarts is also fine. |
