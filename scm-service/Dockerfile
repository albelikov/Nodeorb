# Многоступенчатая сборка для оптимизации размера образа

# Этап 1: Сборка приложения
FROM gradle:9.3.0-jdk21 AS build

WORKDIR /app

# Копируем gradle wrapper и зависимости
COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

# Копируем common библиотеку
COPY common common/

# Копируем исходники конкретного сервиса
COPY scm-service scm-service/

# Собираем приложение (кэширование зависимостей)
RUN ./gradlew :scm-service:build -x test --no-daemon

# Этап 2: Запуск приложения
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Создаем пользователя для запуска приложения
RUN groupadd -r nodeorb && useradd -r -g nodeorb nodeorb

# Копируем собранный jar из этапа сборки
COPY --from=build /app/scm-service/build/libs/*.jar app.jar

# Меняем владельца
RUN chown -R nodeorb:nodeorb /app

USER nodeorb

# Expose порт
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Запуск приложения
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
