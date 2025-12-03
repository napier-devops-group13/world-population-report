# docs/evidence/generate-country-reports.ps1
# Generate CSV evidence files for R01–R06 from the running API.

param(
# Docker:  http://localhost:7080/api
# IntelliJ (run directly): http://localhost:7070/api
  [string]$ApiBase = "http://localhost:7080/api"
)

$ErrorActionPreference = "Stop"

# Folder where this script lives (...\world-population-report\docs\evidence)
$EvidenceDir = $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($EvidenceDir)) {
  $EvidenceDir = (Get-Location).Path
}

$reports = @(
  @{ Id = "R01"; File = "R01_countries_world.csv";                     Path = "/countries/world" },
  @{ Id = "R02"; File = "R02_countries_continent_Asia.csv";            Path = "/countries/continent/Asia" },
  @{ Id = "R03"; File = "R03_countries_region_WesternEurope.csv";      Path = "/countries/region/Western%20Europe" },
  @{ Id = "R04"; File = "R04_countries_world_top10.csv";               Path = "/countries/world/top?n=10" },
  @{ Id = "R05"; File = "R05_countries_continent_Europe_top5.csv";     Path = "/countries/continent/Europe/top?n=5" },
  @{ Id = "R06"; File = "R06_countries_region_WesternEurope_top3.csv"; Path = "/countries/region/Western%20Europe/top?n=3" }
)

# Give the app a few seconds if it was just started
Start-Sleep -Seconds 5

foreach ($r in $reports) {
  $url     = "$ApiBase$($r.Path)"
  $outPath = Join-Path $EvidenceDir $r.File

  Write-Host "Fetching $url"

  try {
    $resp = Invoke-WebRequest -Uri $url -UseBasicParsing -ErrorAction Stop
    $bodyRaw = $resp.Content
  }
  catch {
    Write-Host "!! Failed to fetch $url : $($_.Exception.Message)" -ForegroundColor Red
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
  Write-Host "Saved -> $outPath" -ForegroundColor Green
}
