# Autonomous Ops Dockerfile
# Многоступенчатая сборка для оптимизации размера образа

# Stage 1: Build stage
FROM gradle:8.5-jdk17 AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы сборки
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY gradle/ ./gradle/
COPY gradlew .
COPY gradlew.bat .

# Копируем исходный код
COPY src/ ./src/

# Собираем приложение
RUN ./gradlew clean build -x test

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-jammy

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл из stage 1
COPY --from=builder /app/build/libs/autonomous-ops-*.jar app.jar

# Устанавливаем переменные окружения
ENV JAVA_OPTS="-Xmx1g -Xms512m"
ENV SPRING_PROFILES_ACTIVE=docker

# Открываем порты
EXPOSE 8087

# Запускаем приложение
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8087/actuator/health || exit 1

# Labels for metadata
LABEL maintainer="Nodeorb Team <dev@nodeorb.com>"
LABEL version="1.0.0"
LABEL description="Autonomous Ops: AI/ML for automatic decision making in logistics"