[CmdletBinding()]
param()

. (Join-Path $PSScriptRoot 'tracker-common.ps1')
$operationLock = $null
Push-Location $trackerRoot
try {
    $operationLock = Lock-Tracker
    $managed = Get-ManagedTracker
    $listeners = @(Get-TrackerListeners)
    if (@($listeners | Where-Object { -not $managed -or $_.OwningProcess -ne $managed.Id }).Count -gt 0) {
        throw 'Another app or IntelliJ is using port 8080. Stop it there first; database was not changed.'
    }
    if ($managed) {
        Write-Host 'Stopping Application Tracker...'
        Stop-Process -Id $managed.Id -ErrorAction Stop
        $null = $managed.WaitForExit(15000)
        if (-not $managed.HasExited) { throw 'App is still running; database left running.' }
    } else { Write-Host 'The managed tracker is already stopped.' }
    Remove-Item -LiteralPath $trackerStatePath -ErrorAction SilentlyContinue
    if (@(Get-TrackerListeners).Count -gt 0) {
        throw 'Port 8080 is in use; database left running.'
    }
    if ((Get-Command docker -ErrorAction SilentlyContinue) -and (Test-DockerReady)) {
        & docker compose stop database
        if ($LASTEXITCODE -ne 0) { throw 'Database stop failed. Check Docker Desktop.' }
    }
    Write-Host 'Tracker and its database are stopped. Your saved records are preserved.'
    Write-Host 'Docker Desktop stays open for other projects.'
} catch {
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
} finally {
    Pop-Location
    if ($operationLock) { $operationLock.Dispose() }
}
