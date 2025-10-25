# World Population Report (SET09803 · Group 13)

Minimal, CI-friendly scaffold that delivers:

- Java **21+** with **Javalin 5** REST API
- Maven build ➜ **shaded runnable JAR**
- **Unit tests** + **JaCoCo** coverage
- **Checkstyle (Google)** + **SpotBugs** gates
- **Dockerfile (multi-stage)** for a small, reproducible image
- `docker compose` stack with **MySQL 8.4** auto-seeded from `db/init/01-world.sql`
- **/ready** and **/health** endpoints
- cp .env.example .env   # Windows: Copy-Item .env.example .env


> **Branches**  
> Active work happens on `feature/compose-worlddb` → merged into the default branch by PR.

---

## 1) Prerequisites

- **JDK 21** or newer (project is compiled with `--release 21`, so JDK 24 also works)
- **Maven 3.9+**
- **Docker Desktop** with **Compose v2** (comes with modern Docker Desktop)
- Optional: **IntelliJ IDEA** (Community or Ultimate)

Quick check:

```powershell
mvn -v
java -version
docker --version
