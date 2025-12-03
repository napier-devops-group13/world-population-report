# docs/evidence/generate-capital-reports.ps1
# Generate CSV evidence files for R17–R22 from the running API.

param(
  [string]$ApiBase = "http://localhost:7080/api"
)

# Directory where this script lives = docs/evidence
$EvidenceDir = $PSScriptRoot

$reports = @(
  @{ Id = "R17"; Path = "/capitals/world";                  File = "R17_capitals_world.csv" }
  @{ Id = "R18"; Path = "/capitals/continent/Europe";       File = "R18_capitals_continent_Europe.csv" }
  @{ Id = "R19"; Path = "/capitals/region/Caribbean";       File = "R19_capitals_region_Caribbean.csv" }
  @{ Id = "R20"; Path = "/capitals/world/top/10";           File = "R20_capitals_world_top10.csv" }
  @{ Id = "R21"; Path = "/capitals/continent/Europe/top/5"; File = "R21_capitals_continent_Europe_top5.csv" }
  @{ Id = "R22"; Path = "/capitals/region/Caribbean/top/3"; File = "R22_capitals_region_Caribbean_top3.csv" }
)

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

  # Normalise newlines and drop completely empty lines
  $lines = ($bodyRaw -replace "`r","") -split "`n"
  $lines = $lines | Where-Object { $_.Trim() -ne "" }
  $body  = [string]::Join("`n", $lines)

  # Ensure docs/evidence exists
  $dir = Split-Path -Parent $outPath
  if (-not (Test-Path $dir)) {
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
  }

  $body | Set-Content -Path $outPath -Encoding UTF8
  Write-Host "Saved -> $outPath" -ForegroundColor Green
}
