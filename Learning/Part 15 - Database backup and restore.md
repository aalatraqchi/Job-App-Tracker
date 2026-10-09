## Files created

| File | What changed |
| --- | --- |
| `scripts/backup-database.ps1` | Saves a timestamped PostgreSQL archive and checksum to `backups/`. Copies the binary file directly out of Docker and removes its temporary container copy. |
| `scripts/restore-database.ps1` | Checks the file/checksum, creates a separate restore database, and restores it in one transaction. Refuses existing destinations and the working database. |
| `scripts/test-database-backup.ps1` | Verifies a real personal backup and disposable fixture data, relationships, ID sequences, corrupt-file rejection, and overwrite refusal. Passed in PowerShell 7 and Windows PowerShell 5.1; cleans up only generated test databases. |

## Files edited

| File | What changed |
| --- | --- |
| `.gitignore` | Excludes `backups/`, `.dump` archives, and their checksum files from Git. |
| `README.md` | Added backup, restore, dataset-switching, and verification commands. Explains keeping backups outside the database volume and selecting the recovered dataset for future starts/backups. Packaged app checked against a restored clone with schema validation, health, tracker HTML, and API count checks. |
| `Project Outline.md`, `AGENTS.md` | Recorded the approved PR #7 merge and verified backup milestone. Final personal workflow/start-stop verification remains next; account features stay deferred. |

## New content

| Use | Purpose |
| --- | --- |
| `pg_dump --format=custom` | Save schema, records, relationships, and ID sequence values in a compressed PostgreSQL backup archive. |
| `docker compose cp` | Copy the binary archive between the container and computer without text-pipeline conversion. |
| SHA-256 sidecar | Detect changed/corrupted backup bytes before creating a restore database; keep it beside the archive. |
| `createdb --template=template0` | Create an empty, separate restore destination. |
| `pg_restore --single-transaction --exit-on-error` | Restore the archive as one operation; roll back if any part fails. |
| `--no-owner --no-privileges` | Restore using the local database user without depending on the original owner/grants. |
| `DB_URL` override | Point the unchanged app at the recovered database while retaining the original dataset. |
