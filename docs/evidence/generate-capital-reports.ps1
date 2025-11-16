# docs/evidence/generate-capital-reports.ps1
# Generate CSV evidence for capital city reports R17–R22 under docs/evidence
# Columns: name, country, population

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
    Write-Error "Cannot reach $url. Is 'docker compose up -d' running and the app listening on port 7080?"
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

      # Capital city report shape: name, country, population
      if ($null -ne $json[0].name -and
        $null -ne $json[0].country -and
        $null -ne $json[0].population) {

        # This gives header row: name,country,population
        $csvOutput = $json |
          Select-Object name, country, population |
          ConvertTo-Csv -NoTypeInformation
      }
      else {
        # Unknown shape → just save raw text
        $csvOutput = $text
      }
    }
    else {
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

# --------------------------------------------
# Give the app a few seconds to finish starting
# --------------------------------------------
Start-Sleep -Seconds 5

# -------------------------------
# R17–R22 capital city evidence CSVs
# -------------------------------

# R17 – all capital cities in the world
Save-ReportCsv "/capitals/world"                          "R17_world_capitals.csv"

# R18 – all capital cities in a continent (example: Asia)
Save-ReportCsv "/capitals/continent/Asia"                 "R18_continent_Asia_capitals.csv"

# R19 – all capital cities in a region (example: Eastern Asia)
Save-ReportCsv "/capitals/region/Eastern%20Asia"          "R19_region_EasternAsia_capitals.csv"

# R20 – top-5 capital cities in the world
Save-ReportCsv "/capitals/world/top/5"                    "R20_world_capitals_top5.csv"

# R21 – top-5 capital cities in a continent (example: Asia)
Save-ReportCsv "/capitals/continent/Asia/top/5"           "R21_continent_Asia_capitals_top5.csv"

# R22 – top-5 capital cities in a region (example: Eastern Asia)
Save-ReportCsv "/capitals/region/Eastern%20Asia/top/5"    "R22_region_EasternAsia_capitals_top5.csv"
