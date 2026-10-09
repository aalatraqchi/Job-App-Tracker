## Files created

| File | What changed |
| --- | --- |
| `Start Tracker.cmd`, `Stop Tracker.cmd` | Provide double-click launchers; keep the window open on an error. |
| `scripts/start-tracker.ps1` | Finds JDK 21, starts Docker/database, rebuilds changed source, launches the app in the background, waits for readiness, and opens the browser. Reuses an existing managed app. |
| `scripts/stop-tracker.ps1` | Checks process identity, stops the managed app, then stops the database container without removing its volume. |
| `scripts/tracker-common.ps1` | Shares paths, process/port checks, operation locking, source fingerprinting, readiness, and browser opening. |
| `scripts/install-shortcuts.ps1` | Creates Start/Stop desktop shortcuts pointing to this project; refuses to replace a different shortcut. |
| `scripts/test-tracker-launchers.ps1` | Checks real startup/shutdown, duplicate starts, wrong-process/occupied-port rejection, stale state, cached/changed builds, and saved records across restarts. |

## Files edited

| File | What changed |
| --- | --- |
| `.gitignore` | Keeps `.tracker/` process state, build fingerprints, and logs local. |
| `README.md` | Makes the shortcuts the daily workflow; retains IntelliJ/terminal alternatives and troubleshooting. |
| `Project Outline.md`, `AGENTS.md` | Records the agreed launcher milestone and verification. |

## New content

| Use | Purpose |
| --- | --- |
| `Start-Process` with redirected logs | Runs the app independently of the terminal while preserving diagnostics. |
| PID + process start time + JAR path | Identifies the app safely before stopping it; a PID alone can be reused. |
| Exclusive file lock | Prevents overlapping Start/Stop operations. |
| Source and JAR fingerprints | Reuses a current build and rebuilds changed/missing output. |
| Readiness polling | Opens the browser only after the app responds successfully. |
