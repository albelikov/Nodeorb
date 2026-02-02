# Multi-stage build для оптимизации размера контейнера
FROM gradle:8.7-jdk17 AS builder

WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY src ./src

RUN gradle build -x test --no-daemon

FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8084/actuator/health || exit 1

# Expose the application port
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]