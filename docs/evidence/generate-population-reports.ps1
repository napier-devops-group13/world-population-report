# docs/evidence/generate-population-reports.ps1
# Generate CSV evidence files for R24–R26 from the running API.
#
# Population endpoints are exposed at:
#   /reports/population/regions
#   /reports/population/countries
#   /reports/population/world
#
# Docker:   http://localhost:7080
# IntelliJ: http://localhost:7070

param(
  [string]$ApiBase = "http://localhost:7080"
)

$ErrorActionPreference = "Stop"

# Folder where this script lives (...\world-population-report\docs\evidence)
$EvidenceDir = $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($EvidenceDir)) {
  $EvidenceDir = (Get-Location).Path
}

$reports = @(
  @{ Id = "R24"; File = "R24_population_regions.csv";   Path = "/reports/population/regions"   },
  @{ Id = "R25"; File = "R25_population_countries.csv"; Path = "/reports/population/countries" },
  @{ Id = "R26"; File = "R26_population_world.csv";     Path = "/reports/population/world"     }
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
