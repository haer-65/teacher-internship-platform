param(
  [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
)

$ToolsRoot = Join-Path $ProjectRoot "tools"
$MavenHome = Join-Path $ToolsRoot "apache-maven-3.9.9"
$MavenBin = Join-Path $MavenHome "bin"
$MavenRepo = Join-Path $ToolsRoot "maven-repo"
$MysqlBase = Join-Path $ToolsRoot "mysql\\mysql-8.0.37-winx64"
$RedisBase = Join-Path $ToolsRoot "redis"
$StateRoot = Join-Path $env:LOCALAPPDATA "TeacherInternshipPlatform"
$RuntimeRoot = Join-Path $StateRoot "runtime"
$LogRoot = Join-Path $StateRoot "logs"
$UploadRoot = Join-Path $ProjectRoot "uploads"

New-Item -ItemType Directory -Force -Path $StateRoot | Out-Null
New-Item -ItemType Directory -Force -Path $RuntimeRoot | Out-Null
New-Item -ItemType Directory -Force -Path $LogRoot | Out-Null
New-Item -ItemType Directory -Force -Path $UploadRoot | Out-Null
New-Item -ItemType Directory -Force -Path $MavenRepo | Out-Null

if (Test-Path $MavenBin) {
  $env:MAVEN_HOME = $MavenHome
  if (-not ($env:Path -split ';' | Where-Object { $_ -eq $MavenBin })) {
    $env:Path = "$MavenBin;$env:Path"
  }
}

$env:DB_HOST = "127.0.0.1"
$env:DB_PORT = "3306"
$env:DB_NAME = "teacher_internship_platform"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "root"

$env:REDIS_HOST = "127.0.0.1"
$env:REDIS_PORT = "6379"
$env:REDIS_DB = "0"
$env:REDIS_PASSWORD = ""

$env:FILE_STORAGE_ROOT = $UploadRoot
$env:APP_BOOTSTRAP_ENABLED = "true"

[pscustomobject]@{
  ProjectRoot = $ProjectRoot
  ToolsRoot = $ToolsRoot
  MavenHome = $MavenHome
  MavenRepo = $MavenRepo
  MysqlBase = $MysqlBase
  RedisBase = $RedisBase
  StateRoot = $StateRoot
  RuntimeRoot = $RuntimeRoot
  LogRoot = $LogRoot
}
