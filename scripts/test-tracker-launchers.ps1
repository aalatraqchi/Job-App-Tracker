[CmdletBinding()]
param()

. (Join-Path $PSScriptRoot 'tracker-common.ps1')
if (@(Get-TrackerListeners).Count -gt 0 -or (Get-ManagedTracker)) {
    throw 'Stop the tracker before running launcher verification.'
}
$windowsPowerShell = Join-Path $env:SystemRoot 'System32/WindowsPowerShell/v1.0/powershell.exe'
$savedState = $null
$testListener = $null
$sourceProbe = Join-Path $trackerRoot 'src/main/resources/launcher-verification.tmp'
if (Test-Path -LiteralPath $sourceProbe) { throw 'Verification probe already exists; no files changed.' }

function Invoke-Launcher([string]$Name, [int]$ExpectedExit = 0) {
    & $windowsPowerShell -NoLogo -NoProfile -ExecutionPolicy Bypass `
        -File (Join-Path $PSScriptRoot $Name) @(
            if ($Name -eq 'start-tracker.ps1') { '-NoBrowser' }
        )
    if ($LASTEXITCODE -ne $ExpectedExit) {
        throw "$Name returned $LASTEXITCODE; expected $ExpectedExit."
    }
}

function Get-SavedRecordFingerprint {
    $applications = @(Invoke-RestMethod "$trackerUrl/applications")
    $interviews = @($applications | ForEach-Object {
        Invoke-RestMethod "$trackerUrl/applications/$($_.id)/interviews"
    })
    return @{ applications = $applications; interviews = $interviews } |
        ConvertTo-Json -Depth 12 -Compress
}

Push-Location ([IO.Path]::GetTempPath())
try {
    $testLock = Lock-Tracker
    try { Invoke-Launcher 'start-tracker.ps1' 1 } finally { $testLock.Dispose() }
    Write-Host 'PASS: concurrent launch is rejected.'
    Invoke-Launcher 'start-tracker.ps1'
    $firstProcess = Get-ManagedTracker
    if (-not $firstProcess -or -not (Test-TrackerReady)) { throw 'Startup failed.' }
    $records = Get-SavedRecordFingerprint
    $buildTime = (Get-Item -LiteralPath $trackerJar).LastWriteTimeUtc
    Invoke-Launcher 'start-tracker.ps1'
    if ((Get-ManagedTracker).Id -ne $firstProcess.Id) { throw 'Duplicate start replaced the process.' }
    Write-Host 'PASS: launch from another directory and duplicate start.'

    $savedState = [IO.File]::ReadAllText($trackerStatePath)
    $thisProcess = Get-Process -Id $PID
    @{ processId = $PID; startedUtc = $thisProcess.StartTime.ToUniversalTime().ToString('o') } |
        ConvertTo-Json | Set-Content -LiteralPath $trackerStatePath -Encoding UTF8
    Invoke-Launcher 'stop-tracker.ps1' 1
    if (-not (Get-Process -Id $PID) -or -not (Test-TrackerReady)) {
        throw 'Wrong-process protection failed.'
    }
    [IO.File]::WriteAllText($trackerStatePath, $savedState)
    $savedState = $null
    Write-Host 'PASS: unrelated process identity is rejected.'

    Invoke-Launcher 'stop-tracker.ps1'
    Invoke-Launcher 'stop-tracker.ps1'
    $testListener = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback, 8080)
    $testListener.Start()
    Invoke-Launcher 'start-tracker.ps1' 1
    Invoke-Launcher 'stop-tracker.ps1' 1
    if (-not $testListener.Server.IsBound) { throw 'Unrelated listener was stopped.' }
    Push-Location $trackerRoot
    try {
        $databaseState = & docker compose ps -a --format '{{.State}}' database
        if ($LASTEXITCODE -ne 0 -or $databaseState -ne 'exited') {
            throw 'An occupied port should be rejected before starting the database.'
        }
    } finally { Pop-Location }
    $testListener.Stop()
    $testListener = $null
    Write-Host 'PASS: repeated stop and occupied-port protection.'

    @{ processId = 2147483000; startedUtc = '2000-01-01T00:00:00.0000000Z' } |
        ConvertTo-Json | Set-Content -LiteralPath $trackerStatePath -Encoding UTF8
    Invoke-Launcher 'start-tracker.ps1'
    if ((Get-Item -LiteralPath $trackerJar).LastWriteTimeUtc -ne $buildTime) {
        throw 'Unchanged source was unnecessarily rebuilt.'
    }
    if ((Get-SavedRecordFingerprint) -ne $records) { throw 'Saved records changed after restart.' }
    Write-Host 'PASS: stale PID recovery, cached build, and unchanged saved records.'
    Invoke-Launcher 'stop-tracker.ps1'

    Set-Content -LiteralPath $sourceProbe -Value 'Disposable launcher verification resource.'
    Invoke-Launcher 'start-tracker.ps1'
    if ((Get-Item -LiteralPath $trackerJar).LastWriteTimeUtc -eq $buildTime) {
        throw 'Changed source did not rebuild.'
    }
    Write-Host 'PASS: changed source triggers a rebuild.'
    Invoke-Launcher 'stop-tracker.ps1'
    Remove-Item -LiteralPath $sourceProbe
    Invoke-Launcher 'start-tracker.ps1'
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [IO.Compression.ZipFile]::OpenRead($trackerJar)
    try {
        if ($archive.GetEntry('BOOT-INF/classes/launcher-verification.tmp')) {
            throw 'Removed resource remained in the packaged app.'
        }
    } finally { $archive.Dispose() }
    if ((Get-SavedRecordFingerprint) -ne $records) { throw 'Saved records changed.' }
    Write-Host 'PASS: probe removed and clean source repackaged; records unchanged.'
} finally {
    if ($testListener) { $testListener.Stop() }
    if ($savedState) { [IO.File]::WriteAllText($trackerStatePath, $savedState) }
    if (Test-Path -LiteralPath $sourceProbe) { Remove-Item -LiteralPath $sourceProbe }
    Invoke-Launcher 'stop-tracker.ps1'
    Pop-Location
}
