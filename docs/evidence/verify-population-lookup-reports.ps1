# docs/evidence/verify-population-lookup-reports.ps1
# Verify lookup & language reports R27–R32 against the running API.

param(
  [string]$ApiBase = "http://localhost:7080"
)

$ErrorActionPreference = "Stop"

# Folder where this script lives (...\world-population-report\docs\evidence)
$EvidenceDir = $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($EvidenceDir)) {
  $EvidenceDir = (Get-Location).Path
}

Write-Host "=== Verifying lookup and language reports R27-R32 ==="

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

# R27 - population of a continent (Asia)
Compare-Report "R27" "R27_population_continent_Asia.csv"      "/reports/population/continents/Asia"

# R28 - population of a region (Caribbean)
Compare-Report "R28" "R28_population_region_Caribbean.csv"    "/reports/population/regions/Caribbean"

# R29 - population of a country (Myanmar)
Compare-Report "R29" "R29_population_country_Myanmar.csv"     "/reports/population/countries/Myanmar"

# R30 - population of a district (Rangoon)
Compare-Report "R30" "R30_population_district_Rangoon.csv"    "/reports/population/districts/Rangoon"

# R31 - population of a city (Rangoon (Yangon))
Compare-Report "R31" "R31_population_city_Rangoon_Yangon.csv" "/reports/population/cities/Rangoon%20(Yangon)"

# R32 - language populations (Chinese, English, Hindi, Spanish, Arabic)
Compare-Report "R32" "R32_language_populations.csv"           "/reports/population/languages"

Write-Host "=== Done ==="
