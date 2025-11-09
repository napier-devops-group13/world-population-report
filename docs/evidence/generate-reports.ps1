# docs/evidence/generate-reports.ps1

function Save-ReportCsv {
    param(
        [Parameter(Mandatory = $true)][string]$Url,
        [Parameter(Mandatory = $true)][string]$Path
    )

    Write-Host "Fetching $Url" -ForegroundColor Cyan

    try {
        # Call the API and get the raw response text
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -ErrorAction Stop
    }
    catch {
        Write-Error "Cannot reach $Url. Is the app running on port 7000?"
        return
    }

    $text = $response.Content
    $csvOutput = $null

    try {
        # Try to treat the response as JSON (if your API returns JSON)
        $json = $text | ConvertFrom-Json

        if ($json) {
            if ($json -isnot [System.Array]) {
                $json = @($json)
            }

            if ($null -ne $json[0].code -and $null -ne $json[0].population) {
                $csvOutput = $json |
                    Select-Object code, name, region, population |
                    ConvertTo-Csv -NoTypeInformation
            } else {
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

    $dir = Split-Path -Parent $Path
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }

    $csvOutput | Set-Content -Path $Path -Encoding UTF8

    Write-Host "Saved -> $Path" -ForegroundColor Green
}

# -----------------------------
# R01–R06 evidence CSVs
# -----------------------------

# R01 – all countries in the world
Save-ReportCsv "$base/countries/world"                     "docs/evidence/R01_world.csv"

# R02 – all countries in Asia
Save-ReportCsv "$base/countries/continent/Asia"            "docs/evidence/R02_continent_Asia.csv"

# R03 – all countries in Eastern Asia
Save-ReportCsv "$base/countries/region/Eastern%20Asia"     "docs/evidence/R03_region_EasternAsia.csv"

# R04 – top-5 countries in the world
Save-ReportCsv "$base/countries/world/top/5"               "docs/evidence/R04_world_top5.csv"

# R05 – top-5 countries in Asia
Save-ReportCsv "$base/countries/continent/Asia/top/5"      "docs/evidence/R05_continent_Asia_top5.csv"

# R06 – top-5 countries in Eastern Asia
Save-ReportCsv "$base/countries/region/Eastern%20Asia/top/5" `
    "docs/evidence/R06_region_EasternAsia_top5.csv"
