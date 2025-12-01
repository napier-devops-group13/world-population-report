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

<<<<<<< HEAD
## API Endpoints – Countries (R01–R06)
=======
## API Endpoints – Capital Cities (R17–R22)
>>>>>>> develop

**Base URL:**

- Local JVM (IntelliJ / `java -jar`): `http://localhost:7070/api`
- Docker (`docker-compose up -d`): `http://localhost:7080/api`

<<<<<<< HEAD
Country report responses use the **Country report shape**:

- `code` – 3-letter country code (e.g. `GBR`)
- `name` – country name (e.g. `United Kingdom`)
- `continent` – continent name (e.g. `Europe`)
- `region` – region name (e.g. `Western Europe`)
- `population` – country population (integer)
- `capital` – capital city name, or `null` if no capital is set

| ID  | Method | Endpoint                                        | Description                                                                                      |
|-----|--------|-------------------------------------------------|--------------------------------------------------------------------------------------------------|
| R01 | GET    | `/countries/world`                             | All countries in the **world**, ordered by **population DESC**.                                  |
| R02 | GET    | `/countries/continent/{continent}`             | All countries in a **continent**, ordered by **population DESC**.                                |
| R03 | GET    | `/countries/region/{region}`                   | All countries in a **region**, ordered by **population DESC**.                                   |
| R04 | GET    | `/countries/world/top`                         | Top-N countries in the **world** by population (largest → smallest). Uses query param `?n={n}`.  |
| R05 | GET    | `/countries/continent/{continent}/top`         | Top-N countries in a **continent** by population. Uses query param `?n={n}`.                     |
| R06 | GET    | `/countries/region/{region}/top`               | Top-N countries in a **region** by population. Uses query param `?n={n}`.                        |

**Query parameter for Top-N endpoints:**

- `n` (required for R04–R06 when called externally; defaults to `10` if omitted in internal usage)
  - Example: `/countries/world/top?n=10`
  - Example: `/countries/continent/Europe/top?n=5`
  - Example: `/countries/region/Western%20Europe/top?n=3`
=======
All endpoints return **capital cities** with fields:

- `city` (capital name)
- `country` (country name)
- `population` (population of the capital city)

| ID  | Method | Endpoint                                       | Description                                                                                 |
|-----|--------|-------------------------------------------------|---------------------------------------------------------------------------------------------|
| R17 | GET    | `/capitals/world`                              | All capital cities in the world, ordered by **population DESC**.                           |
| R18 | GET    | `/capitals/continent/{continent}`              | All capital cities in a continent, ordered by **population DESC**.                         |
| R19 | GET    | `/capitals/region/{region}`                    | All capital cities in a region, ordered by **population DESC**.                            |
| R20 | GET    | `/capitals/world/top/{n}`                      | Top-N capital cities in the world by population (largest → smallest).                      |
| R21 | GET    | `/capitals/continent/{continent}/top/{n}`      | Top-N capital cities in a continent by population (largest → smallest).                    |
| R22 | GET    | `/capitals/region/{region}/top/{n}`            | Top-N capital cities in a region by population (largest → smallest).                       |
>>>>>>> develop

**Error handling (examples):**

- `n <= 0` → **HTTP 400** with a plain-text message  
  `n must be a positive integer`

<<<<<<< HEAD
- Unknown `continent` / `region` → **HTTP 200** with an empty JSON array `[]`  
  (no matching countries in the world database).
=======
- Non-integer `n` → **HTTP 400** with a plain-text message  
  `n must be an integer`

- Unknown `continent` / `region` → **HTTP 200** with an empty result  
  (no matching capital cities in the world database).
>>>>>>> develop

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

<<<<<<< HEAD
---

## Project Structure – Country Reports (R01–R06)

| Path                                                                    | Purpose                                                                                                          |
|-------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| `src/main/java/com/group13/population/App.java`                         | Javalin bootstrap; registers **country routes** and exposes `/health`. Uses helpers to read port and DB config. |
| `src/main/java/com/group13/population/db/Db.java`                       | MySQL JDBC helper used by `WorldRepo` and integration tests (builds JDBC URL and manages connections).          |
| `src/main/java/com/group13/population/model/CountryReport.java`        | Domain model for a full **country report** (Code, Name, Continent, Region, Population, Capital).                |
| `src/main/java/com/group13/population/model/CountryRow.java`           | Lightweight row/projection used by JDBC to map R01–R06 query results into immutable objects.                    |
| `src/main/java/com/group13/population/repo/WorldRepo.java`             | Repository class containing all SQL for **R01–R06** (world / continent / region + top-N queries).               |
| `src/main/java/com/group13/population/service/CountryService.java`     | Service layer: wraps `WorldRepo`, validates inputs (continent/region/n), and returns country report rows.       |
| `src/main/java/com/group13/population/web/CountryRoutes.java`          | REST endpoints under `/api/countries/...` implemented with Javalin for **R01–R06**.                             |
| `src/main/resources/app.properties`                                    | Application configuration (default DB host/port, database name, app port) used by `App` / `Db`.                 |
| `src/test/java/com/group13/population/db/DbTest.java`                  | Unit tests for `Db` (JDBC URL formatting, error handling).                                                      |
| `src/test/java/com/group13/population/db/DbIT.java`                    | Integration tests for `Db.connect(..)` against the real MySQL `world` database.                                 |
| `src/test/java/com/group13/population/model/CountryReportTest.java`    | Unit tests for `CountryReport` (constructor, getters, equality, `toString`).                                    |
| `src/test/java/com/group13/population/model/CountryRowTest.java`       | Unit tests for `CountryRow` (field mapping, null-safety, equality).                                             |
| `src/test/java/com/group13/population/repo/WorldRepoGuardTest.java`    | Unit tests checking guard behaviour in `WorldRepo` (e.g. invalid `n`, null continent/region).                   |
| `src/test/java/com/group13/population/repo/WorldRepoIT.java`           | Integration tests for `WorldRepo` against the real `world` schema (SQL correctness for R01–R06).                |
| `src/test/java/com/group13/population/service/CountryServiceTest.java` | Unit tests for `CountryService` covering all R01–R06 service methods and error cases.                           |
| `src/test/java/com/group13/population/web/CountryRoutesTest.java`      | Route tests calling `/api/countries/...` and asserting HTTP status codes and JSON **country report** shape.     |
| `src/test/java/com/group13/population/web/CountryReportsOrderingTest.java` | Extra checks that country reports are ordered by population DESC (and secondary ordering where needed).    |
| `src/test/java/com/group13/population/web/AppConfigTest.java`          | Verifies that `App` wiring (routes, config) works without starting a full server.                               |
| `src/test/java/com/group13/population/web/AppSmokeTest.java`           | Smoke test that the Javalin app starts and stops cleanly with all country routes registered.                    |
| `src/test/java/com/group13/population/AppHelpersTest.java`             | Tests helper methods used by `App` (env/property parsing, port handling, etc.).                                 |
| `db/init/01-world.sql`                                                 | Seed script for the MySQL `world` schema used by Docker and all integration tests.                              |
| `docs/evidence/R01_countries_world.*`                                  | CSV + screenshot evidence for **R01 – all countries in the world by population DESC**.                          |
| `docs/evidence/R02_countries_continent_Asia.*`                         | CSV + screenshot evidence for **R02 – all countries in a continent (Asia)**.                                    |
| `docs/evidence/R03_countries_region_WesternEurope.*`                   | CSV + screenshot evidence for **R03 – all countries in a region (Western Europe)**.                             |
| `docs/evidence/R04_countries_world_top10.*`                            | CSV + screenshot evidence for **R04 – top 10 countries in the world**.                                          |
| `docs/evidence/R05_countries_continent_Europe_top5.*`                  | CSV + screenshot evidence for **R05 – top 5 countries in Europe**.                                              |
| `docs/evidence/R06_countries_region_WesternEurope_top3.*`             | CSV + screenshot evidence for **R06 – top 3 countries in Western Europe**.                                      |
| `docs/evidence/generate-country-reports.ps1`                           | Helper script to call `/api/countries/...` and regenerate evidence CSV files for R01–R06.                       |
| `docs/evidence/verify-country-reports.ps1`                             | PowerShell helper that verifies R01–R06 endpoints respond correctly and match the saved evidence (port 7080).   |

=======
>>>>>>> develop

---

## Project Structure

| Path                                                                  | Purpose                                                                                              |
|-----------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| `src/main/java/com/group13/population/App.java`                       | Javalin bootstrap / `main` entry-point; wires DB + capital repo + service + routes and `/health`.   |
| `src/main/java/com/group13/population/db/Db.java`                     | MySQL JDBC helper – builds the JDBC URL and exposes `connect(..)` used by the app and tests.        |
| `src/main/java/com/group13/population/model/CapitalCityRow.java`     | Domain model for a single capital city row (city, country, population).                             |
| `src/main/java/com/group13/population/model/CapitalCityReport.java`  | Wrapper model for a named capital report plus its list of `CapitalCityRow` results.                 |
| `src/main/java/com/group13/population/model/CityRow.java`            | Lightweight model used for simple city name/population pairs in some capital endpoints.             |
| `src/main/java/com/group13/population/repo/CapitalRepo.java`         | JDBC repository with SQL queries and mappers for the six capital city reports (R17–R22).            |
| `src/main/java/com/group13/population/service/CapitalService.java`   | Service layer: wraps `CapitalRepo`, does light input validation, and returns report rows.           |
| `src/main/java/com/group13/population/web/CapitalRoutes.java`        | HTML / table endpoints for capital reports under `/capitals/...` (R17–R22).                         |
| `src/main/java/com/group13/population/web/CapitalApiRoutes.java`     | CSV API endpoints for capitals under `/api/capitals/...` used by the evidence scripts.              |
| `src/test/java/com/group13/population/AppHelpersTest.java`           | Unit tests for helper methods in `App` (property loading, env parsing, etc.).                       |
| `src/test/java/com/group13/population/db/DbTest.java`                | Unit tests for `Db` (JDBC URL formatting and behaviour when no server is running).                  |
| `src/test/java/com/group13/population/db/DbIT.java`                  | Integration test for `Db.connect(..)` against the real MySQL `world` database.                      |
| `src/test/java/com/group13/population/model/CapitalCityRowTest.java` | Unit tests for `CapitalCityRow` (constructor, getters, equals/hashCode, `toString`).                |
| `src/test/java/com/group13/population/model/CapitalCityReportTest.java` | Unit tests for `CapitalCityReport` (name + list handling, derived properties).                   |
| `src/test/java/com/group13/population/repo/CapitalRepoGuardTest.java`| Fast unit tests checking guard clauses / null handling in `CapitalRepo`.                            |
| `src/test/java/com/group13/population/repo/CapitalRepoIT.java`       | Integration tests for `CapitalRepo` against the real DB (filters, ordering, limits for R17–R22).   |
| `src/test/java/com/group13/population/service/CapitalServiceTest.java`| Unit tests for `CapitalService` covering all R17–R22 service methods.                               |
| `src/test/java/com/group13/population/web/AppConfigTest.java`        | Tests for `App.createApp(..)` and DB wiring without actually starting the HTTP server.              |
| `src/test/java/com/group13/population/web/AppSmokeTest.java`         | Smoke test that starts the Javalin app on a random port and then stops it cleanly.                  |
| `src/test/java/com/group13/population/web/CapitalApiRoutesTest.java` | Coverage-focused tests that hit `/api/capitals/...` endpoints using Javalin’s test tools.           |
| `src/test/java/com/group13/population/web/CapitalReportsOrderingTest.java` | Extra checks that capital report results are ordered by population DESC (and tie-breaks).     |
| `src/test/java/com/group13/population/web/CapitalRoutesTest.java`    | Route tests for the HTML capital endpoints under `/capitals/...`.                                   |
| `db/init/01-world.sql`                                               | Seed script for the MySQL `world` schema used by Docker and integration tests.                      |
| `docs/evidence/R17_*.csv, R18_*.csv, ... R22_*.csv`                  | Captured CSV outputs for the six capital city reports (R17–R22).                                    |
| `docs/evidence/R

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

### Summary of coursework functional requirements and current implementation status

<<<<<<< HEAD
> **Count:** 6 / 32 requirements implemented so far – all **Country** reports **R01–R06**.  
> City (R07–R16), Capital City (R17–R22), Population (R23–R31) and Language (R32) reports are planned for later iterations and will be added to this table as they are implemented.

| ID  | Name                                                                                           |  Met   | Screenshot                                                                                                                     | CSV file                                                                                  |
|-----|------------------------------------------------------------------------------------------------|:------:|--------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| R01 | All the countries in the world organised by largest population to smallest.                    | ✅ Yes | <img src="docs/evidence/R01_countries_world.png" alt="R01 countries world" width="300" />                                     | [R01_countries_world.csv](docs/evidence/R01_countries_world.csv)                         |
| R02 | All the countries in a continent organised by largest population to smallest.                  | ✅ Yes | <img src="docs/evidence/R02_countries_continent_Asia.png" alt="R02 countries continent Asia" width="220" />                  | [R02_countries_continent_Asia.csv](docs/evidence/R02_countries_continent_Asia.csv)       |
| R03 | All the countries in a region organised by largest population to smallest.                     | ✅ Yes | <img src="docs/evidence/R03_countries_region_WesternEurope.png" alt="R03 countries region Western Europe" width="220" />     | [R03_countries_region_WesternEurope.csv](docs/evidence/R03_countries_region_WesternEurope.csv) |
| R04 | The top N populated countries in the world where N is provided by the user.                    | ✅ Yes | <img src="docs/evidence/R04_countries_world_top10.png" alt="R04 top 10 countries world" width="300" />                       | [R04_countries_world_top10.csv](docs/evidence/R04_countries_world_top10.csv)             |
| R05 | The top N populated countries in a continent where N is provided by the user.                  | ✅ Yes | <img src="docs/evidence/R05_countries_continent_Europe_top5.png" alt="R05 top 5 countries Europe" width="220" />             | [R05_countries_continent_Europe_top5.csv](docs/evidence/R05_countries_continent_Europe_top5.csv) |
| R06 | The top N populated countries in a region where N is provided by the user.                     | ✅ Yes | <img src="docs/evidence/R06_countries_region_WesternEurope_top3.png" alt="R06 top 3 countries Western Europe" width="220" /> | [R06_countries_region_WesternEurope_top3.csv](docs/evidence/R06_countries_region_WesternEurope_top3.csv) |
| R07 | All the cities in the world organised by largest population to smallest.                       | ❌ No  | –                                                                                                                              | –                                                                                         |
| R08 | All the cities in a continent organised by largest population to smallest.                     | ❌ No  | –                                                                                                                              | –                                                                                         |
| R09 | All the cities in a region organised by largest population to smallest.                        | ❌ No  | –                                                                                                                              | –                                                                                         |
| R10 | All the cities in a country organised by largest population to smallest.                       | ❌ No  | –                                                                                                                              | –                                                                                         |
| R11 | All the cities in a district organised by largest population to smallest.                      | ❌ No  | –                                                                                                                              | –                                                                                         |
| R12 | The top N populated cities in the world where N is provided by the user.                       | ❌ No  | –                                                                                                                              | –                                                                                         |
| R13 | The top N populated cities in a continent where N is provided by the user.                     | ❌ No  | –                                                                                                                              | –                                                                                         |
| R14 | The top N populated cities in a region where N is provided by the user.                        | ❌ No  | –                                                                                                                              | –                                                                                         |
| R15 | The top N populated cities in a country where N is provided by the user.                       | ❌ No  | –                                                                                                                              | –                                                                                         |
| R16 | The top N populated cities in a district where N is provided by the user.                      | ❌ No  | –                                                                                                                              | –                                                                                         |
| R17 | All the capital cities in the world organised by largest population to smallest.               | ❌ No  | –                                                                                                                              | –                                                                                         |
| R18 | All the capital cities in a continent organised by largest population to smallest.             | ❌ No  | –                                                                                                                              | –                                                                                         |
| R19 | All the capital cities in a region organised by largest population to smallest.                | ❌ No  | –                                                                                                                              | –                                                                                         |
| R20 | The top N populated capital cities in the world where N is provided by the user.               | ❌ No  | –                                                                                                                              | –                                                                                         |
| R21 | The top N populated capital cities in a continent where N is provided by the user.             | ❌ No  | –                                                                                                                              | –                                                                                         |
| R22 | The top N populated capital cities in a region where N is provided by the user.                | ❌ No  | –                                                                                                                              | –                                                                                         |
| R23 | Population of people, in cities and not in cities, for each continent.                         | ❌ No  | –                                                                                                                              | –                                                                                         |
| R24 | Population of people, in cities and not in cities, for each region.                            | ❌ No  | –                                                                                                                              | –                                                                                         |
| R25 | Population of people, in cities and not in cities, for each country.                           | ❌ No  | –                                                                                                                              | –                                                                                         |
| R26 | The population of the world.                                                                    | ❌ No  | –                                                                                                                              | –                                                                                         |
| R27 | The population of a continent.                                                                  | ❌ No  | –                                                                                                                              | –                                                                                         |
| R28 | The population of a region.                                                                     | ❌ No  | –                                                                                                                              | –                                                                                         |
| R29 | The population of a country.                                                                    | ❌ No  | –                                                                                                                              | –                                                                                         |
| R30 | The population of a district.                                                                   | ❌ No  | –                                                                                                                              | –                                                                                         |
| R31 | The population of a city.                                                                       | ❌ No  | –                                                                                                                              | –                                                                                         |
| R32 | Number of people who speak Chinese, English, Hindi, Spanish, and Arabic, with world % shares. | ❌ No  | –                                                                                                                              | –                                                                                         |
=======
> **Count:** 6 / 32 requirements implemented (all **Capital City** reports **R17–R22**) → **18.75%**.

| ID  | Name                                                                                           |   Met   | Screenshot                                                                                                                | CSV file                                                                  |
|-----|------------------------------------------------------------------------------------------------|:-------:|---------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| R01 | All the countries in the world organised by largest population to smallest.                    | ❌ No   | –                                                                                                                         | –                                                                         |
| R02 | All the countries in a continent organised by largest population to smallest.                  | ❌ No   | –                                                                                                                         | –                                                                         |
| R03 | All the countries in a region organised by largest population to smallest.                     | ❌ No   | –                                                                                                                         | –                                                                         |
| R04 | The top N populated countries in the world where N is provided by the user.                    | ❌ No   | –                                                                                                                         | –                                                                         |
| R05 | The top N populated countries in a continent where N is provided by the user.                  | ❌ No   | –                                                                                                                         | –                                                                         |
| R06 | The top N populated countries in a region where N is provided by the user.                     | ❌ No   | –                                                                                                                         | –                                                                         |
| R07 | All the cities in the world organised by largest population to smallest.                       | ❌ No   | –                                                                                                                         | –                                                                         |
| R08 | All the cities in a continent organised by largest population to smallest.                     | ❌ No   | –                                                                                                                         | –                                                                         |
| R09 | All the cities in a region organised by largest population to smallest.                        | ❌ No   | –                                                                                                                         | –                                                                         |
| R10 | All the cities in a country organised by largest population to smallest.                       | ❌ No   | –                                                                                                                         | –                                                                         |
| R11 | All the cities in a district organised by largest population to smallest.                      | ❌ No   | –                                                                                                                         | –                                                                         |
| R12 | The top N populated cities in the world where N is provided by the user.                       | ❌ No   | –                                                                                                                         | –                                                                         |
| R13 | The top N populated cities in a continent where N is provided by the user.                     | ❌ No   | –                                                                                                                         | –                                                                         |
| R14 | The top N populated cities in a region where N is provided by the user.                        | ❌ No   | –                                                                                                                         | –                                                                         |
| R15 | The top N populated cities in a country where N is provided by the user.                       | ❌ No   | –                                                                                                                         | –                                                                         |
| R16 | The top N populated cities in a district where N is provided by the user.                      | ❌ No   | –                                                                                                                         | –                                                                         |
| R17 | All the capital cities in the world organised by largest population to smallest.               | ✅ Yes  | <img src="docs/evidence/R17_world_capitals.png" alt="R17 screenshot" width="300" />                                       | [R17_capitals_world.csv](docs/evidence/R17_capitals_world.csv)           |
| R18 | All the capital cities in a continent organised by largest population to smallest.             | ✅ Yes  | <img src="docs/evidence/R18_capitals_continent_Europe.png" alt="R18 screenshot" width="260" />                            | [R18_capitals_continent_Europe.csv](docs/evidence/R18_capitals_continent_Europe.csv) |
| R19 | All the capital cities in a region organised by largest population to smallest.                | ✅ Yes  | <img src="docs/evidence/R19_capitals_region_Caribbean.png" alt="R19 screenshot" width="260" />                            | [R19_capitals_region_Caribbean.csv](docs/evidence/R19_capitals_region_Caribbean.csv) |
| R20 | The top N populated capital cities in the world where N is provided by the user.               | ✅ Yes  | <img src="docs/evidence/R20_capitals_world_top10.png" alt="R20 screenshot" width="300" />                                 | [R20_capitals_world_top10.csv](docs/evidence/R20_capitals_world_top10.csv) |
| R21 | The top N populated capital cities in a continent where N is provided by the user.             | ✅ Yes  | <img src="docs/evidence/R21_capitals_continent_Europe_top5.png" alt="R21 screenshot" width="260" />                       | [R21_capitals_continent_Europe_top5.csv](docs/evidence/R21_capitals_continent_Europe_top5.csv) |
| R22 | The top N populated capital cities in a region where N is provided by the user.                | ✅ Yes  | <img src="docs/evidence/R22_capitals_region_Caribbean_top3.png" alt="R22 screenshot" width="260" />                       | [R22_capitals_region_Caribbean_top3.csv](docs/evidence/R22_capitals_region_Caribbean_top3.csv) |
| R23 | Population of people, in cities and not in cities, for each continent.                         | ❌ No   | –                                                                                                                         | –                                                                         |
| R24 | Population of people, in cities and not in cities, for each region.                            | ❌ No   | –                                                                                                                         | –                                                                         |
| R25 | Population of people, in cities and not in cities, for each country.                           | ❌ No   | –                                                                                                                         | –                                                                         |
| R26 | The population of the world.                                                                    | ❌ No   | –                                                                                                                         | –                                                                         |
| R27 | The population of a continent.                                                                  | ❌ No   | –                                                                                                                         | –                                                                         |
| R28 | The population of a region.                                                                     | ❌ No   | –                                                                                                                         | –                                                                         |
| R29 | The population of a country.                                                                    | ❌ No   | –                                                                                                                         | –                                                                         |
| R30 | The population of a district.                                                                   | ❌ No   | –                                                                                                                         | –                                                                         |
| R31 | The population of a city.                                                                       | ❌ No   | –                                                                                                                         | –                                                                         |
| R32 | Number of people who speak Chinese, English, Hindi, Spanish, and Arabic, with world % shares. | ❌ No   | –                                                                                                                         | –                                                                         |
>>>>>>> develop

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

