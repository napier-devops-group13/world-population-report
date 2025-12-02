# docs/evidence/generate-population-lookup-reports.ps1
# Generate CSV evidence files for lookup reports R27–R32 from the running API.
#
# Endpoints (from PopulationRoutes.java):
#   R27: /reports/population/continents/Asia
#   R28: /reports/population/regions/Caribbean
#   R29: /reports/population/countries/Myanmar
#   R30: /reports/population/districts/Rangoon%20%5BYangon%5D
#   R31: /reports/population/cities/Rangoon%20%28Yangon%29
#   R32: /reports/population/languages
#
# Docker base URL example:
#   http://localhost:7080
# IntelliJ base URL example:
#   http://localhost:7070

param(
  [string]$ApiBase = "http://localhost:7080"
)

$ErrorActionPreference = "Stop"

# Folder where this script lives (...\world-population-report\docs\evidence)
$EvidenceDir = $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($EvidenceDir)) {
  $EvidenceDir = (Get-Location).Path
}

# Make sure the evidence directory exists
if (-not (Test-Path $EvidenceDir)) {
  New-Item -ItemType Directory -Path $EvidenceDir -Force | Out-Null
}

# ---------------------------------------------------------------------------
# Lookup report definitions (R27–R32)
# ---------------------------------------------------------------------------
$lookups = @(
# R27 - continent: Asia
  @{
    Id   = "R27"
    File = "R27_population_continent_Asia.csv"
    Path = "/reports/population/continents/Asia"
  },

  # R28 - region: Caribbean
  @{
    Id   = "R28"
    File = "R28_population_region_Caribbean.csv"
    Path = "/reports/population/regions/Caribbean"
  },

  # R29 - country: Myanmar
  @{
    Id   = "R29"
    File = "R29_population_country_Myanmar.csv"
    Path = "/reports/population/countries/Myanmar"
  },

  # R30 - district: Rangoon [Yangon]
  # URL encoded "[ ]" so it matches the DB value exactly
  @{
    Id   = "R30"
    File = "R30_population_district_Rangoon.csv"
    Path = "/reports/population/districts/Rangoon%20%5BYangon%5D"
  },

  # R31 - city: Rangoon (Yangon)
  # URL encoded "( )" so it matches the DB value exactly
  @{
    Id   = "R31"
    File = "R31_population_city_Rangoon_Yangon.csv"
    Path = "/reports/population/cities/Rangoon%20%28Yangon%29"
  },

  # R32 - languages (Chinese, English, Hindi, Spanish, Arabic)
  @{
    Id   = "R32"
    File = "R32_language_populations.csv"
    Path = "/reports/population/languages"
  }
)

# Give the app a few seconds if it was just started
Start-Sleep -Seconds 5

foreach ($r in $lookups) {
  $url     = $ApiBase.TrimEnd('/') + $r.Path
  $outPath = Join-Path $EvidenceDir $r.File

  Write-Host ("[{0}] Fetching {1}" -f $r.Id, $url)

  try {
    $resp    = Invoke-WebRequest -Uri $url -ErrorAction Stop
    $bodyRaw = $resp.Content
  }
  catch {
    Write-Host ("!! Failed to fetch {0} : {1}" -f $url, $_.Exception.Message) -ForegroundColor Red
    continue
  }

  # Remove empty trailing lines so CSV has no blank line at the end
  $lines = ($bodyRaw -replace "`r","") -split "`n"
  $lines = $lines | Where-Object { $_.Trim() -ne "" }
  $body  = [string]::Join("`n", $lines)

  # Ensure directory exists
  $dir = Split-Path -Parent $outPath
  if (-not (Test-Path $dir)) {
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
  }

  $body | Set-Content -Path $outPath -Encoding UTF8
  Write-Host ("Saved -> {0}" -f $outPath) -ForegroundColor Green
}

Write-Host "=== Done generating lookup population reports R27-R32 ==="
