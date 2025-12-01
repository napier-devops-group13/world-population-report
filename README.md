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

## API Endpoints – Population in / out of cities (R24–R26)

**Base URL (Reports):**

- Local JVM (IntelliJ / `java -jar`): `http://localhost:7070/reports`
- Docker (`docker-compose up -d`): `http://localhost:7080/reports`

All endpoints return **population report rows** with fields:

- `name` – region / country name, or `World`
- `total_population` – total population for that region / country / world
- `in_cities` – number of people living in cities
- `not_in_cities` – number of people not living in cities
- `percent_in_cities` – percentage of people living in cities
- `percent_not_in_cities` – percentage of people not living in cities

> All responses are CSV files with a header row  
> `Name,TotalPopulation,InCities,NotInCities,PercentInCities,PercentNotInCities`  
> and are sorted by **total_population DESC**.

| ID  | Method | Endpoint                     | Description                                                                                             |
|-----|--------|------------------------------|---------------------------------------------------------------------------------------------------------|
| R24 | GET    | `/population/regions`        | Population living **in / not in cities** for each **region**, including totals and percentages.        |
| R25 | GET    | `/population/countries`      | Population living **in / not in cities** for each **country**, including totals and percentages.       |
| R26 | GET    | `/population/world`          | **World** population: people living in cities vs not in cities, totals and percentages (single row).   |

**Notes:**

- These endpoints are **read-only** and do not take any parameters.
- They correspond directly to functional requirements **R24–R26** in the coursework.
- The evidence scripts use them as follows:
  - `docs/evidence/generate-population-reports.ps1` – downloads R24–R26 CSV files.
  - `docs/evidence/verify-population-reports.ps1` – compares the CSV files with live API output.

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

## Project Structure – Population Reports (R24–R26)

| Path                                                                  | Purpose                                                                                                             |
|-----------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| `src/main/java/com/group13/population/App.java`                       | Javalin bootstrap / `main` entry point; wires `Db` + `PopulationRepo` + `PopulationService` + `PopulationRoutes`, exposes `/health` and `/reports/...` endpoints. |
| `src/main/java/com/group13/population/db/Db.java`                     | MySQL JDBC helper – reads env vars, builds the JDBC URL, and exposes `connect(..)` / `getConnection()` used by the app and integration tests. |
| `src/main/java/com/group13/population/model/PopulationRow.java`      | Domain model for a single population report row (name, total population, in / not in cities, percentages).        |
| `src/main/java/com/group13/population/repo/PopulationRepo.java`      | JDBC repository with SQL queries and mappers for the three population reports **R24–R26** (regions, countries, world). |
| `src/main/java/com/group13/population/service/PopulationService.java`| Service layer: wraps `PopulationRepo`, applies light validation / transformation, and returns ordered population rows for R24–R26. |
| `src/main/java/com/group13/population/web/PopulationRoutes.java`     | CSV API endpoints for population reports under `/reports/population/...` (used by the evidence scripts for R24–R26). |
| `src/test/java/com/group13/population/AppHelpersTest.java`           | Unit tests for helper methods in `App` (property loading, environment parsing, startup delay handling, etc.).      |
| `src/test/java/com/group13/population/db/DbTest.java`                | Unit tests for `Db` (JDBC URL formatting, behaviour when no MySQL server is running, and reconnect logic).        |
| `src/test/java/com/group13/population/db/DbIT.java`                  | Integration test for `Db.connect(..)` against the real MySQL `world` database (Docker `world-db` on port 43306).  |
| `src/test/java/com/group13/population/model/PopulationRowTest.java`  | Unit tests for `PopulationRow` (factory methods, totals, and percentage calculations).                             |
| `src/test/java/com/group13/population/repo/PopulationRepoTest.java`  | Core unit tests for `PopulationRepo` using stub data, checking correct SQL mapping, ordering and totals for R24–R26. |
| `src/test/java/com/group13/population/repo/PopulationRepoGuardTest.java` | Guard tests ensuring `PopulationRepo` returns empty results / 0 when `Db.getConnection()` fails or is `null` (no crash). |
| `src/test/java/com/group13/population/repo/PopulationRepoNoRowsTest.java` | Tests behaviour when the underlying queries return **no rows** (CSV header only, counts stay at zero).           |
| `src/test/java/com/group13/population/repo/PopulationRepoEdgeCaseTest.java` | Extra edge-case checks for rounding of percentages, large populations and other boundary conditions.          |
| `src/test/java/com/group13/population/repo/PopulationRepoIT.java`    | Integration tests for `PopulationRepo` against the real DB (regions / countries / world queries for R24–R26).     |
| `src/test/java/com/group13/population/service/PopulationServiceTest.java` | Unit tests for `PopulationService` covering all three population service methods and their mapping to the repo. |
| `src/test/java/com/group13/population/web/AppConfigTest.java`        | Tests for web app configuration (wiring `Db` + repo + service + routes) without starting a full HTTP server.      |
| `src/test/java/com/group13/population/web/AppSmokeTest.java`         | Smoke test that starts the Javalin app on a random port, hits basic endpoints, and then shuts it down cleanly.    |
| `src/test/java/com/group13/population/web/PopulationRoutesTest.java` | Route tests for `/reports/population/...` including the CSV header / row structure and helper methods.            |
| `db/init/01-world.sql`                                               | Seed script for the MySQL `world` schema used by Docker and all integration tests (including R24–R26).            |
| `docs/evidence/generate-population-reports.ps1`                      | PowerShell script that calls `/reports/population/...` and saves CSV evidence files for **R24–R26**.              |
| `docs/evidence/verify-population-reports.ps1`                        | Script that re-runs the population APIs and compares them with the CSV files to confirm R24–R26 still match.      |
| `docs/evidence/R24_population_regions.csv`                           | Captured CSV output for **R24** – population in / not in cities by region.                                         |
| `docs/evidence/R25_population_countries.csv`                         | Captured CSV output for **R25** – population in / not in cities by country.                                        |
| `docs/evidence/R26_population_world.csv`                             | Captured CSV output for **R26** – world population in / not in cities (single summary row).                        |
| `docs/evidence/R24_*.png … R26_*.png`                                | Screenshot evidence for each population report CSV, used in the coursework submission.                             |

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

> **Count:** 9 / 32 requirements implemented  
> → 6 **Country** reports **R01–R06** + 3 **Population** reports **R24–R26** → **28.13%**.

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
| R27 | The population of a continent.                                                                 | ❌ No  | –                                                                                                                            | –                                                                                         |
| R28 | The population of a region.                                                                    | ❌ No  | –                                                                                                                            | –                                                                                         |
| R29 | The population of a country.                                                                   | ❌ No  | –                                                                                                                            | –                                                                                         |
| R30 | The population of a district.                                                                  | ❌ No  | –                                                                                                                            | –                                                                                         |
| R31 | The population of a city.                                                                      | ❌ No  | –                                                                                                                            | –                                                                                         |
| R32 | Number of people who speak Chinese, English, Hindi, Spanish, and Arabic, with world % shares. | ❌ No  | –                                                                                                                            | –                                                                                         |

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

