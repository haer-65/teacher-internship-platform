Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$envInfo = & (Join-Path $PSScriptRoot "env.ps1")

function Test-PathFlag {
  param([string]$Path)
  if (Test-Path $Path) { "OK" } else { "MISSING" }
}

function Port-Status {
  param([int]$Port)
  $lines = netstat -ano -p tcp 2>$null
  $pattern = "[:\.]$Port\s+.*LISTENING"
  if ($lines | Select-String -Pattern $pattern) { "LISTENING" } else { "DOWN" }
}

[pscustomobject]@{
  MavenHome = Test-PathFlag $envInfo.MavenHome
  MavenRepo = Test-PathFlag $envInfo.MavenRepo
  MysqlBin = Test-PathFlag (Join-Path $envInfo.MysqlBase "bin\\mysqld.exe")
  RedisBin = Test-PathFlag (Join-Path $envInfo.RedisBase "redis-server.exe")
  Mysql3306 = Port-Status 3306
  Redis6379 = Port-Status 6379
  Backend8080 = Port-Status 8080
  Frontend5173 = Port-Status 5173
} | Format-List
