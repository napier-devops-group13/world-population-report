# World Population Report (SET09803 Â· Group 13)

Minimal, CI-friendly scaffold for the coursework that delivers:

- Java **21+** with **Javalin 5** REST API
- **Maven** build â†’ **shaded runnable JAR**
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
[![CI](https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/napier-devops-group13/world-population-report/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/napier-devops-group13/world-population-report/branch/master/graph/badge.svg)](https://app.codecov.io/gh/napier-devops-group13/world-population-report)


---
## License

This project is licensed under the **MIT License** â€“ see [`LICENSE`](./LICENSE) for details.

- _Notes_: This coursework uses third-party tools and dependencies (e.g., Javalin, MySQL Docker image) under their own licenses.
---
## Project metadata

- **Code of Conduct:** see [`.github/CODE_OF_CONDUCT.md`](.github/CODE_OF_CONDUCT.md)
- **Issue templates:** user story, bug, and feature in [`.github/ISSUE_TEMPLATE/`](.github/ISSUE_TEMPLATE/)
- **Reviewers / ownership:** [`CODEOWNERS`](.github/CODEOWNERS)
- **CI/CD:** GitHub Actions workflow at [`.github/workflows/ci.yml`](.github/workflows/ci.yml)
---
GitFlow branching | Met | Screenshot | Used: master, develop, feature/*, release/*; hotfix policy defined (not exercised)

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

## CR1 Evidence (Screenshots)

| ID | Criterion (Rubric) | Met | Screenshot |
|---:|---|:--:|---|
| 1 | GitHub project set-up | ✅ | `docs/evidence/01_repo-root.png` |
| 2 | Product Backlog created | ✅ | `docs/evidence/07_issues-backlog.png` |
| 3 | Builds to self-contained JAR with Maven | ✅ | `docs/evidence/03_target-jar.png` |
| 4 | Dockerfile set-up and works | ✅ | `docs/evidence/04_compose-ps.png` / `docs/evidence/05_app-logs.png` |
| 5 | GitHub Actions build using JAR & Docker | ✅ | `docs/evidence/02_actions-green.png` |
| 6 | GitFlow branches (master, develop, release) | ✅ | `docs/evidence/06_branches.png` |
| 7 | First release created on GitHub | ✅ | `docs/evidence/14_release.png` |
| 8 | Code of Conduct defined | ✅ | `docs/evidence/01_repo-root.png` |
| 9 | Issues being used on GitHub | ✅ | `docs/evidence/07_issues-backlog.png` |
| 10 | Tasks defined as user stories | ✅ | `docs/evidence/07_issues-backlog.png` |
| 11 | Project integrated with Zube.io | ✅ | `docs/evidence/10_zube-board.png` |
| 12 | Kanban/Project Board being used | ✅ | `docs/evidence/08_project-board.png` |
| 13 | Sprint Boards being used | ✅ | `docs/evidence/09_sprint-iteration.png` |
| 14 | Full use cases defined | ✅ | `docs/evidence/11_use-cases.png` |
| 15 | Use case diagram created | ✅ | `docs/evidence/12_usecase-diagram.png` |
| 16 | Overall quality (GitHub metrics) | ✅ | `docs/evidence/15_insights.png` |
| 17 | Code quality incl. comments | ✅ | CI logs show Checkstyle/PMD clean (see 02) |
| 18 | Correct usage of branches | ✅ | `docs/evidence/13_pr-merged.png` |
| 19 | Continuous integration working | ✅ | `docs/evidence/02_actions-green.png` |
| 20 | Use cases well defined | ✅ | `docs/evidence/11_use-cases.png` |

---
## Team & Contributions (CR1)

This project is a Group 13 submission. The team roster is listed in **TEAM.md**.

### Individual Contribution Declaration — CR1
As required by SET09803, the percentages below reflect work **completed up to CR1**.

| Member                            | GitHub            | CR1 Contribution (%) |
|----------------------------------|-------------------|----------------------|
| **Khant Soe Thwin**              | @Yamikirito       | **100**              |
| Htet Wai                         | @HtetWai9671      | 0                    |
| Swam Htet Win                    | @SwamHtetWin10    | 0                    |
| Zay                              | @Zay12576         | 0                    |
| Minus Algo                       | @MinusAlgo4       | 0                    |
| Moe Myint Thaw                   | @MoeMyintThawDevOps | 0                  |

> Declaration: The above reflects contributions before the CR1 deadline. Any subsequent work will be captured in CR2 with updated percentages and evidence (PRs, commits, issues, reviews).
