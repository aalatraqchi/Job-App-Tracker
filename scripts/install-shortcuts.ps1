[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$desktopPath = [Environment]::GetFolderPath('Desktop')
$shortcutShell = New-Object -ComObject WScript.Shell
foreach ($action in @('Start', 'Stop')) {
    $shortcutPath = Join-Path $desktopPath "$action Job Application Tracker.lnk"
    if (Test-Path -LiteralPath $shortcutPath) {
        $existing = $shortcutShell.CreateShortcut($shortcutPath)
        if ($existing.TargetPath -ne (Join-Path $projectRoot "$action Tracker.cmd") -and
            $existing.Description -ne "$action the local Job Application Tracker") {
            throw "A different shortcut already exists at $shortcutPath. No replacement was made."
        }
    }
    $shortcut = $shortcutShell.CreateShortcut($shortcutPath)
    $shortcut.TargetPath = Join-Path $projectRoot "$action Tracker.cmd"
    $shortcut.WorkingDirectory = $projectRoot
    $shortcut.Description = "$action the local Job Application Tracker"
    $shortcut.Save()
    Write-Host "Desktop shortcut ready: $action Job Application Tracker"
}
