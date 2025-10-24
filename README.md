# World Population Report (SET09803 · Group 13)

Minimal scaffold to satisfy CR1:
- Maven build → shaded runnable JAR
- Unit tests + JaCoCo coverage
- Checkstyle + SpotBugs
- Dockerfile
- GitHub Actions CI (build, test, analyze, docker build)

## Run locally
```bash
mvn -q -DskipTests package
java -jar target/world-population-report.jar
# http://localhost:7000/health
