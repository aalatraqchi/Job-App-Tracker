[CmdletBinding()]
param(
    [ValidatePattern('^job_app_tracker(?:_[a-z0-9_]{1,40})?$')]
    [string]$Database = 'job_app_tracker',
    [string]$OutputDirectory = (Join-Path (Split-Path $PSScriptRoot -Parent) 'backups')
)

$ErrorActionPreference = 'Stop'
$backupDirectory = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($OutputDirectory)
$backupName = '{0}-{1}-{2}.dump' -f $Database, (Get-Date -Format 'yyyyMMdd-HHmmss'),
    ([Guid]::NewGuid().ToString('N').Substring(0, 8))
$backupPath = Join-Path $backupDirectory $backupName
$containerFile = '/tmp/tracker-backup-{0}.dump' -f [Guid]::NewGuid().ToString('N')
$completed = $false
$copyStarted = $false
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    if (Test-Path -LiteralPath $backupPath) { throw 'Backup destination already exists; nothing was overwritten.' }
    $null = New-Item -ItemType Directory -Path $backupDirectory -Force
    & docker compose exec -T database pg_dump -U job_app_tracker -d $Database `
        --format=custom --file=$containerFile
    if ($LASTEXITCODE -ne 0) { throw 'Backup failed. Start the database and check its name.' }
    $null = & docker compose exec -T database pg_restore --list $containerFile
    if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL could not read the backup archive.' }
    # Copy the binary archive directly; PowerShell text pipelines can corrupt it.
    $copyStarted = $true
    & docker compose cp "database:$containerFile" $backupPath
    if ($LASTEXITCODE -ne 0) { throw 'Could not copy the archive to this computer.' }
    if ((Get-Item -LiteralPath $backupPath).Length -eq 0) { throw 'Backup archive is empty.' }
    $checksum = (Get-FileHash -LiteralPath $backupPath -Algorithm SHA256).Hash
    [IO.File]::WriteAllText("$backupPath.sha256", $checksum + "`n")
    $completed = $true
    Write-Host "Backup saved: $backupPath"
    # Return the path so another script can verify/restore this exact file.
    Write-Output $backupPath
} finally {
    & docker compose exec -T database rm -f $containerFile
    if ($LASTEXITCODE -ne 0) { Write-Warning 'Could not remove the temporary archive inside the container.' }
    if ($copyStarted -and -not $completed -and (Test-Path -LiteralPath $backupPath)) {
        Remove-Item -LiteralPath $backupPath
    }
    Pop-Location
}
