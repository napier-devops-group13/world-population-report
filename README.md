# World Population Report (SET09803 · Group 13)

Minimal, CI-friendly scaffold for the coursework that delivers:

- Java **21+** with **Javalin 5** REST API
- **Maven** build → **shaded runnable JAR**
- **Unit tests** + **JaCoCo** coverage
- **Checkstyle (Google)** + **SpotBugs** gates
- **Dockerfile (multi-stage)** for a small, reproducible image
- **docker compose** stack with **MySQL 8.4** auto-seeded from `db/init/01-world.sql`
- **/ready** and **/health** endpoints
- Issue Forms (bug / feature / user story), PR template, CODEOWNERS, **Code of Conduct**

<p align="left">
  <!-- CI -->
  <a href="https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml">
    <img alt="CI" src="https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml/badge.svg">
  </a>
  <!-- License -->
  <a href="https://github.com/napier-devops-group13/world-population-report/blob/develop/LICENSE">
    <img alt="License: MIT" src="https://img.shields.io/badge/License-MIT-yellow.svg">
  </a>
  <!-- JDK hint (static badge) -->
  <img alt="JDK" src="https://img.shields.io/badge/JDK-21%2B-blue">
  <!-- Docker Compose hint (static badge) -->
  <img alt="Compose" src="https://img.shields.io/badge/Docker-Compose-green">
</p>

---
## License

This project is licensed under the **MIT License** – see [`LICENSE`](./LICENSE) for details.

- _Notes_: This coursework uses third-party tools and dependencies (e.g., Javalin, MySQL Docker image) under their own licenses.
---
## Project metadata

- **Code of Conduct:** see [`.github/CODE_OF_CONDUCT.md`](.github/CODE_OF_CONDUCT.md)
- **Issue templates:** user story, bug, and feature in [`.github/ISSUE_TEMPLATE/`](.github/ISSUE_TEMPLATE/)
- **Reviewers / ownership:** [`CODEOWNERS`](.github/CODEOWNERS)
- **CI/CD:** GitHub Actions workflow at [`.github/workflows/ci.yml`](.github/workflows/ci.yml)
---


## Table of contents

- [1) Prerequisites](#1-prerequisites)
- [2) Quick start](#2-quick-start)
- [3) Configuration](#3-configuration)
- [4) API](#4-api)
- [5) Build, test & quality gates](#5-build-test--quality-gates)
- [6) Run with Docker Compose](#6-run-with-docker-compose)
- [7) Project structure](#7-project-structure)
- [8) CI/CD](#8-cicd)
- [9) Collaboration workflow (Assessment GitFlow)](#9-collaboration-workflow-assessment-gitflow)
- [10) Contributing & Community](#10-contributing--community)
- [11) Troubleshooting](#11-troubleshooting)

---

## 1) Prerequisites

- **JDK 21** (or newer; the project is compiled with `--release 21`)
- **Maven 3.9+**
- **Docker Desktop** with **Compose v2** (for the compose workflow)
- Optional: **IntelliJ IDEA** (Community or Ultimate)

Quick check:

```powershell
mvn -v
java -version
docker --version
```

---

## 2) Quick start

A) Run locally (without Docker)

```powershell
# 1) Clone
git clone https://github.com/napier-devops-group13/world-population-report.git
cd world-population-report

# 2) Build a shaded JAR
mvn -q -DskipTests package

# 3) Run the app
java -jar target/world-population-report.jar

# 4) Smoke-test
curl http://localhost:7000/ready
curl http://localhost:7000/health

```

B) Run the full stack with Docker Compose

```powershell
# 1) Copy environment template and (optionally) edit values
Copy-Item .env.example .env   # macOS/Linux: cp .env.example .env

# 2) Build & start
docker compose up -d --build

# 3) Check
curl http://localhost:7000/ready
curl http://localhost:7000/health

# 4) Stop & clean
docker compose down -v
```

---

## 3) Configuration

- The app reads configuration from .env (for Compose) and Spring-style src/main/resources/application.properties (already aligned).
- Change PORT if 7000 is busy.
- In Compose the db service auto-seeds from db/init/01-world.sql.
- Do not commit a real .env – the repo tracks .env.example only.

---
## 4) API

Current health endpoints:

| Method | Path     | Purpose                               | Example                              |
|------: |----------|---------------------------------------|--------------------------------------|
| GET    | `/ready` | Liveness check (app is up)            | `curl http://localhost:7000/ready`   |
| GET    | `/health`| Health incl. DB status (JSON payload) | `curl http://localhost:7000/health`  |

---

## 5) Build, test & quality gates

**Local Maven targets:**

```powershell
# Fast build (skip tests)
mvn -q -DskipTests package

# Full verification with unit tests + JaCoCo + SpotBugs + Checkstyle
mvn -q verify

# Open coverage report (path)
# target/site/jacoco/index.html
```
**Artifacts / reports:**

- **Shaded JAR:** `target/world-population-report.jar`
- **JaCoCo report:** `target/site/jacoco/index.html`
- **SpotBugs:** `target/spotbugs.html`
- **Checkstyle:** `target/checkstyle-result.xml`

CI enforces the same gates on PRs (see [CI/CD](#8-cicd) ↗).

---

## 6) Run with Docker Compose

```powershell
# Build images (multi-stage) and start both services
docker compose up -d --build

# Follow logs
docker compose logs -f

# Restart app only after code changes
docker compose up -d --build app

# Tear down everything (incl. volumes)
docker compose down -v
```
**Services (see docker-compose.yml):**

- app → Javalin service exposing :${PORT:-7000}
- db → MySQL 8.4 seeded by db/init/01-world.sql on first run

---

## 7) Project structure (quick map)

- `.github/` → Issue Forms (**bug** / **feature** / **user story** + `config.yml`), **PR template**, **CODEOWNERS**, **CODE_OF_CONDUCT**, and CI workflows (`workflows/ci.yml`, `workflows/auto-triage.yml`).
- `config/checkstyle/` → **Checkstyle** rules + **suppressions** (quality gate).
- `db/init/01-world.sql` → **MySQL** seed loaded on first Compose run.
- `src/main/java/` → **Javalin** REST API (health endpoints; DB access in `db/` and `repo/` packages).
- `src/main/resources/application.properties` → App **configuration** (port & DB props).
- `src/test/java/` → **Sanity** and **unit tests**.
- `Dockerfile` & `docker-compose.yml` → Container build + **2-service stack** (app + MySQL).
- `.env.example` → Template for local env vars (copy → `.env`).
- `pom.xml` → **Maven** build (shaded JAR, plugins, **JaCoCo**, **SpotBugs**, **Checkstyle**).
- `.editorconfig`, `.gitignore`, `.dockerignore` → repo/IDE hygiene.

---

## 8) CI/CD

[![CI](https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml/badge.svg)](https://github.com/napier-devops-group13/world-population-report/actions)

**GitHub Actions:** `.github/workflows/ci.yml`

Pipeline (on push/PR to `develop` & `master`):

1. **Build on JDK 24** – fast build (skips coverage).
2. **Coverage on JDK 21** – runs unit tests with **JaCoCo** coverage.
3. **Docker build** – verifies the container image builds successfully.
4. **Publish shaded JAR artifact** for each successful run.

See runs & artifacts → **Actions**:  
https://github.com/napier-devops-group13/world-population-report/actions
---

## 9) Collaboration workflow (Assessment GitFlow)

We follow the **GitFlow** model required by the coursework.

### Branch roles
- **master** → production-ready only; each release is **tagged** (`vX.Y.Z`).
- **develop** → integration branch; default target for PRs; CI must be green.
- **feature/*** → short-lived branches from `develop` for new work  
  (e.g., `feature/compose-worlddb`).
- **release/*** → stabilization branch cut from `develop` to prepare a version  
  (e.g., `release/v0.1.4`). Fixes & docs only; **no new features**.

### Feature flow
```powershell
# Create your branch from develop
git checkout -b feature/<topic> develop

# ... commit small, focused changes ...
git push -u origin feature/<topic>

# Open a PR -> target: develop (CI green, at least one approval)
# Merge (squash/rebase), then delete the feature branch
```
### Release flow
```powershell
# cut a release branch
git checkout -b release/vX.Y.Z develop
# bump version/CHANGELOG, fixes only
git push -u origin release/vX.Y.Z

# finalize: merge to master with a merge commit and tag
git checkout master
git merge --no-ff release/vX.Y.Z -m "Release vX.Y.Z"
git tag -a vX.Y.Z -m "Release vX.Y.Z"
git push origin master --tags

# back-merge the version bump into develop
git checkout develop
git merge --no-ff release/vX.Y.Z -m "Back-merge version bump from release/vX.Y.Z"
git push origin develop

# remove release branch
git push origin :release/vX.Y.Z
git branch -D release/vX.Y.Z
```

### Guardrails:

- CODEOWNERS requires review for critical paths.
- PR template + Issue forms (bug / feature / user story) keep quality high.
---

## 10) Contributing & Community

- Please read our **[Code of Conduct](.github/CODE_OF_CONDUCT.md)** ↗.
- Open **User story**, **Bug** or **Feature** requests via **Issues**  
  (templates live in **`.github/ISSUE_TEMPLATE/`**).
- Q&A: use **Discussions** on the repository  
  (the link is defined in **`.github/ISSUE_TEMPLATE/config.yml`**).
- For local changes, follow the **GitFlow** steps in **[§9](#9-collaboration-workflow-assessment-gitflow)** ↗.

---
## 11) Troubleshooting

### App starts but `/health` fails
- DB may still be initializing; run: `docker compose logs -f db`.
- If schema is corrupted, reset volumes: `docker compose down -v` then `docker compose up -d --build`.

### Port 7000 already in use
- Change `PORT` in `.env` (and/or `src/main/resources/application.properties`) then rebuild/restart.

### Tests fail locally but pass in CI
- Clean everything: `mvn -q clean verify`.
- Ensure Java is **21+**: `java -version`.

### Compose can’t start on Windows
- Check that **WSL2 / virtualization** is enabled and Docker Desktop is running.

### Stale images/volumes (reset hard)

```powershell
docker compose down -v
docker system prune -f
docker volume prune -f
docker compose up -d --build
```
---

