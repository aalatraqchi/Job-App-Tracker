# Backup and restore

The scripts use the `pg_dump` and `pg_restore` tools inside the PostgreSQL 17 container, so you don't need a separate PostgreSQL client. They run in Windows PowerShell from the project root. Docker and the database must be running.

## Back up

```powershell
$backup = .\scripts\backup-database.ps1
```

This writes a timestamped `.dump` archive and a matching `.dump.sha256` checksum to `backups/`, which is git-ignored. The archive contains the schema, application and interview records, and ID sequence values. It doesn't contain database passwords or global roles. The app can keep running during a backup.

Archives aren't encrypted. Keep each archive with its checksum, and copy both to a private location on another drive if you want protection against losing this one.

## Restore

A restore always goes into a **new, separate database**, so your current data is never overwritten:

```powershell
# For an older backup, pass the full path to its .dump file instead of $backup.
$restored = .\scripts\restore-database.ps1 -BackupFile $backup
```

The script verifies the checksum and the archive, creates a uniquely named `job_app_tracker_restore_...` database, and restores into it in a single transaction. It refuses the working database name and any database that already exists. If the restore fails, its changes are rolled back and the new database is left for inspection.

To use the restored data, stop the app, point it at the restored database and start it again:

```powershell
$env:DB_URL = "jdbc:postgresql://127.0.0.1:5433/$restored"
.\mvnw.cmd spring-boot:run
```

While you use the restored database, back it up with `.\scripts\backup-database.ps1 -Database $restored`.

To go back to the original database, stop the app, remove the override and start it again:

```powershell
Remove-Item Env:DB_URL -ErrorAction SilentlyContinue
.\mvnw.cmd spring-boot:run
```

On a fresh machine, run `.\scripts\setup-database.ps1` before restoring, to create the database user and local credentials.

## Verify backup and restore

Stop writes to the app, then run:

```powershell
.\scripts\test-database-backup.ps1
```

The script takes a real backup and checks the restored application and interview data. It then uses disposable databases to check Unicode text, dates, nulls, relationships and ID sequences, and confirms that overwrites and corrupted files are rejected. It drops only the temporary databases it created, and it confirms that your development data hasn't changed.
