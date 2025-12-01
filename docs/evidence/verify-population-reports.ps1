# docs/evidence/verify-population-reports.ps1
# Compare live API output with stored CSV evidence for R24–R26.
#
# Default base URL assumes the population routes are mounted at:
#   http://localhost:7080/reports/population/...
# If you mount them under /api instead, run with:
#   .\verify-population-reports.ps1 -ApiBase "http://localhost:7080/api"

param(
  [string]$ApiBase = "http://localhost:7080"
)

$ErrorActionPreference = "Stop"

# Folder where this script lives (...\world-population-report\docs\evidence)
$EvidenceDir = $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($EvidenceDir)) {
  $EvidenceDir = (Get-Location).Path
}

Write-Host "=== Verifying population reports R24-R26 ==="

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

# R24 – Population in / out of cities for each region.
Compare-Report "R24" "R24_population_regions.csv"   "/reports/population/regions"

# R25 – Population in / out of cities for each country.
Compare-Report "R25" "R25_population_countries.csv" "/reports/population/countries"

# R26 – Population of the world.
Compare-Report "R26" "R26_population_world.csv"    "/reports/population/world"

Write-Host "=== Done ==="
