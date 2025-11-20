# docs/evidence/verify-city-reports.ps1
# Quick sanity checks for city reports R07–R16.
# Uses the running Docker app on http://localhost:7080.

$ErrorActionPreference = "Stop"
$base = "http://localhost:7080/api"

function Test-CityReport {
  param(
    [Parameter(Mandatory = $true)][string]$RelativeUrl,
    [Parameter(Mandatory = $true)][string]$Description
  )

  $url = "$base$RelativeUrl"
  Write-Host "Checking $Description -> $url" -ForegroundColor Cyan

  try {
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing -ErrorAction Stop
  }
  catch {
    Write-Host "❌  $Description : request failed ($url)" -ForegroundColor Red
    Write-Host $_.Exception.Message
    return
  }

  if ($response.StatusCode -ne 200) {
    Write-Host "❌  $Description : HTTP $($response.StatusCode)" -ForegroundColor Red
    return
  }

  try {
    $json = $response.Content | ConvertFrom-Json
  }
  catch {
    Write-Host "❌  $Description : response was not valid JSON" -ForegroundColor Red
    return
  }

  if (-not $json) {
    Write-Host "❌  $Description : empty result set" -ForegroundColor Red
    return
  }

  # Normalise to array
  if ($json -isnot [System.Array]) {
    $json = @($json)
  }

  # City report shape: name, country, district, population
  $sample = $json[0]
  if ($null -eq $sample.name -or
    $null -eq $sample.country -or
    $null -eq $sample.district -or
    $null -eq $sample.population) {

    Write-Host "❌  $Description : fields missing (expected name,country,district,population)" -ForegroundColor Red
    return
  }

  Write-Host "✅  $Description looks OK (rows: $($json.Count))" -ForegroundColor Green
}

Write-Host "=== Verifying city reports R07–R16 ===" -ForegroundColor Yellow

# R07–R11 "all" reports
Test-CityReport "/cities/world"                      "R07 cities world"
Test-CityReport "/cities/continent/Asia"            "R08 cities continent Asia"
Test-CityReport "/cities/region/Eastern%20Asia"     "R09 cities region Eastern Asia"
Test-CityReport "/cities/country/China"             "R10 cities country China"
Test-CityReport "/cities/district/New%20York"       "R11 cities district New York"

# R12–R16 top-5 reports (use query param n)
Test-CityReport "/cities/world/top?n=5"             "R12 cities world top 5"
Test-CityReport "/cities/continent/Asia/top?n=5"    "R13 cities continent Asia top 5"
Test-CityReport "/cities/region/Eastern%20Asia/top?n=5" "R14 cities region Eastern Asia top 5"
Test-CityReport "/cities/country/China/top?n=5"     "R15 cities country China top 5"
Test-CityReport "/cities/district/New%20York/top?n=5" "R16 cities district New York top 5"

Write-Host "=== Done ===" -ForegroundColor Yellow
