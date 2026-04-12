Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$envInfo = & (Join-Path $PSScriptRoot "env.ps1")

function Test-PortListening {
  param([int]$Port)
  $lines = netstat -ano -p tcp 2>$null
  if (-not $lines) { return $false }
  $pattern = "[:\.]$Port\s+.*LISTENING"
  return [bool]($lines | Select-String -Pattern $pattern)
}

function Wait-Port {
  param([int]$Port, [int]$TimeoutSeconds = 30)
  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  while ((Get-Date) -lt $deadline) {
    if (Test-PortListening -Port $Port) { return $true }
    Start-Sleep -Milliseconds 500
  }
  return $false
}

function Start-WindowsServiceIfExists {
  param([string]$Name)
  $service = Get-Service -Name $Name -ErrorAction SilentlyContinue
  if (-not $service) { return $false }
  try {
    if ($service.Status -ne "Running") {
      Start-Service -Name $Name -ErrorAction Stop
      $service.WaitForStatus("Running", [TimeSpan]::FromSeconds(20))
    }
  } catch {
    return $false
  }
  $service = Get-Service -Name $Name -ErrorAction SilentlyContinue
  return ($service -and $service.Status -eq "Running")
}

function Start-RedisLocal {
  $redisExe = Join-Path $envInfo.RedisBase "redis-server.exe"
  if (-not (Test-Path $redisExe)) {
    throw "Redis binary not found: $redisExe"
  }
  $redisDataDir = Join-Path $envInfo.RuntimeRoot "redis\\data"
  $redisLogDir = Join-Path $envInfo.RuntimeRoot "redis\\logs"
  New-Item -ItemType Directory -Force -Path $redisDataDir | Out-Null
  New-Item -ItemType Directory -Force -Path $redisLogDir | Out-Null

  $confPath = Join-Path $envInfo.RuntimeRoot "redis\\redis.local.conf"
  $logPath = (Join-Path $redisLogDir "redis.log").Replace('\', '/')
  $dataPath = $redisDataDir.Replace('\', '/')
  @"
bind 127.0.0.1
protected-mode yes
port 6379
timeout 0
tcp-keepalive 300
daemonize no
supervised no
loglevel notice
logfile $logPath
databases 16
dir $dataPath
save 900 1
save 300 10
save 60 10000
appendonly no
"@ | Set-Content -Path $confPath -Encoding UTF8

  $proc = Start-Process -FilePath $redisExe -ArgumentList "`"$confPath`"" -PassThru
  $proc.Id | Set-Content -Path (Join-Path $envInfo.LogRoot "redis-local.pid") -Encoding ASCII
}

function Start-MysqlLocal {
  $mysqlBin = Join-Path $envInfo.MysqlBase "bin"
  $mysqld = Join-Path $mysqlBin "mysqld.exe"
  $mysqlCli = Join-Path $mysqlBin "mysql.exe"
  if (-not (Test-Path $mysqld)) {
    throw "MySQL binary not found: $mysqld"
  }

  $mysqlDataDir = Join-Path $envInfo.RuntimeRoot "mysql\\data"
  $mysqlTmpDir = Join-Path $envInfo.RuntimeRoot "mysql\\tmp"
  $mysqlLogDir = Join-Path $envInfo.RuntimeRoot "mysql\\logs"
  New-Item -ItemType Directory -Force -Path $mysqlDataDir | Out-Null
  New-Item -ItemType Directory -Force -Path $mysqlTmpDir | Out-Null
  New-Item -ItemType Directory -Force -Path $mysqlLogDir | Out-Null

  $cnf = Join-Path $envInfo.RuntimeRoot "mysql\\my.local.ini"
  $basedir = $envInfo.MysqlBase.Replace('\', '/')
  $datadir = $mysqlDataDir.Replace('\', '/')
  $tmpdir = $mysqlTmpDir.Replace('\', '/')
  $errlog = (Join-Path $mysqlLogDir "error.log").Replace('\', '/')
  @"
[mysqld]
port=3306
basedir=$basedir
datadir=$datadir
tmpdir=$tmpdir
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
default-time-zone=+08:00
default_authentication_plugin=mysql_native_password
max_connections=300
sql_mode=STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION
log-error=$errlog

[client]
port=3306
default-character-set=utf8mb4
"@ | Set-Content -Path $cnf -Encoding UTF8

  if (-not (Test-Path (Join-Path $mysqlDataDir "mysql"))) {
    & $mysqld "--defaults-file=$cnf" --initialize-insecure
  }

  $proc = Start-Process -FilePath $mysqld -ArgumentList "--defaults-file=$cnf" -PassThru
  $proc.Id | Set-Content -Path (Join-Path $envInfo.LogRoot "mysql-local.pid") -Encoding ASCII

  if (Wait-Port -Port 3306 -TimeoutSeconds 40) {
    if (Test-Path $mysqlCli) {
      & $mysqlCli --default-character-set=utf8mb4 -h 127.0.0.1 -P 3306 -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root'; CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED BY 'root'; GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION; FLUSH PRIVILEGES;" 2>$null
    }
  }
}

if (-not (Test-PortListening -Port 3306)) {
  $mysqlServiceStarted = Start-WindowsServiceIfExists -Name "MySQL80_Internship"
  if (-not $mysqlServiceStarted) {
    Start-MysqlLocal
  }
}

if (-not (Wait-Port -Port 3306 -TimeoutSeconds 40)) {
  throw "MySQL startup failed, port 3306 is not listening."
}

if (-not (Test-PortListening -Port 6379)) {
  $redisServiceStarted = Start-WindowsServiceIfExists -Name "redis_internship"
  if (-not $redisServiceStarted) {
    Start-RedisLocal
  }
}

if (-not (Wait-Port -Port 6379 -TimeoutSeconds 20)) {
  throw "Redis startup failed, port 6379 is not listening."
}

Write-Output "deps_ready"
