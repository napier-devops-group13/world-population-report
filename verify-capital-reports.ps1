# verify-capital-reports.ps1
# Quick sanity checks for capital city reports R17–R22.
# Uses the running Docker app on http://localhost:7080.

$ErrorActionPreference = "Stop"

$base = "http://localhost:7080/api"

function Test-CapitalReport {
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

  # Capital city report shape: name, country, population
  $sample = $json[0]
  if ($null -eq $sample.name -or
    $null -eq $sample.country -or
    $null -eq $sample.population) {

    Write-Host "❌  $Description : fields missing (expected name,country,population)" -ForegroundColor Red
    return
  }

  Write-Host "✅  $Description looks OK (rows: $($json.Count))" -ForegroundColor Green
}

Write-Host "=== Verifying capital city reports R17–R22 ===" -ForegroundColor Yellow

# R17 – all capital cities in the world
Test-CapitalReport "/capitals/world"                          "R17 world capitals"

# R18 – all capital cities in a continent (example: Asia)
Test-CapitalReport "/capitals/continent/Asia"                 "R18 continent Asia capitals"

# R19 – all capital cities in a region (example: Eastern Asia)
Test-CapitalReport "/capitals/region/Eastern%20Asia"          "R19 region Eastern Asia capitals"

# R20 – top-5 capital cities in the world
Test-CapitalReport "/capitals/world/top/5"                    "R20 world capitals top 5"

# R21 – top-5 capital cities in a continent (example: Asia)
Test-CapitalReport "/capitals/continent/Asia/top/5"           "R21 continent Asia capitals top 5"

# R22 – top-5 capital cities in a region (example: Eastern Asia)
Test-CapitalReport "/capitals/region/Eastern%20Asia/top/5"    "R22 region Eastern Asia capitals top 5"

Write-Host "=== Done ===" -ForegroundColor Yellow
