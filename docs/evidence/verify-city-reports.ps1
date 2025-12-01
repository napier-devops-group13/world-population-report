# docs/evidence/verify-city-reports.ps1
# Compare live API output with stored CSV evidence for R07–R16 (city reports).

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

Write-Host "=== Verifying city reports R07-R16 ==="

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

  # Normalise line endings and drop blank lines on both sides
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

# R07–R11: all cities variants
Compare-Report "R07" "R07_cities_world.csv"                      "/cities/world"
Compare-Report "R08" "R08_cities_continent_Asia.csv"             "/cities/continent/Asia"
Compare-Report "R09" "R09_cities_region_WesternEurope.csv"       "/cities/region/Western%20Europe"
Compare-Report "R10" "R10_cities_country_UnitedKingdom.csv"      "/cities/country/United%20Kingdom"
Compare-Report "R11" "R11_cities_district_Kabol.csv"             "/cities/district/Kabol"

# R12–R16: top-N city reports
Compare-Report "R12" "R12_cities_world_top10.csv"                "/cities/world/top?n=10"
Compare-Report "R13" "R13_cities_continent_Europe_top5.csv"      "/cities/continent/Europe/top?n=5"
Compare-Report "R14" "R14_cities_region_WesternEurope_top5.csv"  "/cities/region/Western%20Europe/top?n=5"
Compare-Report "R15" "R15_cities_country_UnitedKingdom_top5.csv" "/cities/country/United%20Kingdom/top?n=5"
Compare-Report "R16" "R16_cities_district_Kabol_top3.csv"        "/cities/district/Kabol/top?n=3"

Write-Host "=== Done ==="
