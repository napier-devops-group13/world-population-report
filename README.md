# World Population Report (SET09803 · Group 13)

[![build: master](https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml)
[![build: develop](https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml)
[![coverage](https://codecov.io/gh/napier-devops-group13/world-population-report/branch/master/graph/badge.svg)](https://codecov.io/gh/napier-devops-group13/world-population-report)
![release](https://img.shields.io/github/v/release/napier-devops-group13/world-population-report?include_prereleases&sort=semver)
![license](https://img.shields.io/github/license/napier-devops-group13/world-population-report)

> **Tech stack:** Java 24 · [Javalin](https://javalin.io) · MySQL (sample **world** DB) · Maven · JaCoCo · Checkstyle (Google) · SpotBugs · Docker · Docker Compose · GitHub Actions (CI/CD)

---

## What this project does

Implements the **World Population Reporting System** required by the coursework. The service exposes REST endpoints to produce 32 population/country/city/capital/language reports and additional population summaries. It is fully automated with CI, unit/initial integration tests, quality gates, and a Dockerized runtime with a MySQL container auto-seeded from the official **world** dataset.

---

## Quick start

### Option A — Run locally (no Docker)
```bash
# 1) Build shaded runnable JAR
mvn -q -DskipTests package

# 2) Run it (expects DB env vars if you target a live MySQL)
java -jar target/world-population-report.jar
# → http://localhost:7000/health
