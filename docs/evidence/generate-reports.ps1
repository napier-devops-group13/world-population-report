# docs/evidence/generate-reports.ps1
# Generate CSV evidence for R01–R06 under docs/evidence

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
    # Try to treat the response as JSON
    $json = $text | ConvertFrom-Json

    if ($json) {
      # Normalise to array
      if ($json -isnot [System.Array]) {
        $json = @($json)
      }

      # Country report shape: code, name, continent, region, population, capital
      if ($null -ne $json[0].code -and $null -ne $json[0].population) {
        $csvOutput = $json |
          Select-Object code, name, continent, region, population, capital |
          ConvertTo-Csv -NoTypeInformation
      } else {
        # Unknown shape → just save raw text
        $csvOutput = $text
      }
    } else {
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

# -------------------------------
# R01–R06 evidence CSVs
# -------------------------------

# R01 – all countries in the world
Save-ReportCsv "/countries/world"                    "R01_world.csv"

# R02 – all countries in a continent (example: Asia)
Save-ReportCsv "/countries/continent/Asia"           "R02_continent_Asia.csv"

# R03 – all countries in a region (example: Eastern Asia)
Save-ReportCsv "/countries/region/Eastern%20Asia"    "R03_region_EasternAsia.csv"

# R04 – top-5 countries in the world
Save-ReportCsv "/countries/world/top/5"              "R04_world_top5.csv"

# R05 – top-5 countries in a continent (example: Asia)
Save-ReportCsv "/countries/continent/Asia/top/5"     "R05_continent_Asia_top5.csv"

# R06 – top-5 countries in a region (example: Eastern Asia)
Save-ReportCsv "/countries/region/Eastern%20Asia/top/5" `
    "R06_region_EasternAsia_top5.csv"
