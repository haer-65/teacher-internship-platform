Set-StrictMode -Version Latest
$ErrorActionPreference = "SilentlyContinue"

$envInfo = & (Join-Path $PSScriptRoot "env.ps1")
$logRoot = $envInfo.LogRoot

function Stop-ByPidFile {
  param([string]$PidFile)
  if (Test-Path $PidFile) {
    $processId = Get-Content -Path $PidFile -ErrorAction SilentlyContinue
    if ($processId) {
      Stop-Process -Id ([int]$processId) -Force -ErrorAction SilentlyContinue
    }
    Remove-Item -Path $PidFile -Force -ErrorAction SilentlyContinue
  }
}

function Stop-ByListeningPort {
  param([int]$Port)
  $lines = netstat -ano -p tcp 2>$null | Select-String -Pattern "[:\.]$Port\s+.*LISTENING"
  if (-not $lines) { return }
  $pids = @()
  foreach ($line in $lines) {
    $parts = ($line.ToString() -split '\s+') | Where-Object { $_ }
    if ($parts.Length -ge 5) {
      $processId = $parts[$parts.Length - 1]
      if ($processId -match '^\d+$') { $pids += [int]$processId }
    }
  }
  $pids = $pids | Select-Object -Unique
  foreach ($processId in $pids) {
    Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
  }
}

Stop-ByPidFile -PidFile (Join-Path $logRoot "frontend.pid")
Stop-ByPidFile -PidFile (Join-Path $logRoot "backend.pid")
Stop-ByPidFile -PidFile (Join-Path $logRoot "mysql-local.pid")
Stop-ByPidFile -PidFile (Join-Path $logRoot "redis-local.pid")

Stop-ByListeningPort -Port 5173
Stop-ByListeningPort -Port 8080

function Port-Listening {
  param([int]$Port)
  $lines = netstat -ano -p tcp 2>$null
  [bool]($lines | Select-String -Pattern "[:\.]$Port\s+.*LISTENING")
}

function Wait-PortClosed {
  param(
    [int]$Port,
    [int]$TimeoutSeconds = 15
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  while ((Get-Date) -lt $deadline) {
    if (-not (Port-Listening -Port $Port)) {
      return $true
    }
    Start-Sleep -Milliseconds 400
  }

  return $false
}

Wait-PortClosed -Port 8080 | Out-Null
Wait-PortClosed -Port 5173 | Out-Null

$still8080 = Port-Listening -Port 8080
$still5173 = Port-Listening -Port 5173
if ($still8080 -or $still5173) {
  Write-Output "project_stopped_with_warning"
  if ($still8080) { Write-Output "warning: port 8080 is still listening (process may need elevated stop)." }
  if ($still5173) { Write-Output "warning: port 5173 is still listening (process may need elevated stop)." }
} else {
  Write-Output "project_stopped"
}
