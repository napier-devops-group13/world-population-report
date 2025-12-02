# World Population Report (SET09803 – Group 13)

<p align="left">
  <!-- CI (GitFlow branches) -->
  <a href="https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml?query=branch%3Amaster">
    <img alt="CI (master)" src="https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml/badge.svg?branch=master" />
  </a>
  <a href="https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml?query=branch%3Adevelop">
    <img alt="CI (develop)" src="https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml/badge.svg?branch=develop" />
  </a>

  <!-- Coverage (master) -->
  <a href="https://app.codecov.io/gh/napier-devops-group13/world-population-report">
    <img alt="Coverage (master)" src="https://codecov.io/gh/napier-devops-group13/world-population-report/branch/master/graph/badge.svg" />
  </a>

  <!-- Release + License + Tech -->
  <a href="https://github.com/napier-devops-group13/world-population-report/releases/latest">
    <img
      alt="Latest release"
      src="https://img.shields.io/github/v/release/napier-devops-group13/world-population-report?label=release&sort=semver"
    >
  </a>


  <a href="LICENSE">
    <img alt="License: MIT" src="https://img.shields.io/badge/License-MIT-yellow.svg" />
  </a>
  <img alt="JDK 21+" src="https://img.shields.io/badge/JDK-21%2B-blue" />
  <img alt="Docker Compose" src="https://img.shields.io/badge/Docker-Compose-green" />
</p>



REST API coursework for **SET09803** using the classic MySQL **`world`** dataset.  
The project is built to be **CI-friendly**, **Dockerised**, and aligned with the **Code Review 1**, **Code Review 2**, and **Final Delivery** marking criteria.

Currently the project:

- Implements the **Country Reports R01–R06** end-to-end (SQL ➜ Javalin ➜ JSON ➜ CSV evidence).
- Provides a reproducible stack using **Docker Compose** (MySQL 8.4 + app).
- Runs via **Maven** with unit + integration tests, JaCoCo coverage, Checkstyle and SpotBugs.
- Integrates with **GitHub Actions** (build, tests, coverage upload, Docker smoke test).
- Uses a **GitFlow**-style workflow (`master`, `develop`, `release/*`) with PR templates, issue templates, CODEOWNERS and a Code of Conduct.

---

## Table of Contents

- [Quick Start](#quick-start)
- [API Endpoints — Countries (R01–R06)](#api-endpoints--countries-r01r06)
- [Testing & Coverage](#testing-&-coverage)
- [Report Evidence for R01–R06](#report-evidence-for-r01r06)
- [Database & Seeding](#database--seeding)
- [Project Structure](#project-structure)
- [Quality & CI/CD](#quality--cicd)
- [Functional Requirements (R01–R32)](#functional-requirements-r01r32)
- [Assessment Evidence (CR1 / CR2 / Final)](#assessment-evidence-cr1--cr2--final)
- [Team](#team)
- [License](#license)

---

## Quick Start

> **Requirements:** JDK 21+, Maven 3.9+, Docker Desktop

### Full stack via Docker Compose (recommended for demo)

```bash
# from repo root
docker compose up -d
# db  -> MySQL 8.4 with 'world' schema
# app -> Javalin API on http://localhost:7070
```
---

## Testing & Coverage

We separate fast unit tests from integration tests.

### Unit tests

- `AppRoutesTest` exercises all HTTP routes in `App` using an in-memory `FakeCountryRepo`.
  - Introduced a `CountryRepository` interface for the country reports (R01–R06).
  - `WorldRepo` implements this interface in production.
  - The test verifies:
    - `/health` returns `200 OK`.
    - All `/countries/...` and `/countries/.../top/{n}` routes return JSON.
    - `?sort=pop` selects the population-sorted variants.
    - Invalid `n` (`0` or non-numeric) returns HTTP `400` with a clear JSON error.
    - Unexpected failures in the repository are mapped to HTTP `500` (`internal server error`).

Other unit tests:
- `ComparatorsTest` – checks sorting behaviour for country reports.

These tests run with:

```bash
mvn test
```
---

## API Endpoints – Population Lookup & Languages (R27–R32)

**Base URL (Reports):**

- Local JVM (IntelliJ / `java -jar`): `http://localhost:7070/reports`
- Docker (`docker-compose up -d`): `http://localhost:7080/reports`

There are two families of endpoints:

1. **Lookup reports (R27–R31)** – return a **single row** with:

  - `Name` – continent / region / country / district / city
  - `Population` – total population for that place

   > CSV header: `Name,Population`  
   > The row is omitted if the name is not found.

2. **Language report (R32)** – returns one row per language with:

  - `Language` – language name (Chinese, English, Hindi, Spanish, Arabic)
  - `Speakers` – number of speakers calculated from the `world` database
  - `WorldPopulationPercent` – percentage of world population speaking that language

   > CSV header: `Language,Speakers,WorldPopulationPercent`  
   > Rows are sorted by **Speakers DESC**.

### Path-parameter endpoints

| ID  | Method | Endpoint                                | Description                                        |
|-----|--------|-----------------------------------------|----------------------------------------------------|
| R27 | GET    | `/population/continents/{continent}`    | Population of a **continent** (e.g. `Asia`).       |
| R28 | GET    | `/population/regions/{region}`          | Population of a **region** (e.g. `Caribbean`).     |
| R29 | GET    | `/population/countries/{country}`       | Population of a **country** (e.g. `Myanmar`).      |
| R30 | GET    | `/population/districts/{district}`      | Population of a **district** (e.g. `Rangoon [Yangon]`). |
| R31 | GET    | `/population/cities/{city}`             | Population of a **city** (e.g. `Rangoon (Yangon)`). |
| R32 | GET    | `/population/languages`                 | Speaker counts and world % for key **languages**.  |

### Query-parameter aliases (used by scripts)

For the first five lookups there are convenience aliases that use a `name` query parameter:

| ID  | Method | Endpoint                          | Example                                            |
|-----|--------|-----------------------------------|----------------------------------------------------|
| R27 | GET    | `/population/continent?name=...`  | `/population/continent?name=Asia`                  |
| R28 | GET    | `/population/region?name=...`     | `/population/region?name=Caribbean`                |
| R29 | GET    | `/population/country?name=...`    | `/population/country?name=Myanmar`                 |
| R30 | GET    | `/population/district?name=...`   | `/population/district?name=Rangoon%20%5BYangon%5D` |
| R31 | GET    | `/population/city?name=...`       | `/population/city?name=Rangoon%20%28Yangon%29`     |

**Notes:**

- All endpoints are **read-only**.
- Missing or blank `name` values are handled safely and return an `"unknown …"` row with population `0`.
- These endpoints implement functional requirements **R27–R32**.
- The evidence scripts use them as follows:
  - `docs/evidence/generate-population-lookup-reports.ps1` – downloads R27–R32 CSV files.
  - `docs/evidence/verify-population-lookup-reports.ps1` – compares the CSV files with live API output.

---


## Database & Seeding

- **Image:** `mysql:8.4`
- **Service name (compose):** `db`
- **Schema:** classic *world* dataset.
- **Seed file:** `db/init/01-world.sql` mounted into the container.

`docker-compose.yml` exposes:

- MySQL container port `3306` → host port `43306`.
- App container port `7070` → host port `7080` (used by the Docker smoke test and browser).

## Application database configuration

Defaults for local development (running the app directly from IntelliJ/Maven) are in  
`src/main/resources/application.properties`:

```properties
port=7070

DB_HOST=localhost
DB_PORT=43306
DB_NAME=world
DB_USER=app
DB_PASS=app
```


---

## Project Structure – Population Lookup & Language Reports (R27–R32)

| Path                                                                              | Purpose                                                                                                                                                                      |
|-----------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `src/main/java/com/group13/population/model/PopulationLookupRow.java`            | Simple domain model for lookup reports **R27–R31** – holds `name` and `population` with factory helpers (e.g. `of(..)`) used by repo, service and routes.                    |
| `src/main/java/com/group13/population/model/LanguagePopulationRow.java`          | Domain model for **R32** language statistics – stores `language`, `speakers` and `worldPopulationPercent`; includes `fromWorldTotal(..)` helper used by the repository.     |
| `src/main/java/com/group13/population/repo/PopulationRepo.java`                  | JDBC repository now extended to support **R27–R32**: `findWorldPopulation`, `findContinentPopulation`, `findRegionPopulation`, `findCountryPopulation`, `findDistrictPopulation`, `findCityPopulation` and `findLanguagePopulations`. Uses safe fallbacks when the DB is unavailable. |
| `src/main/java/com/group13/population/service/PopulationService.java`            | Service layer that wraps `PopulationRepo` and exposes methods for all lookup + language reports: `getWorldPopulation()`, `getContinentPopulation(..)`, `getRegionPopulation(..)`, `getCountryPopulation(..)`, `getDistrictPopulation(..)`, `getCityPopulation(..)` and `getLanguagePopulations()`. |
| `src/main/java/com/group13/population/web/PopulationRoutes.java`                 | Javalin routes for **R27–R32** under `/reports/population/...` (continents, regions, countries, districts, cities and languages). Contains CSV builders for lookup and language reports used by the PowerShell evidence scripts. |
| `src/test/java/com/group13/population/model/PopulationLookupRowTest.java`        | Unit tests for `PopulationLookupRow` covering construction, getters and equality, ensuring it behaves correctly in lookup reports.                                           |
| `src/test/java/com/group13/population/model/LanguagePopulationRowTest.java`      | Unit tests for `LanguagePopulationRow`, especially the `fromWorldTotal(..)` helper and percentage rounding used in **R32**.                                                 |
| `src/test/java/com/group13/population/repo/PopulationRepoLookupAndLanguageTest.java` | Focused tests for `PopulationRepo` lookup + language methods – verifies SQL mapping and that continent / region / country / district / city and language rows are calculated correctly. |
| `src/test/java/com/group13/population/repo/PopulationRepoNoRowsTest.java`        | Ensures that when lookup queries return **no rows**, the repo still returns safe objects or empty lists (header-only CSV with zero counts).                                 |
| `src/test/java/com/group13/population/repo/PopulationRepoGuardTest.java`         | Guard tests for lookup and language methods: checks behaviour when `Db.getConnection()` throws or returns `null`, and when names are missing (e.g. “unknown continent”).    |
| `src/test/java/com/group13/population/web/PopulationRoutesTest.java`             | Route + helper tests for **R24–R32**, including `buildLookupCsv(..)` and `buildLanguageCsv(..)`, plus HTTP tests that all lookup endpoints return CSV with the correct headers and rows. |
| `docs/evidence/generate-population-lookup-reports.ps1`                           | PowerShell script that calls `/reports/population/continents/...`, `/regions/...`, `/countries/...`, `/districts/...`, `/cities/...` and `/languages` and saves CSV files for **R27–R32**. |
| `docs/evidence/verify-population-lookup-reports.ps1`                             | Verification script that re-runs the lookup + language APIs and compares them with the stored CSV evidence to ensure **R27–R32** still produce the expected results.        |
| `docs/evidence/R27_population_continent_Asia.csv`                                | Captured CSV output for **R27** – population of the continent *Asia*.                                                                                                       |
| `docs/evidence/R28_population_region_Caribbean.csv`                              | Captured CSV output for **R28** – population of the region *Caribbean*.                                                                                                     |
| `docs/evidence/R29_population_country_Myanmar.csv`                               | Captured CSV output for **R29** – population of the country *Myanmar*.                                                                                                      |
| `docs/evidence/R30_population_district_Rangoon.csv`                              | Captured CSV output for **R30** – total population of the district *Rangoon [Yangon]*.                                                                                      |
| `docs/evidence/R31_population_city_Rangoon_Yangon.csv`                           | Captured CSV output for **R31** – population of the city *Rangoon (Yangon)* (summing duplicate city names if needed).                                                      |
| `docs/evidence/R32_language_populations.csv`                                     | Captured CSV output for **R32** – Chinese, English, Hindi, Spanish and Arabic speaker counts plus percentage of world population.                                          |
| `docs/evidence/R27_*.png … R32_*.png`                                            | Screenshot evidence for each lookup / language CSV (R27–R32), used in the coursework submission and linked from the functional-requirements table.                         |

---





## Quality & CI/CD

- **CI triggers on:** pushes and PRs to `master`, `develop`, and `release/*`.

- **Build & test:**
  - Uses a Temurin JDK in CI (Maven compiler targets Java 21).
  - Spins up a MySQL service and seeds the `world` database from `db/init/01-world.sql`.
  - Runs unit tests and integration tests (`mvn verify` with Surefire + Failsafe).
  - Publishes the shaded JAR from `target/world-population-report-*-shaded.jar` as an artifact.

- **Coverage:**
  - JaCoCo runs as part of the Maven build.
  - Coverage is uploaded to **Codecov**, which feeds the coverage badge in the README.

- **Static analysis:**
  - Checkstyle (Google-style configuration) and SpotBugs run as part of the Maven build.

- **Docker smoke test:**
  - Builds the Docker image for the app.
  - Runs the container alongside a MySQL service (same configuration as `docker-compose.yml`).
  - Verifies `GET /api/countries/world` on the running container returns HTTP `200`.


---

## Functional Requirements (R01–R32)

### Summary of the coursework functional requirements and current implementation status

> **Count:** 15 / 32 requirements implemented  
> → 6 **Country** reports **R01–R06** + 9 **Population / Lookup / Language** reports **R24–R32** → **46.88%**.

| ID  | Name                                                                                           | Met   | Screenshot                                                                                                                   | CSV file                                                                                  |
|-----|------------------------------------------------------------------------------------------------|:-----:|------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| R01 | All the countries in the world organised by largest population to smallest.                    | ✅ Yes | <img src="docs/evidence/R01_countries_world.png" alt="R01 screenshot" width="300" />                                         | [R01_countries_world.csv](docs/evidence/R01_countries_world.csv)                         |
| R02 | All the countries in a continent organised by largest population to smallest.                  | ✅ Yes | <img src="docs/evidence/R02_countries_continent_Asia.png" alt="R02 screenshot" width="300" />                                | [R02_countries_continent_Asia.csv](docs/evidence/R02_countries_continent_Asia.csv)       |
| R03 | All the countries in a region organised by largest population to smallest.                     | ✅ Yes | <img src="docs/evidence/R03_countries_region_WesternEurope.png" alt="R03 screenshot" width="300" />                          | [R03_countries_region_WesternEurope.csv](docs/evidence/R03_countries_region_WesternEurope.csv) |
| R04 | The top N populated countries in the world where N is provided by the user.                    | ✅ Yes | <img src="docs/evidence/R04_countries_world_top10.png" alt="R04 screenshot" width="300" />                                   | [R04_countries_world_top10.csv](docs/evidence/R04_countries_world_top10.csv)             |
| R05 | The top N populated countries in a continent where N is provided by the user.                  | ✅ Yes | <img src="docs/evidence/R05_countries_continent_Europe_top5.png" alt="R05 screenshot" width="300" />                         | [R05_countries_continent_Europe_top5.csv](docs/evidence/R05_countries_continent_Europe_top5.csv) |
| R06 | The top N populated countries in a region where N is provided by the user.                     | ✅ Yes | <img src="docs/evidence/R06_countries_region_WesternEurope_top3.png" alt="R06 screenshot" width="300" />                     | [R06_countries_region_WesternEurope_top3.csv](docs/evidence/R06_countries_region_WesternEurope_top3.csv) |
| R07 | All the cities in the world organised by largest population to smallest.                       | ❌ No  | –                                                                                                                            | –                                                                                         |
| R08 | All the cities in a continent organised by largest population to smallest.                     | ❌ No  | –                                                                                                                            | –                                                                                         |
| R09 | All the cities in a region organised by largest population to smallest.                        | ❌ No  | –                                                                                                                            | –                                                                                         |
| R10 | All the cities in a country organised by largest population to smallest.                       | ❌ No  | –                                                                                                                            | –                                                                                         |
| R11 | All the cities in a district organised by largest population to smallest.                      | ❌ No  | –                                                                                                                            | –                                                                                         |
| R12 | The top N populated cities in the world where N is provided by the user.                       | ❌ No  | –                                                                                                                            | –                                                                                         |
| R13 | The top N populated cities in a continent where N is provided by the user.                     | ❌ No  | –                                                                                                                            | –                                                                                         |
| R14 | The top N populated cities in a region where N is provided by the user.                        | ❌ No  | –                                                                                                                            | –                                                                                         |
| R15 | The top N populated cities in a country where N is provided by the user.                       | ❌ No  | –                                                                                                                            | –                                                                                         |
| R16 | The top N populated cities in a district where N is provided by the user.                      | ❌ No  | –                                                                                                                            | –                                                                                         |
| R17 | All the capital cities in the world organised by largest population to smallest.               | ❌ No  | –                                                                                                                            | –                                                                                         |
| R18 | All the capital cities in a continent organised by largest population to smallest.             | ❌ No  | –                                                                                                                            | –                                                                                         |
| R19 | All the capital cities in a region organised by largest population to smallest.                | ❌ No  | –                                                                                                                            | –                                                                                         |
| R20 | The top N populated capital cities in the world where N is provided by the user.               | ❌ No  | –                                                                                                                            | –                                                                                         |
| R21 | The top N populated capital cities in a continent where N is provided by the user.             | ❌ No  | –                                                                                                                            | –                                                                                         |
| R22 | The top N populated capital cities in a region where N is provided by the user.                | ❌ No  | –                                                                                                                            | –                                                                                         |
| R23 | Population of people, in cities and not in cities, for each continent.                         | ❌ No  | –                                                                                                                            | –                                                                                         |
| R24 | Population of people, in cities and not in cities, for each region.                            | ✅ Yes | <img src="docs/evidence/R24_population_regions.png" alt="R24 screenshot" width="300" />                                      | [R24_population_regions.csv](docs/evidence/R24_population_regions.csv)                   |
| R25 | Population of people, in cities and not in cities, for each country.                           | ✅ Yes | <img src="docs/evidence/R25_population_countries.png" alt="R25 screenshot" width="300" />                                    | [R25_population_countries.csv](docs/evidence/R25_population_countries.csv)               |
| R26 | The population of the world.                                                                   | ✅ Yes | <img src="docs/evidence/R26_population_world.png" alt="R26 screenshot" width="300" />                                        | [R26_population_world.csv](docs/evidence/R26_population_world.csv)                       |
| R27 | The population of a continent.                                                                 | ✅ Yes | <img src="docs/evidence/R27_population_continent_Asia.png" alt="R27 screenshot" width="300" />                               | [R27_population_continent_Asia.csv](docs/evidence/R27_population_continent_Asia.csv)     |
| R28 | The population of a region.                                                                    | ✅ Yes | <img src="docs/evidence/R28_population_region_Caribbean.png" alt="R28 screenshot" width="300" />                             | [R28_population_region_Caribbean.csv](docs/evidence/R28_population_region_Caribbean.csv) |
| R29 | The population of a country.                                                                   | ✅ Yes | <img src="docs/evidence/R29_population_country_Myanmar.png" alt="R29 screenshot" width="300" />                              | [R29_population_country_Myanmar.csv](docs/evidence/R29_population_country_Myanmar.csv)   |
| R30 | The population of a district.                                                                  | ✅ Yes | <img src="docs/evidence/R30_population_district_Rangoon.png" alt="R30 screenshot" width="300" />                             | [R30_population_district_Rangoon.csv](docs/evidence/R30_population_district_Rangoon.csv) |
| R31 | The population of a city.                                                                      | ✅ Yes | <img src="docs/evidence/R31_population_city_Rangoon_Yangon.png" alt="R31 screenshot" width="300" />                          | [R31_population_city_Rangoon_Yangon.csv](docs/evidence/R31_population_city_Rangoon_Yangon.csv) |
| R32 | Number of people who speak Chinese, English, Hindi, Spanish, and Arabic, with world % shares. | ✅ Yes | <img src="docs/evidence/R32_language_populations.png" alt="R32 screenshot" width="300" />                                    | [R32_language_populations.csv](docs/evidence/R32_language_populations.csv)               |

---



## Assessment Evidence (CR1 / CR2 / Final)

### CR1 — Checklist

| ID | Criterion (Rubric)                                  | Met | Evidence (path / note)                                      |
|----|-----------------------------------------------------|-----|-------------------------------------------------------------|
| 1  | GitHub project set-up                               | ✅  | Screenshot of repo root in `docs/evidence/`                 |
| 2  | Product Backlog created                             | ✅  | Issues / project board screenshot                           |
| 3  | Builds to self-contained JAR with Maven             | ✅  | `target/world-population-report.jar`                        |
| 4  | Dockerfile set-up and works                         | ✅  | `docker-compose.yml` + compose PowerShell output            |
| 5  | GitHub Actions build using JAR & Docker             | ✅  | CI run screenshot                                           |
| 6  | GitFlow branches (`master`, `develop`, `release/*`) | ✅  | Branches screenshot                                         |
| 7  | First release created                               | ✅  | GitHub Releases screenshot                                  |
| 8  | Code of Conduct defined                             | ✅  | `CODE_OF_CONDUCT.md`                                        |
| 9  | Issues used on GitHub                               | ✅  | Issues / backlog screenshot                                 |
| 10 | Tasks defined as user stories                       | ✅  | Example user-story issues                                   |
| 11 | Project integrated with Zube.io                     | ✅  | Zube board screenshot                                       |
| 12 | Kanban / Project Board being used                   | ✅  | GitHub Project board screenshot                             |
| 13 | Sprint Boards being used                            | ✅  | Iteration / sprint view screenshot                          |
| 14 | Full use cases defined                              | ✅  | `docs/use-cases`                                            |
| 15 | Use case diagram created                            | ✅  | UML diagram in `docs/uml`                                   |


---
### CR2 — Graded Criteria

| ID | Criterion (Rubric)                                           | Met / Value | Evidence (path / note)                                  |
|----|--------------------------------------------------------------|-------------|---------------------------------------------------------|
| 1  | Quality & coverage of unit tests                             | ✅ (see % in JaCoCo) | JaCoCo report screenshot                           |
| 2  | Suitable integration tests defined                           | ✅           | `CountriesIT` passing, CI logs                          |
| 3  | Continuous integration running tests (GitHub Actions)        | ✅           | CI pipeline status                                      |
| 4  | Deployment working (Docker run + smoke)                      | ✅           | `docker compose up` + `curl` smoke-test screenshot      |
| 5  | Bug reporting system set-up                                  | ✅           | Issue / label screenshots                               |
| 6  | Badges (build master/develop, coverage, release, license)    | ✅           | This README header                                      |
| 7  | Project Requirements Met (R01–R32)                            | **6 / 32**   | See **Functional Requirements** section                 |
| 8  | Correct use of GitHub & Kanban; commit frequency; comments   | ✅           | Insights / commit history screenshots                   |


---
### Final — Delivery Checklist

| ID | Item (assessed on master)                                        | Met | Evidence (path / note)                          |
|----|------------------------------------------------------------------|-----|-------------------------------------------------|
| 1  | README contains all required badges                              | ✅  | This file                                       |
| 2  | Evidence table for requirements R01–R32 present                  | ✅  | See **Functional Requirements** section         |
| 3  | Master branch contains assessable code (builds & runs)           | ✅  | `mvn test` + manual run                         |
| 4  | Final release/tag created                                        | ✅  | GitHub Releases                                 |
| 5  | Individual contribution spreadsheet submitted (CR1 & CR2)        | ✅  | Submitted separately via Moodle                 |


---
## Team

This is a **Group 13** submission for **SET09803**.

The full team roster (names, student numbers and GitHub usernames) is listed in
[`TEAM.md`](TEAM.md).

> Individual contribution percentages for **Code Review 1 (CR1)**,  
> **Code Review 2 (CR2)** and the **Final Delivery** are recorded in the
> official contribution spreadsheets submitted via Moodle, as required by the
> module handbook. GitHub commit and pull-request history provides additional
> evidence of each member’s contribution.

---
## License

This project is released under the **MIT License**.

See `LICENSE` for details.
---

