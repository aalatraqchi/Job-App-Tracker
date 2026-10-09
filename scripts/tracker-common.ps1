$ErrorActionPreference = 'Stop'
$trackerRoot = Split-Path $PSScriptRoot -Parent
$trackerRuntime = Join-Path $trackerRoot '.tracker'
$trackerStatePath = Join-Path $trackerRuntime 'app.json'
$trackerPom = [xml](Get-Content -LiteralPath (Join-Path $trackerRoot 'pom.xml') -Raw)
$trackerJarName = "$($trackerPom.project.artifactId)-$($trackerPom.project.version).jar"
$trackerJar = Join-Path $trackerRoot "target/$trackerJarName"
$trackerUrl = 'http://127.0.0.1:8080'

function Lock-Tracker {
    $null = New-Item -ItemType Directory -Path $trackerRuntime -Force
    try {
        return [IO.File]::Open((Join-Path $trackerRuntime 'operation.lock'),
            [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    } catch {
        throw 'Another Start/Stop operation is in progress. Wait for it to finish.'
    }
}

function Get-ManagedTracker {
    if (-not (Test-Path -LiteralPath $trackerStatePath)) { return $null }
    $state = Get-Content -LiteralPath $trackerStatePath -Raw | ConvertFrom-Json
    $process = Get-Process -Id $state.processId -ErrorAction SilentlyContinue
    if (-not $process) { return $null }
    $created = $process.StartTime.ToUniversalTime().ToString('o')
    if ($created -ne $state.startedUtc) { return $null }
    $identity = Get-CimInstance Win32_Process -Filter "ProcessId=$($process.Id)"
    $jarPattern = '-jar\s+"?' + [regex]::Escape($trackerJar) + '"?(?:\s|$)'
    if ($identity.Name -notin @('java.exe', 'javaw.exe') -or
        $identity.CommandLine -notmatch $jarPattern) {
        throw 'The recorded process is not this tracker. No process was stopped.'
    }
    return $process
}

function Get-TrackerListeners {
    return @(Get-NetTCPConnection -State Listen -LocalPort 8080 `
        -ErrorAction SilentlyContinue)
}

function Test-DockerReady {
    try {
        $null = & docker info --format '{{.ServerVersion}}' 2>$null
        return $LASTEXITCODE -eq 0
    } catch { return $false }
}

function Get-TrackerFingerprint {
    $files = @(Get-ChildItem -LiteralPath (Join-Path $trackerRoot 'src/main') -Recurse -File |
        Where-Object { $_.Name -ne 'application-local.properties' })
    $files += Get-Item -LiteralPath (Join-Path $trackerRoot 'pom.xml')
    $entries = foreach ($file in ($files | Sort-Object FullName)) {
        $relative = $file.FullName.Substring($trackerRoot.Length)
        $relative + ':' + (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash
    }
    $hash = [Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [Text.Encoding]::UTF8.GetBytes(($entries -join "`n"))
        return [BitConverter]::ToString($hash.ComputeHash($bytes)).Replace('-', '')
    } finally { $hash.Dispose() }
}

function Test-TrackerReady {
    try {
        $health = Invoke-RestMethod "$trackerUrl/health" -TimeoutSec 2
        return $health.application -eq 'job-app-tracker' -and $health.status -eq 'UP'
    } catch { return $false }
}

function Open-TrackerBrowser {
    try { Start-Process $trackerUrl } catch {
        Write-Host "The app is ready. Open $trackerUrl in your browser."
    }
}
