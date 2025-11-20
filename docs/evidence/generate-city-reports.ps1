# docs/evidence/generate-city-reports.ps1
# Generate CSV evidence for R07–R16 under docs/evidence
# Columns: Name, Country, District, Population

# Fail fast if something goes wrong
$ErrorActionPreference = "Stop"

# Base URL for the API (docker-compose exposes app on localhost:7080)
$base = "http://localhost:7080/api"

function Save-ReportCsv {
  param(
    [Parameter(Mandatory = $true)][string]$RelativeUrl,
    [Parameter(Mandatory = $true)][string]$FileName
  )

  # Folder where this script lives, e.g. ...\world-population-report\docs\evidence
  $root = $PSScriptRoot
  if ([string]::IsNullOrWhiteSpace($root)) {
    # Fallback if script is run from the console, not from a file
    $root = (Get-Location).Path
  }

  # Build full URL and output path
  $url  = "$base$RelativeUrl"
  $path = Join-Path $root $FileName

  Write-Host "Fetching $url" -ForegroundColor Cyan

  try {
    # Call the API and get the raw response text
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing -ErrorAction Stop
  }
  catch {
    Write-Error "Cannot reach $url. Is 'docker-compose up -d' running and the app listening on port 7080?"
    return
  }

  $text      = $response.Content
  $csvOutput = $null

  try {
    # Treat response as JSON (list of city objects)
    $json = $text | ConvertFrom-Json

    if ($json) {
      # Normalise to array
      if ($json -isnot [System.Array]) {
        $json = @($json)
      }

      # Map to a clean 4-column shape:
      # Name, Country, District, Population
      $csvOutput = $json |
        ForEach-Object {
          [PSCustomObject]@{
            Name       = $_.name
            Country    = $_.country
            District   = $_.district
            Population = $_.population
          }
        } |
        ConvertTo-Csv -NoTypeInformation
    } else {
      # Unexpected shape → just save raw text
      $csvOutput = $text
    }
  }
  catch {
    # Not JSON → assume the API already returned CSV/text
    $csvOutput = $text
  }

  # Make sure the folder exists (should already be docs/evidence)
  $dir = Split-Path -Parent $path
  if (-not (Test-Path $dir)) {
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
  }

  $csvOutput | Set-Content -Path $path -Encoding UTF8
  Write-Host "Saved -> $path" -ForegroundColor Green
}

# Give the app a few seconds if you just started it
Start-Sleep -Seconds 5

# -------------------------------
# R07–R16 evidence CSVs (cities)
# -------------------------------

# R07 – all cities in the world
Save-ReportCsv "/cities/world" `
  "R07_cities_world.csv"

# R08 – all cities in a continent (example: Asia)
Save-ReportCsv "/cities/continent/Asia" `
  "R08_cities_continent_Asia.csv"

# R09 – all cities in a region (example: Eastern Asia)
Save-ReportCsv "/cities/region/Eastern%20Asia" `
  "R09_cities_region_EasternAsia.csv"

# R10 – all cities in a country (example: China)
Save-ReportCsv "/cities/country/China" `
  "R10_cities_country_China.csv"

# R11 – all cities in a district (example: New York)
Save-ReportCsv "/cities/district/New%20York" `
  "R11_cities_district_NewYork.csv"

# For R12–R16 we’ll use top 5 as example (n=5)
# NOTE: Top-N endpoints use query param n, not /top/5

# R12 – top-5 cities in the world
Save-ReportCsv "/cities/world/top?n=5" `
  "R12_cities_world_top5.csv"

# R13 – top-5 cities in a continent (Asia)
Save-ReportCsv "/cities/continent/Asia/top?n=5" `
  "R13_cities_continent_Asia_top5.csv"

# R14 – top-5 cities in a region (Eastern Asia)
Save-ReportCsv "/cities/region/Eastern%20Asia/top?n=5" `
  "R14_cities_region_EasternAsia_top5.csv"

# R15 – top-5 cities in a country (China)
Save-ReportCsv "/cities/country/China/top?n=5" `
  "R15_cities_country_China_top5.csv"

# R16 – top-5 cities in a district (New York)
Save-ReportCsv "/cities/district/New%20York/top?n=5" `
  "R16_cities_district_NewYork_top5.csv"
