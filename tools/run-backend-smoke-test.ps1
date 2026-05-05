param(
  [string]$Level = "B1",
  [int]$Count = 10,
  [int]$Seed = 12345
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Push-Location (Split-Path -Parent $PSCommandPath)
try {
  javac .\BackendSmokeTest.java
  if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

  java BackendSmokeTest $Level $Count $Seed
  exit $LASTEXITCODE
} finally {
  Pop-Location
}

