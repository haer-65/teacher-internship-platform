param(
  [int]$TimeoutSeconds = 90
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$envInfo = & (Join-Path $PSScriptRoot "env.ps1")
$backendDir = Join-Path $envInfo.ProjectRoot "backend"
$mvnCmd = Join-Path $envInfo.MavenHome "bin\\mvn.cmd"

if (-not (Test-Path $mvnCmd)) {
  throw "Maven not found: $mvnCmd"
}

$backendLog = Join-Path $envInfo.LogRoot "backend-dev.log"
$backendErr = Join-Path $envInfo.LogRoot "backend-dev.err.log"
$backendPid = Join-Path $envInfo.LogRoot "backend.pid"

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

function Wait-BackendReady {
  param(
    [int]$Port,
    [int]$TimeoutSeconds,
    [string]$LogPath,
    [int]$ProcessId
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  while ((Get-Date) -lt $deadline) {
    if ((Test-Path $LogPath) -and (Select-String -Path $LogPath -Pattern "Started InternshipPlatformApplication" -Quiet -ErrorAction SilentlyContinue)) {
      return $true
    }

    if ((Test-Path $backendErr) -and (Get-Item $backendErr -ErrorAction SilentlyContinue).Length -gt 0) {
      return $false
    }

    if (Test-PortListening -Port $Port) {
      return $true
    }

    $proc = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    if (-not $proc) {
      Start-Sleep -Milliseconds 300
      continue
    }

    Start-Sleep -Milliseconds 500
  }

  return $false
}

$exists = Test-PortListening -Port 8080
if ($exists) {
  Write-Output "backend_already_running"
  exit 0
}

if (Test-Path $backendLog) { Remove-Item $backendLog -Force }
if (Test-Path $backendErr) { Remove-Item $backendErr -Force }

$proc = Start-Process -FilePath $mvnCmd `
  -ArgumentList @(
    "-Dmaven.repo.local=$($envInfo.MavenRepo)",
    "-Dspring-boot.run.fork=true",
    "spring-boot:run"
  ) `
  -WorkingDirectory $backendDir `
  -RedirectStandardOutput $backendLog `
  -RedirectStandardError $backendErr `
  -PassThru

$proc.Id | Set-Content -Path $backendPid -Encoding ASCII
Write-Output "backend_started_pid=$($proc.Id)"

if (-not (Wait-BackendReady -Port 8080 -TimeoutSeconds $TimeoutSeconds -LogPath $backendLog -ProcessId $proc.Id)) {
  Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
  throw "Backend startup timed out. Check logs: $backendLog / $backendErr"
}

$listeningPid = Get-PortListeningPid -Port 8080
if ($listeningPid) {
  $listeningPid | Set-Content -Path $backendPid -Encoding ASCII
}

Write-Output "backend_ready"
