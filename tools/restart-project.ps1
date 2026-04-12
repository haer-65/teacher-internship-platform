param(
  [switch]$SkipDeps,
  [int]$BackendTimeoutSeconds = 90,
  [int]$FrontendTimeoutSeconds = 45
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

& (Join-Path $PSScriptRoot "start-project.ps1") `
  -Restart `
  -SkipDeps:$SkipDeps `
  -BackendTimeoutSeconds $BackendTimeoutSeconds `
  -FrontendTimeoutSeconds $FrontendTimeoutSeconds
