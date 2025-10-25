# World Population Report (SET09803 · Group 13)

[![build: master](https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml)
[![build: develop](https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml)
[![coverage](https://codecov.io/gh/napier-devops-group13/world-population-report/branch/master/graph/badge.svg)](https://codecov.io/gh/napier-devops-group13/world-population-report)
![release](https://img.shields.io/github/v/release/napier-devops-group13/world-population-report?include_prereleases&sort=semver)
![license](https://img.shields.io/github/license/napier-devops-group13/world-population-report)

> **Tech stack:** Java 24 · Javalin · MySQL (sample **world** DB) · Maven · JaCoCo · Checkstyle (Google) · SpotBugs · Docker · Docker Compose · GitHub Actions (CI/CD)

---

## What this project does

Implements the **World Population Reporting System** for the coursework. The service exposes REST endpoints to produce 32 population/country/city/capital/language reports with CI, tests, code quality gates, and a Dockerized runtime. MySQL is **auto-seeded** from the official `world` dataset on first start.

---

## Quick start

### A) Run locally (no Docker)
```bash
mvn -q -DskipTests package
java -jar target/world-population-report.jar
# → http://localhost:7000/health
