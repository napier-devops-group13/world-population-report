# docs/evidence/generate-city-reports.ps1
# Generate CSV evidence files for R07–R16 (city reports) from the running API.

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

# R07–R16 mappings (example parameters chosen to match coursework spec)
$reports = @(
# R07–R11 : “all cities …”
  @{ Id = "R07"; File = "R07_cities_world.csv";                       Path = "/cities/world" },
  @{ Id = "R08"; File = "R08_cities_continent_Asia.csv";              Path = "/cities/continent/Asia" },
  @{ Id = "R09"; File = "R09_cities_region_WesternEurope.csv";        Path = "/cities/region/Western%20Europe" },
  @{ Id = "R10"; File = "R10_cities_country_UnitedKingdom.csv";       Path = "/cities/country/United%20Kingdom" },
  @{ Id = "R11"; File = "R11_cities_district_Kabol.csv";              Path = "/cities/district/Kabol" },

  # R12–R16 : “top N cities …” (N chosen for clear evidence files)
  @{ Id = "R12"; File = "R12_cities_world_top10.csv";                 Path = "/cities/world/top?n=10" },
  @{ Id = "R13"; File = "R13_cities_continent_Europe_top5.csv";       Path = "/cities/continent/Europe/top?n=5" },
  @{ Id = "R14"; File = "R14_cities_region_WesternEurope_top5.csv";   Path = "/cities/region/Western%20Europe/top?n=5" },
  @{ Id = "R15"; File = "R15_cities_country_UnitedKingdom_top5.csv";  Path = "/cities/country/United%20Kingdom/top?n=5" },
  @{ Id = "R16"; File = "R16_cities_district_Kabol_top3.csv";         Path = "/cities/district/Kabol/top?n=3" }
)

# Give the app a few seconds if it was just started
Start-Sleep -Seconds 5

foreach ($r in $reports) {
  $url     = "$ApiBase$($r.Path)"
  $outPath = Join-Path $EvidenceDir $r.File

  Write-Host "Fetching $($r.Id) from $url"

  try {
    $resp    = Invoke-WebRequest -Uri $url -UseBasicParsing -ErrorAction Stop
    $bodyRaw = $resp.Content
  }
  catch {
    Write-Host "!! Failed to fetch $url : $($_.Exception.Message)" -ForegroundColor Red
    continue
  }

  # Normalise line endings and remove completely empty lines
  $lines = ($bodyRaw -replace "`r","") -split "`n"
  $lines = $lines | Where-Object { $_.Trim() -ne "" }
  $body  = [string]::Join("`n", $lines)

  # Ensure directory exists
  $dir = Split-Path -Parent $outPath
  if (-not (Test-Path $dir)) {
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
  }

  $body | Set-Content -Path $outPath -Encoding UTF8
  Write-Host "Saved -> $outPath" -ForegroundColor Green
}
