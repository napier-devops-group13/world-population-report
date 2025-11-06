# World Population Report (SET09803 — Group 13)

<p align="left">
  <!-- CI (GitFlow branches) -->
  <a href="https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml?query=branch%3Amaster">
    <img alt="CI (master)" src="https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml/badge.svg?branch=master">
  </a>
  <a href="https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml?query=branch%3Adevelop">
    <img alt="CI (develop)" src="https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml/badge.svg?branch=develop">
  </a>

  <!-- Coverage (master) -->
  <a href="https://app.codecov.io/gh/napier-devops-group13/world-population-report">
    <img alt="Coverage" src="https://codecov.io/gh/napier-devops-group13/world-population-report/branch/master/graph/badge.svg">
  </a>

  <!-- Release + License + Tech -->
  <a href="https://github.com/napier-devops-group13/world-population-report/releases">
    <img alt="Release" src="https://img.shields.io/github/v/release/napier-devops-group13/world-population-report">
  </a>
  <a href="LICENSE">
    <img alt="License: MIT" src="https://img.shields.io/badge/License-MIT-yellow.svg">
  </a>
  <img alt="JDK" src="https://img.shields.io/badge/JDK-21%2B-blue">
  <img alt="Compose" src="https://img.shields.io/badge/Docker-Compose-green">
</p>

A clean, CI-friendly coursework scaffold that **fully implements the Country Reports (R01–R06)** and aligns to **Code Review 1**, **Code Review 2**, and **Final Delivery** expectations.

- Java 21+ with **Javalin 5** REST API (port **7070**)
- Maven build → **shaded runnable JAR**
- Unit tests + **JaCoCo** coverage (uploaded to **Codecov**)
- Checkstyle (Google) + SpotBugs (quality gates via CI)
- Dockerfile (multi-stage) + `docker compose` for **MySQL 8.4** (seeded with `world` dataset)
- GitFlow: **master**, **develop**, **release/**\* (CI triggers on all)
- Issue templates, PR template, CODEOWNERS, Code of Conduct

---

## Table of Contents
- [Quick Start](#quick-start)
- [API Endpoints — Countries (R01–R06)](#api-endpoints--countries-r01r06)
- [Database & Seeding](#database--seeding)
- [Project Structure](#project-structure)
- [Quality & CI/CD](#quality--cicd)
- [Assessment Evidence (CR1 / CR2 / Final)](#assessment-evidence-cr1--cr2--final)
- [Team & Contributions](#team--contributions)
- [License](#license)

---

## Quick Start

> **Requirements**: JDK 21+, Maven 3.9+, Docker Desktop

### 1) Run with Docker Compose (DB only) + local JAR (recommended for dev)

```bash
# from repo root
docker compose up -d db

# set app env when running the JAR (Linux/macOS)
export DB_HOST=127.0.0.1
export DB_PORT=$(docker compose port db 3306 | awk -F: '{print $NF}')
export DB_NAME=world
export DB_USER=app
export DB_PASSWORD=app

mvn -q -ntp clean package -DskipTests
java -jar target/world-population-report.jar
# -> Listening on http://localhost:7070
```
---
### API Endpoints — Countries (R01–R06)
**Base URL:** `http://localhost:7070`

| ID  | Method | Endpoint                                   | Notes                                                 |
|-----|:------:|--------------------------------------------|------------------------------------------------------|
| R01 |  GET   | `/countries/world`                          | All countries (world). Optional sort: `?sort=pop`    |
| R02 |  GET   | `/countries/continent/{continent}`          | All countries in a continent. `?sort=pop` supported  |
| R03 |  GET   | `/countries/region/{region}`                | All countries in a region. `?sort=pop` supported     |
| R04 |  GET   | `/countries/world/top/{n}`                  | Top-N countries (world) by population                |
| R05 |  GET   | `/countries/continent/{continent}/top/{n}`  | Top-N countries in a continent                        |
| R06 |  GET   | `/countries/region/{region}/top/{n}`        | Top-N countries in a region                           |

> Invalid `{n}` values (e.g., `0`, negative, non-integer) return **HTTP 400** with a JSON error.


---
## Database & Seeding

- **Image:** `mysql:8.4`
- **Schema:** preloaded **world** dataset
- **Compose service name:** `db`
- **Seed files** (supported in CI and local):
  - `db/init/01-world.sql` **or** `db/01-world.sql`

### App env vars

| Name          | Description                                 | Example          |
|---------------|---------------------------------------------|------------------|
| `DB_HOST`     | Database host (use `127.0.0.1`)             | `127.0.0.1`      |
| `DB_PORT`     | **Host** port for MySQL                     | `43306` (varies) |
| `DB_NAME`     | Database name                               | `world`          |
| `DB_USER`     | DB user                                     | `app` or `root`  |
| `DB_PASSWORD` | DB password                                 | `app` or `root`  |

> **Windows tip:** `DB_PORT` must be the **host** port printed by  
> `docker compose port db 3306` — typically `43306`. **Do not** use `33060`.


---
### Project Structure

| Path | Purpose |
|------|---------|
| `/` | Repository root (**world-population-report**) |
| `src/main/java/com/group13/population/App.java` | Javalin bootstrap + route wiring for **R01–R06** |
| `src/main/java/com/group13/population/db/Db.java` | MySQL connection utilities + `awaitReady()` |
| `src/main/java/com/group13/population/model/` | POJOs / DTOs used by the API |
| `src/main/java/com/group13/population/repo/WorldRepo.java` | SQL queries + mappers for country reports |
| `db/init/01-world.sql` | Seed data for MySQL (**or** `db/01-world.sql`) |
| `Dockerfile` | Multi-stage build for small runnable image |
| `docker-compose.yml` | Local stack: **db** service (+ optional app) |
| `pom.xml` | Maven config (shaded JAR, surefire/failsafe, JaCoCo, Checkstyle, SpotBugs) |
| `.github/workflows/ci.yml` | GitFlow-aware CI: build, unit/ITs, coverage, Docker smoke |


---
## Quality & CI/CD

- **CI triggers on:** `master`, `develop`, `release/**` (and PRs targeting `master` or `develop`)
- **Jobs:**
  1. **Build + Tests (JDK 24)** — spins up MySQL, seeds the **world** DB, runs unit **and** integration tests, then uploads the shaded JAR + test reports.
  2. **Coverage (JDK 21)** — generates JaCoCo HTML report and uploads coverage to **Codecov**.
  3. **Docker Smoke** — builds the Docker image, runs the container against a MySQL service, and verifies  
     `GET /countries/world` returns **200**.
- **Static analysis:** Checkstyle (Google) and SpotBugs run in CI.
- **Releases/Tags:** create releases from `master` following GitFlow  
  *(feature ➜ develop ➜ release/* ➜ master + tag)*.

---

## Assessment Evidence (CR1 / CR2 / Final)

### CR1 — Checklist (all required items present)

| ID | Criterion (Rubric)                                    | Met | Evidence (path / note)                          |
|---:|--------------------------------------------------------|:--:|-------------------------------------------------|
|  1 | GitHub project set-up                                  | ✅  | `docs/evidence/01_repo-root.png`                |
|  2 | Product Backlog created                                | ✅  | `docs/evidence/07_issues-backlog.png`           |
|  3 | Builds to self-contained JAR with Maven                | ✅  | `docs/evidence/03_target-jar.png`               |
|  4 | Dockerfile set-up and works                            | ✅  | `docs/evidence/04_compose-ps.png`               |
|  5 | GitHub Actions build using JAR & Docker                | ✅  | `docs/evidence/02_actions-green.png`            |
|  6 | GitFlow branches (master, develop, release/*)          | ✅  | `docs/evidence/06_branches.png`                 |
|  7 | First release created                                  | ✅  | `docs/evidence/14_release.png`                  |
|  8 | Code of Conduct defined                                | ✅  | `docs/evidence/01_repo-root.png`                |
|  9 | Issues used on GitHub                                  | ✅  | `docs/evidence/07_issues-backlog.png`           |
| 10 | Tasks defined as user stories                          | ✅  | `docs/evidence/07_issues-backlog.png`           |
| 11 | Project integrated with Zube.io                        | ✅  | `docs/evidence/10_zube-board.png`               |
| 12 | Kanban/Project Board being used                        | ✅  | `docs/evidence/08_project-board.png`            |
| 13 | Sprint Boards being used                               | ✅  | `docs/evidence/09_sprint-iteration.png`         |
| 14 | Full use cases defined                                 | ✅  | `docs/evidence/11_use-cases.png`                |
| 15 | Use case diagram created                               | ✅  | `docs/evidence/12_usecase-diagram.png`          |
| 16 | Overall quality: Metrics from GitHub                   | ✅  | `docs/evidence/15_insights.png`                 |
| 17 | Code quality including comments                        | ✅  | CI logs (Checkstyle/SpotBugs) `02_actions-green.png` |
| 18 | Correct usage of branches                              | ✅  | `docs/evidence/13_pr-merged.png`                |
| 19 | Continuous integration working                         | ✅  | `docs/evidence/02_actions-green.png`            |
| 20 | Use cases well defined                                 | ✅  | `docs/evidence/11_use-cases.png`                |

---

### CR2 — Graded Criteria

| ID | Criterion (Rubric)                                        | Met / Value          | Evidence (path / note)                                  |
|---:|------------------------------------------------------------|:---------------------|---------------------------------------------------------|
|  1 | Quality & coverage of unit tests                           | ✅ (JaCoCo %)        | `docs/evidence/21_jacoco-report.png` (`target/site/jacoco`) |
|  2 | Suitable integration tests defined                         | ✅                   | `docs/evidence/22_failsafe-it.png` (DB ITs passing)     |
|  3 | Continuous integration running tests (GitHub Actions)      | ✅                   | `docs/evidence/02_actions-green.png`                    |
|  4 | Deployment working (Docker run + smoke)                    | ✅                   | `docs/evidence/23_docker-smoke.png`                     |
|  5 | Bug reporting system set-up                                | ✅                   | `docs/evidence/24_bug-template.png` / Issues list       |
|  6 | Badges (build master/develop, coverage, release, license)  | ✅                   | `docs/evidence/25_readme-badges.png`                    |
|  7 | **Project Requirements Met (R01–R32)**                     | **6 implemented**    | Countries R01–R06 pass (see `docs/evidence/26_country-api-tests.png`) |
|  8 | Correct use of GitHub & Kanban; commit frequency; comments | ✅                   | `docs/evidence/27_commits-and-board.png`                |

> Update the **JaCoCo %** screenshot and the **“6 implemented”** count as you add more requirements.

---

### Final — Delivery Checklist

| ID | Item (assessed on master)                               | Met | Evidence (path / note)                          |
|---:|----------------------------------------------------------|:--:|-------------------------------------------------|
|  1 | README contains all required badges                      | ✅  | `docs/evidence/25_readme-badges.png`            |
|  2 | Evidence table (this section) present & complete         | ✅  | `docs/evidence/28_evidence-table.png`           |
|  3 | Master branch contains assessable code (builds & runs)   | ✅  | `docs/evidence/29_master-build-ok.png`          |
|  4 | Final release/tag created                                | ✅  | `docs/evidence/30_final-release.png`            |
|  5 | Individual contribution spreadsheet attached (CR1 & CR2) | ✅  | `docs/evidence/31_contrib-spreadsheets.png`     |


---
## Team & Contributions (CR1)

This project is a Group 13 submission. The team roster is listed in **TEAM.md**.

### Individual Contribution Declaration — CR1
As required by SET09803, the percentages below reflect work **completed up to CR1**.

| Member              | GitHub              | CR1 Contribution (%) |
|---------------------|---------------------|----------------------|
| **Khant Soe Thwin** | @Yamikirito         | **70**               |
| Htet Wai Yan Lin    | @HtetWai9671        | 50                   |
| Swam Htet Win       | @SwamHtetWin10      | 50                   |
| Zay Lin Myat        | @Zay12576           | 50                   |
| Naing Lin Aung      | @MinusAlgo4         | 50                   |
| Moe Myint Thaw      | @MoeMyintThawDevOps | 50                   |

> Declaration: The above reflects contributions before the CR1 deadline. Any subsequent work will be captured in CR2 with updated percentages and evidence (PRs, commits, issues, reviews).
---

## Country Report — API (R01–R06)

Base URL: `http://localhost:7070`

| ID  | Endpoint                                       | Notes                                                       | Met |
|-----|------------------------------------------------|-------------------------------------------------------------|:---:|
| R01 | `GET /countries/world`                         | All countries (world). Optional sort by population: `?sort=pop`. | ✅ |
| R02 | `GET /countries/continent/{continent}`         | All countries in a continent. Optional `?sort=pop`.         | ✅ |
| R03 | `GET /countries/region/{region}`               | All countries in a region. Optional `?sort=pop`.            | ✅ |
| R04 | `GET /countries/world/top/{n}`                 | Top-N countries (world) by population.                      | ✅ |
| R05 | `GET /countries/continent/{continent}/top/{n}` | Top-N countries in the given continent.                     | ✅ |
| R06 | `GET /countries/region/{region}/top/{n}`       | Top-N countries in the given region.                        | ✅ |

**Error handling (for evidence):**
- `n <= 0` or non-integer → **400** with JSON body: `{"error":"n must be > 0"}` or `{"error":"n must be a positive integer"}`.
- Invalid path variables return **400**/**404** depending on your repo lookups.

### Quick local checks (PowerShell)
```powershell
# Start the app (separate terminal) after `mvn -q -ntp -DskipITs package`
java -jar target/world-population-report.jar

# Sorted lists
curl.exe -s "http://localhost:7070/countries/world?sort=pop" | ConvertFrom-Json | Select-Object -First 1
curl.exe -s "http://localhost:7070/countries/continent/Asia?sort=pop" | ConvertFrom-Json | Select-Object -First 1
curl.exe -s "http://localhost:7070/countries/region/Eastern%20Asia?sort=pop" | ConvertFrom-Json | Select-Object -First 1

# Top-N
curl.exe -s "http://localhost:7070/countries/world/top/5" | ConvertFrom-Json | Measure-Object
curl.exe -s "http://localhost:7070/countries/continent/Europe/top/5" | ConvertFrom-Json | Measure-Object
curl.exe -s "http://localhost:7070/countries/region/Eastern%20Asia/top/5" | ConvertFrom-Json | Measure-Object

# 400 proofs (bad input)
curl.exe -i "http://localhost:7070/countries/world/top/0"
curl.exe -i "http://localhost:7070/countries/continent/Asia/top/-3"
curl.exe -i "http://localhost:7070/countries/region/Eastern%20Asia/top/abc"
```
---
## License

This project is released under the **MIT License**. See [LICENSE](LICENSE).
---
