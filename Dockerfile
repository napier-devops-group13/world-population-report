# ---- Build stage
FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /app

# Copy POM + Checkstyle config first (needed during validate phase)
COPY pom.xml .
COPY config ./config

# Go offline to cache deps (skip tests & analysis for speed in image build)
RUN mvn -q -B -ntp -DskipTests -Dcheckstyle.skip=true -Dspotbugs.skip=true -Djacoco.skip=true dependency:go-offline

# Copy source and build shaded JAR
COPY src ./src
RUN mvn -q -B -ntp -DskipTests -Dcheckstyle.skip=true -Dspotbugs.skip=true -Djacoco.skip=true package

# ---- Runtime stage
FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=build /app/target/world-population-report.jar /app/world-population-report.jar
EXPOSE 7000
ENV PORT=7000
ENTRYPOINT ["java","-jar","/app/world-population-report.jar"]
