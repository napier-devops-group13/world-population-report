# docs/evidence/verify-capital-reports.ps1
# Compare live API output with stored CSV evidence for R17–R22 (capital reports).

param(
# Use http://localhost:7070/api if running from IntelliJ instead of Docker
  [string]$ApiBase = "http://localhost:7080/api"
)

$ErrorActionPreference = "Stop"

# Folder where this script lives (...\world-population-report\docs\evidence)
$EvidenceDir = $PSScriptRoot
if (-not $EvidenceDir) {
  $EvidenceDir = (Get-Location).Path
}

Write-Host "=== Verifying capital reports R17-R22 ==="

function Compare-Report {
  param(
    [string]$Id,
    [string]$File,
    [string]$Path
  )

  $csvPath = Join-Path $EvidenceDir $File
  $url     = "$ApiBase$Path"

  Write-Host "Checking $Id $File -> $url"

  if (-not (Test-Path $csvPath)) {
    Write-Host "FAIL $Id : evidence file not found at $csvPath" -ForegroundColor Red
    return
  }

  # Normalise line endings and drop blank lines
  $expectedLines = (Get-Content $csvPath) -replace "`r","" | Where-Object { $_ -ne "" }

  try {
    $resp = Invoke-WebRequest -Uri $url -UseBasicParsing -ErrorAction Stop
    $actualLines = ($resp.Content -replace "`r","") -split "`n" | Where-Object { $_ -ne "" }
  }
  catch {
    Write-Host "FAIL $Id : request failed - $($_.Exception.Message)" -ForegroundColor Red
    return
  }

  $diff = Compare-Object -ReferenceObject $expectedLines -DifferenceObject $actualLines

  if ($diff) {
    Write-Host "FAIL $Id : differs from API output" -ForegroundColor Red
  }
  else {
    Write-Host "OK   $Id : matches API output" -ForegroundColor Green
  }
}

# R17-R22 checks (must match generate-capital-reports.ps1)
Compare-Report "R17" "R17_capitals_world.csv"                     "/capitals/world"
Compare-Report "R18" "R18_capitals_continent_Europe.csv"          "/capitals/continent/Europe"
Compare-Report "R19" "R19_capitals_region_Caribbean.csv"          "/capitals/region/Caribbean"
Compare-Report "R20" "R20_capitals_world_top10.csv"               "/capitals/world/top/10"
Compare-Report "R21" "R21_capitals_continent_Europe_top5.csv"     "/capitals/continent/Europe/top/5"
Compare-Report "R22" "R22_capitals_region_Caribbean_top3.csv"     "/capitals/region/Caribbean/top/3"

Write-Host "=== Done ==="
