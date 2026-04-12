param(
  [switch]$SkipDeps,
  [switch]$Restart,
  [bool]$AutoRestartIfProjectRunning = $true,
  [int]$BackendTimeoutSeconds = 90,
  [int]$FrontendTimeoutSeconds = 45
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$envInfo = & (Join-Path $PSScriptRoot "env.ps1")
$backendPidFile = Join-Path $envInfo.LogRoot "backend.pid"
$frontendPidFile = Join-Path $envInfo.LogRoot "frontend.pid"

function Test-PortListening {
  param([int]$Port)
  $lines = netstat -ano -p tcp 2>$null
  if (-not $lines) { return $false }
  $pattern = "[:\.]$Port\s+.*LISTENING"
  return [bool]($lines | Select-String -Pattern $pattern)
}

function Test-ProjectPidFileExists {
  return (Test-Path $backendPidFile) -or (Test-Path $frontendPidFile)
}

function Test-ProjectPossiblyRunning {
  return (Test-PortListening -Port 8080) -or (Test-PortListening -Port 5173)
}

if ($Restart) {
  Write-Output "project_restart_requested"
  & (Join-Path $PSScriptRoot "stop-project.ps1")
} elseif ($AutoRestartIfProjectRunning -and (Test-ProjectPidFileExists) -and (Test-ProjectPossiblyRunning)) {
  Write-Output "project_detected_running_restart"
  & (Join-Path $PSScriptRoot "stop-project.ps1")
}

if (-not $SkipDeps) {
  & (Join-Path $PSScriptRoot "start-deps.ps1")
}

& (Join-Path $PSScriptRoot "start-backend.ps1") -TimeoutSeconds $BackendTimeoutSeconds
& (Join-Path $PSScriptRoot "start-frontend.ps1") -TimeoutSeconds $FrontendTimeoutSeconds

Write-Output "project_started"
Write-Output "frontend=http://127.0.0.1:5173"
Write-Output "backend=http://127.0.0.1:8080"
