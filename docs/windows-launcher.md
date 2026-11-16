# Windows launcher and daily use

These scripts make the tracker a one-click desktop app on Windows. They use Windows PowerShell 5.1, JDK 21 and Docker Desktop.

## Install the desktop shortcuts

Run this from the project root:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\install-shortcuts.ps1
```

This creates **Start Job Application Tracker** and **Stop Job Application Tracker** shortcuts on the desktop. They point to `Start Tracker.cmd` and `Stop Tracker.cmd` in the project root. Run the script again if you move the project folder.

## Start

Double-click **Start Job Application Tracker**, or run `Start Tracker.cmd`. The launcher:

1. Starts Docker Desktop if it isn't running.
2. Prepares PostgreSQL, generating local credentials on the first run.
3. Rebuilds the app only when source files have changed (with `clean package`, skipping tests).
4. Waits for the website, then opens <http://127.0.0.1:8080/> in your default browser.

The app keeps running in the background, so you can close the launch window. Clicking Start again opens the running app instead of starting a second copy. The first start can take longer while dependencies and the PostgreSQL image download.

If startup fails, the window stays open with an error message. Logs are written to the git-ignored `.tracker/` folder: `build.log`, `app.log` and `app-error.log`.

## Stop

Double-click **Stop Job Application Tracker**, or run `Stop Tracker.cmd`. It stops the Java process the launcher started, then this project's database container. Your records stay in the Docker volume. Closing the browser alone leaves the app running.

Notes:

- The launcher only manages the app it started. If an IDE or terminal session is already using port 8080, stop that first. Stop refuses to touch an unrelated process on the port.
- Stop ends the Java process directly rather than sending a graceful Spring shutdown, so finish any in-progress saves first.
- No system execution-policy setting is changed. The `.cmd` wrappers allow only these local script invocations.
- After changing code, click Stop and then Start to rebuild. Run `.\mvnw.cmd verify` to run the tests before using a new build.

## Run the app manually

To run from a terminal or an IDE instead, start Docker Desktop and run:

```powershell
.\scripts\setup-database.ps1
.\mvnw.cmd spring-boot:run
```

`setup-database.ps1` generates a random password on first run and writes it to two git-ignored files: `.env` (for Docker Compose) and `src/main/resources/application-local.properties` (for Spring Boot). Running the script again keeps the existing credentials. The local properties file is also excluded from the packaged JAR. Run the app with the project root as its working directory so Spring can find that file.

To run the app from an IDE, start the database with the script above, then run `JobAppTrackerApplication` with the project root as the working directory. Stop the app with Ctrl+C or the IDE's stop button. To stop the database as well, run `docker compose stop database`. Stopping the database keeps its data.

To run on a different port:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.port=8081'
```

## Verify the launchers

With the tracker stopped, run:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\test-tracker-launchers.ps1
```

This script checks duplicate and concurrent starts, repeated stops, rejection of wrong processes and occupied ports, recovery from a stale PID, cached and changed builds, and that saved records survive restarts.
