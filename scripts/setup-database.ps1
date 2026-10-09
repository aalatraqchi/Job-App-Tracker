$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    $secretPath = Join-Path (Get-Location) '.env'
    $localPath = Join-Path (Get-Location) 'src/main/resources/application-local.properties'
    if (-not (Test-Path -LiteralPath $secretPath)) {
        if (Test-Path -LiteralPath $localPath) {
            throw 'Local configuration already exists. Configure .env with its database password rather than overwriting it.'
        }
        $randomBytes = New-Object byte[] 32
        $randomGenerator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
        try { $randomGenerator.GetBytes($randomBytes) } finally { $randomGenerator.Dispose() }
        $databaseSecret = [BitConverter]::ToString($randomBytes).Replace('-', '')
        [System.IO.File]::WriteAllText($secretPath, "DB_PASSWORD=$databaseSecret`n")
    }
    $secretLine = Get-Content -LiteralPath $secretPath |
        Where-Object { $_ -match '^DB_PASSWORD=[A-Fa-f0-9]{64}$' } | Select-Object -First 1
    if (-not $secretLine) {
        throw '.env must contain a generated 64-character hexadecimal DB_PASSWORD. No files were overwritten.'
    }
    if (-not (Test-Path -LiteralPath $localPath)) {
        $databaseSecret = $secretLine.Substring('DB_PASSWORD='.Length)
        [System.IO.File]::WriteAllText($localPath, "DB_PASSWORD=$databaseSecret`n")
    }
    & docker compose up -d --wait database
    if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL failed to start. Start Docker Desktop and retry.' }
    Write-Output 'PostgreSQL is ready on localhost:5433. Local credentials are in ignored files.'
} finally {
    Pop-Location
}
