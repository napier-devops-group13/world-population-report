# docs/evidence/verify-country-reports.ps1
# Compare live API output with stored CSV evidence for R01–R06.

param(
# Change to http://localhost:7070/api if you run the app directly in IntelliJ
  [string]$ApiBase = "http://localhost:7080/api"
)

$ErrorActionPreference = "Stop"

# Folder where this script lives (...\world-population-report\docs\evidence)
$EvidenceDir = $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($EvidenceDir)) {
  $EvidenceDir = (Get-Location).Path
}

Write-Host "=== Verifying country reports R01-R06 ==="

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

Compare-Report "R01" "R01_countries_world.csv"                  "/countries/world"
Compare-Report "R02" "R02_countries_continent_Asia.csv"         "/countries/continent/Asia"
Compare-Report "R03" "R03_countries_region_WesternEurope.csv"   "/countries/region/Western%20Europe"
Compare-Report "R04" "R04_countries_world_top10.csv"            "/countries/world/top?n=10"
Compare-Report "R05" "R05_countries_continent_Europe_top5.csv"  "/countries/continent/Europe/top?n=5"
Compare-Report "R06" "R06_countries_region_WesternEurope_top3.csv" "/countries/region/Western%20Europe/top?n=3"

Write-Host "=== Done ==="
