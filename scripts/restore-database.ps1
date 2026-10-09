[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$BackupFile,
    [ValidatePattern('^job_app_tracker_restore_[a-z0-9_]{1,32}$')]
    [string]$Database = ('job_app_tracker_restore_{0}_{1}' -f (Get-Date -Format 'yyyyMMdd_HHmmss'),
        ([Guid]::NewGuid().ToString('N').Substring(0, 8)))
)

$ErrorActionPreference = 'Stop'
$backupPath = (Resolve-Path -LiteralPath $BackupFile).ProviderPath
if (-not (Test-Path -LiteralPath $backupPath -PathType Leaf)) { throw 'BackupFile must be a file.' }
if (-not (Test-Path -LiteralPath "$backupPath.sha256" -PathType Leaf)) {
    throw 'The matching .sha256 file is required; keep it beside the backup.'
}
$expectedHash = [IO.File]::ReadAllText("$backupPath.sha256").Trim()
if ($expectedHash -notmatch '^[A-Fa-f0-9]{64}$' -or
    (Get-FileHash -LiteralPath $backupPath -Algorithm SHA256).Hash -ne $expectedHash) {
    throw 'Backup checksum does not match. No database was created or changed.'
}
$containerFile = '/tmp/tracker-restore-{0}.dump' -f [Guid]::NewGuid().ToString('N')
$created = $false
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    $exists = & docker compose exec -T database psql -X -U job_app_tracker -d postgres -At `
        -v ON_ERROR_STOP=1 -c "SELECT 1 FROM pg_database WHERE datname = '$Database';"
    if ($LASTEXITCODE -ne 0) { throw 'Could not check the restore destination. Start the database.' }
    if ($exists -eq '1') { throw 'Restore destination already exists; choose a new name. Nothing was overwritten.' }
    & docker compose cp $backupPath "database:$containerFile"
    if ($LASTEXITCODE -ne 0) { throw 'Could not copy the archive into the container.' }
    $null = & docker compose exec -T database pg_restore --list $containerFile
    if ($LASTEXITCODE -ne 0) { throw 'Invalid PostgreSQL archive. No database was created.' }
    & docker compose exec -T database createdb -U job_app_tracker --template=template0 $Database
    if ($LASTEXITCODE -ne 0) { throw 'Could not create the new restore database.' }
    $created = $true
    & docker compose exec -T database pg_restore -U job_app_tracker -d $Database `
        --no-owner --no-privileges --single-transaction --exit-on-error $containerFile
    if ($LASTEXITCODE -ne 0) {
        throw "Restore failed and its transaction was rolled back. Empty destination retained: $Database"
    }
    Write-Host "Restored into separate database: $Database"
    Write-Host "To use it, set DB_URL=jdbc:postgresql://127.0.0.1:5433/$Database before starting the app."
    Write-Output $Database
} catch {
    if ($created) { Write-Warning "The new database $Database was retained for inspection; the working database was not replaced." }
    throw
} finally {
    & docker compose exec -T database rm -f $containerFile
    if ($LASTEXITCODE -ne 0) { Write-Warning 'Could not remove the temporary archive inside the container.' }
    Pop-Location
}
