# verify-country-reports.ps1
# Quick sanity checks for country reports R01–R06.
# Uses the running Docker app on http://localhost:7080.

$ErrorActionPreference = "Stop"

$base = "http://localhost:7080/api"

function Test-CountryReport {
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

  # Country report shape: code, name, continent, region, population, capital
  $sample = $json[0]
  if ($null -eq $sample.code -or
    $null -eq $sample.name -or
    $null -eq $sample.continent -or
    $null -eq $sample.region -or
    $null -eq $sample.population -or
    $null -eq $sample.capital) {

    Write-Host "❌  $Description : fields missing (expected code,name,continent,region,population,capital)" -ForegroundColor Red
    return
  }

  Write-Host "✅  $Description looks OK (rows: $($json.Count))" -ForegroundColor Green
}

Write-Host "=== Verifying country reports R01–R06 ===" -ForegroundColor Yellow

# R01 – all countries in the world
Test-CountryReport "/countries/world"                 "R01 world"

# R02 – all countries in a continent (example: Asia)
Test-CountryReport "/countries/continent/Asia"        "R02 continent Asia"

# R03 – all countries in a region (example: Eastern Asia)
Test-CountryReport "/countries/region/Eastern%20Asia" "R03 region Eastern Asia"

# R04 – top-5 countries in the world
Test-CountryReport "/countries/world/top/5"           "R04 world top 5"

# R05 – top-5 countries in a continent (example: Asia)
Test-CountryReport "/countries/continent/Asia/top/5"  "R05 continent Asia top 5"

# R06 – top-5 countries in a region (example: Eastern Asia)
Test-CountryReport "/countries/region/Eastern%20Asia/top/5" "R06 region Eastern Asia top 5"

Write-Host "=== Done ===" -ForegroundColor Yellow
