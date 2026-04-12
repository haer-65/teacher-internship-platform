param(
  [int]$TimeoutSeconds = 45
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$envInfo = & (Join-Path $PSScriptRoot "env.ps1")
$frontendDir = Join-Path $envInfo.ProjectRoot "frontend"
$frontLog = Join-Path $envInfo.LogRoot "frontend-dev.log"
$frontErr = Join-Path $envInfo.LogRoot "frontend-dev.err.log"
$frontPid = Join-Path $envInfo.LogRoot "frontend.pid"

function Test-PortListening {
  param([int]$Port)
  $lines = netstat -ano -p tcp 2>$null
  if (-not $lines) { return $false }
  $pattern = "[:\.]$Port\s+.*LISTENING"
  return [bool]($lines | Select-String -Pattern $pattern)
}

function Get-PortListeningPid {
  param([int]$Port)
  $lines = netstat -ano -p tcp 2>$null | Select-String -Pattern "[:\.]$Port\s+.*LISTENING"
  if (-not $lines) { return $null }

  foreach ($line in $lines) {
    $parts = ($line.ToString() -split '\s+') | Where-Object { $_ }
    if ($parts.Length -ge 5) {
      $processId = $parts[$parts.Length - 1]
      if ($processId -match '^\d+$') {
        return [int]$processId
      }
    }
  }

  return $null
}

function Wait-FrontendReady {
  param(
    [int]$Port,
    [int]$TimeoutSeconds,
    [string]$LogPath,
    [int]$ProcessId
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  while ((Get-Date) -lt $deadline) {
    if ((Test-Path $LogPath) -and (Select-String -Path $LogPath -Pattern "ready in|Local:" -Quiet -ErrorAction SilentlyContinue)) {
      return $true
    }

    if ((Test-Path $frontErr) -and (Get-Item $frontErr -ErrorAction SilentlyContinue).Length -gt 0) {
      return $false
    }

    if (Test-PortListening -Port $Port) {
      return $true
    }

    $proc = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    if (-not $proc) {
      Start-Sleep -Milliseconds 200
      continue
    }

    Start-Sleep -Milliseconds 400
  }

  return $false
}

$exists = Test-PortListening -Port 5173
if ($exists) {
  Write-Output "frontend_already_running"
  exit 0
}

$npmCmd = Get-Command "npm.cmd" -ErrorAction SilentlyContinue
if (-not $npmCmd) {
  $fallback = "D:\\node\\npm.cmd"
  if (Test-Path $fallback) {
    $npmExe = $fallback
  } else {
    throw "npm.cmd not found in PATH or D:\\node\\npm.cmd"
  }
} else {
  $npmExe = $npmCmd.Source
}

if (Test-Path $frontLog) { Remove-Item $frontLog -Force }
if (Test-Path $frontErr) { Remove-Item $frontErr -Force }

$proc = Start-Process -FilePath $npmExe `
  -ArgumentList @("run", "dev", "--", "--host", "0.0.0.0", "--port", "5173") `
  -WorkingDirectory $frontendDir `
  -RedirectStandardOutput $frontLog `
  -RedirectStandardError $frontErr `
  -PassThru

$proc.Id | Set-Content -Path $frontPid -Encoding ASCII
Write-Output "frontend_started_pid=$($proc.Id)"

if (-not (Wait-FrontendReady -Port 5173 -TimeoutSeconds $TimeoutSeconds -LogPath $frontLog -ProcessId $proc.Id)) {
  Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
  throw "Frontend startup timed out. Check logs: $frontLog / $frontErr"
}

$listeningPid = Get-PortListeningPid -Port 5173
if ($listeningPid) {
  $listeningPid | Set-Content -Path $frontPid -Encoding ASCII
}

Write-Output "frontend_ready"
