# Setup

## Prerequisites

- **JDK 21.** Set `JAVA_HOME` to your JDK 21 installation, or make sure `java` is on your `PATH`.
- **Docker** with Docker Compose (Docker Desktop on Windows and macOS). Docker runs PostgreSQL 17 for the app and for the tests, so you don't need to install PostgreSQL.

You don't need to install Maven. The Maven Wrapper (`mvnw` / `mvnw.cmd`) downloads the right version on first use. The first run also downloads dependencies and the PostgreSQL image, so it needs internet access.

All commands below run from the project root.

## Run locally

### macOS, Linux or Git Bash

```bash
# 1. Create a local database password. .env is git-ignored.
echo "DB_PASSWORD=$(openssl rand -hex 32)" > .env

# 2. Start PostgreSQL on 127.0.0.1:5433
docker compose up -d --wait database

# 3. Start the app with the same password
export $(cat .env) && ./mvnw spring-boot:run
```

### Windows PowerShell

```powershell
.\scripts\setup-database.ps1
.\mvnw.cmd spring-boot:run
```

`setup-database.ps1` generates a random password on first run and writes it to two git-ignored files: `.env` (for Docker Compose) and `src/main/resources/application-local.properties` (for Spring Boot). Running it again keeps the existing credentials. The local properties file is excluded from the packaged JAR.

Once the app has started, open <http://127.0.0.1:8080/>. The health check is at <http://127.0.0.1:8080/health>.

### Run from an IDE

Start the database with one of the commands above, then run `JobAppTrackerApplication` with the project root as the working directory, so Spring can find `application-local.properties`. On macOS or Linux, add `DB_PASSWORD` from `.env` to the run configuration's environment variables.

## Configuration

| Environment variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://127.0.0.1:5433/job_app_tracker` |
| `DB_USERNAME` | `job_app_tracker` |
| `DB_PASSWORD` | No default. Read from the environment or from the git-ignored `application-local.properties`. |

The app and the database bind to `127.0.0.1` only. PostgreSQL uses port 5433 so it doesn't clash with a PostgreSQL server already on the default port.

To run the app on another port, pass `--server.port`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

The schema is created and updated automatically (`spring.jpa.hibernate.ddl-auto=update`).

## Stop and restart

Stop the app with Ctrl+C, or your IDE's stop button. Stop the database with:

```bash
docker compose stop database
```

Your records are kept in the Docker volume `job-app-tracker_application-data`. Start the database again with `docker compose up -d --wait database`. The app needs the database running to start.

## Run the tests

```bash
./mvnw verify        # Windows: .\mvnw.cmd verify
```

Docker must be running. Testcontainers starts a separate, disposable PostgreSQL database on a random port, so the tests never touch your own records.

## Windows desktop launcher

On Windows, the project includes one-click Start and Stop launchers that manage the app and the database together. They use Windows PowerShell 5.1.

### Install the desktop shortcuts

Run this from the project root:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\install-shortcuts.ps1
```

This creates **Start Job Application Tracker** and **Stop Job Application Tracker** shortcuts on the desktop. They point to `Start Tracker.cmd` and `Stop Tracker.cmd` in the project root. Run the script again if you move the project folder.

### Start

Double-click **Start Job Application Tracker**, or run `Start Tracker.cmd`. The launcher:

1. Starts Docker Desktop if it isn't running.
2. Prepares PostgreSQL, generating local credentials on the first run.
3. Rebuilds the app only when source files have changed (with `clean package`, skipping tests).
4. Waits for the website, then opens <http://127.0.0.1:8080/> in your default browser.

The app keeps running in the background, so you can close the launch window. Clicking Start again opens the running app instead of starting a second copy. The first start can take longer while dependencies and the PostgreSQL image download.

If startup fails, the window stays open with an error message. Logs are written to the git-ignored `.tracker/` folder: `build.log`, `app.log` and `app-error.log`.

### Stop

Double-click **Stop Job Application Tracker**, or run `Stop Tracker.cmd`. It stops the Java process the launcher started, then this project's database container. Your records stay in the Docker volume. Closing the browser alone leaves the app running.

Notes:

- The launcher only manages the app it started. If an IDE or terminal session is already using port 8080, stop that first. Stop refuses to touch an unrelated process on the port.
- Stop ends the Java process directly rather than sending a graceful Spring shutdown, so finish any in-progress saves first.
- No system execution-policy setting is changed. The `.cmd` wrappers allow only these local script invocations.
- After changing code, click Stop and then Start to rebuild. Run `.\mvnw.cmd verify` to run the tests before using a new build.


### Verify the launchers

With the tracker stopped, run:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\test-tracker-launchers.ps1
```

This script checks duplicate and concurrent starts, repeated stops, rejection of wrong processes and occupied ports, recovery from a stale PID, cached and changed builds, and that saved records survive restarts.
