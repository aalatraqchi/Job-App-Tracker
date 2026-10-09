[CmdletBinding()]
param([switch]$NoBrowser)

. (Join-Path $PSScriptRoot 'tracker-common.ps1')
$operationLock = $null
$launchedProcess = $null
Push-Location $trackerRoot
try {
    $operationLock = Lock-Tracker
    $managed = Get-ManagedTracker
    if ($managed) {
        if (-not (Test-TrackerReady)) {
            throw 'The tracker process is running but not ready. Use Stop Tracker, then retry Start.'
        }
    }
    if (-not $managed -and @(Get-TrackerListeners).Count -gt 0) {
        throw 'Port 8080 is already in use. Stop your IntelliJ app or the other app, then retry.'
    }
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw 'Docker Desktop is required. Install it, then retry Start Tracker.'
    }
    if (-not (Test-DockerReady)) {
        $dockerDesktop = Join-Path $env:ProgramFiles 'Docker/Docker/Docker Desktop.exe'
        if (-not (Test-Path -LiteralPath $dockerDesktop)) {
            throw 'Start Docker Desktop manually, then retry Start Tracker.'
        }
        Write-Host 'Starting Docker Desktop...'
        Start-Process -FilePath $dockerDesktop -WindowStyle Hidden
        $dockerDeadline = [DateTime]::UtcNow.AddSeconds(120)
        while (-not (Test-DockerReady)) {
            if ([DateTime]::UtcNow -gt $dockerDeadline) {
                throw 'Docker is not ready yet. Check Docker Desktop, then retry.'
            }
            Start-Sleep -Seconds 2
        }
    }

    $javaHomes = @($env:JAVA_HOME, (Join-Path $env:ProgramFiles 'Java/jdk-21'))
    $javaHomes += @(Get-ChildItem -Path (Join-Path $env:USERPROFILE '.jdks') `
        -Directory -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName)
    $javaExecutable = $null
    foreach ($candidate in $javaHomes) {
        if (-not $candidate) { continue }
        $executable = Join-Path $candidate 'bin/java.exe'
        if (-not (Test-Path -LiteralPath $executable)) { continue }
        $versionText = (& $executable --version 2>&1 | Out-String)
        if ($versionText -match '(?m)^(?:java|openjdk) 21[. ]') {
            $env:JAVA_HOME = $candidate
            $javaExecutable = $executable
            break
        }
    }
    if (-not $javaExecutable) { throw 'Java 21 was not found. Set JAVA_HOME to your JDK 21 folder.' }

    Write-Host 'Preparing the database...'
    & (Join-Path $PSScriptRoot 'setup-database.ps1')
    if ($managed) {
        Write-Host 'Tracker is already running. Opening the existing website.'
        if (-not $NoBrowser) { Open-TrackerBrowser }
        return
    }
    $fingerprint = Get-TrackerFingerprint
    $buildPath = Join-Path $trackerRuntime 'build.json'
    $build = $null
    if (Test-Path -LiteralPath $buildPath) {
        try { $build = Get-Content -LiteralPath $buildPath -Raw | ConvertFrom-Json } catch { }
    }
    $currentJarHash = $null
    if (Test-Path -LiteralPath $trackerJar) {
        $currentJarHash = (Get-FileHash -LiteralPath $trackerJar -Algorithm SHA256).Hash
    }
    if (-not $build -or $build.sourceHash -ne $fingerprint -or
        $build.jarHash -ne $currentJarHash) {
        Write-Host 'Building the current source (first launch or changed files)...'
        & (Join-Path $trackerRoot 'mvnw.cmd') clean package -DskipTests *> (Join-Path $trackerRuntime 'build.log')
        if ($LASTEXITCODE -ne 0) {
            throw 'Build failed. See .tracker/build.log in the project folder.'
        }
        @{ sourceHash = $fingerprint
           jarHash = (Get-FileHash -LiteralPath $trackerJar -Algorithm SHA256).Hash } |
            ConvertTo-Json | Set-Content -LiteralPath $buildPath -Encoding UTF8
    }

    Write-Host 'Starting Application Tracker...'
    $launchedProcess = Start-Process -FilePath $javaExecutable `
        -ArgumentList @('-jar', ('"' + $trackerJar + '"'), '--server.port=8080',
            '--server.address=127.0.0.1') -WorkingDirectory $trackerRoot `
        -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput (Join-Path $trackerRuntime 'app.log') `
        -RedirectStandardError (Join-Path $trackerRuntime 'app-error.log')
    @{ processId = $launchedProcess.Id
       startedUtc = $launchedProcess.StartTime.ToUniversalTime().ToString('o') } |
        ConvertTo-Json | Set-Content -LiteralPath $trackerStatePath -Encoding UTF8
    $appDeadline = [DateTime]::UtcNow.AddSeconds(90)
    while (-not (Test-TrackerReady)) {
        $launchedProcess.Refresh()
        if ($launchedProcess.HasExited -or [DateTime]::UtcNow -gt $appDeadline) {
            throw 'Tracker did not become ready. See .tracker/app.log and app-error.log.'
        }
        Start-Sleep -Seconds 1
    }
    $listeners = @(Get-TrackerListeners)
    if ($listeners.Count -eq 0 -or
        @($listeners | Where-Object { $_.OwningProcess -ne $launchedProcess.Id }).Count -gt 0) {
        throw 'Port 8080 belongs to another process. The managed tracker will be stopped.'
    }
    Write-Host "Ready: $trackerUrl"
    Write-Host 'You may close this window. Use Stop Tracker when finished.'
    if (-not $NoBrowser) { Open-TrackerBrowser }
} catch {
    if ($launchedProcess) {
        $managed = Get-ManagedTracker
        if ($managed) { Stop-Process -Id $managed.Id -ErrorAction SilentlyContinue }
        Remove-Item -LiteralPath $trackerStatePath -ErrorAction SilentlyContinue
    }
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
} finally {
    Pop-Location
    if ($operationLock) { $operationLock.Dispose() }
}
