# World Population Report (SET09803 · Group 13)

Minimal, CI-friendly scaffold that delivers:
- Java 21+ with [Javalin 5] REST API
- Maven build → shaded runnable JAR
- Unit tests + JaCoCo coverage
- Checkstyle (Google) + SpotBugs gates
- Dockerfile (multi-stage) for a small, reproducible image
- `docker compose` stack with **MySQL 8.4** auto-seeded using the official *world* sample DB
- Health/Readiness endpoints + first feature (R26): world population

> **Branches**  
> Active work happens on `feature/compose-worlddb` → merged to `develop` by PR.

---

## 1) Prerequisites

- **JDK 21** or newer (project is compiled with `--release 21` so JDK 24 also works)
- **Maven 3.9+**
- **Docker** and **Docker Compose V2** (comes with modern Docker Desktop)
- Optional: **IntelliJ IDEA** (community or ultimate)

---

## 2) Project layout (key files)

