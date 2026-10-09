@echo off
setlocal
set "PSModulePath="
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\start-tracker.ps1"
set "trackerExit=%errorlevel%"
if not "%trackerExit%"=="0" pause
exit /b %trackerExit%
