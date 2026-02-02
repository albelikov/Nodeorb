# ЭТАП 1: Сборка
FROM gradle:9.3.0-jdk21 AS build
WORKDIR /app

# 1. Копируем файлы управления
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY buildSrc buildSrc

# 2. Создаем ПУСТЫЕ директории для ВСЕХ модулей, упомянутых в settings
# Эта команда берет все строки с include, очищает их от кавычек и скобок
# и создает папки, чтобы Gradle был доволен.
RUN grep "include" settings.gradle.kts | tr -d '()", ' | sed 's/include//' | xargs mkdir -p

# На всякий случай добавим те, что уже вылетали:
RUN mkdir -p freight-marketplace wms-service fms-service tms-service admin-backend admin-frontend common
# 3. Копируем реальный код только для нужного сервиса
COPY scm-service scm-service

# 4. Подготовка gradlew
RUN apt-get update && apt-get install -y dos2unix && \
    dos2unix gradlew && chmod +x gradlew

# 5. Сборка
RUN ./gradlew :scm-service:bootJar -x test --no-daemon

# ЭТАП 2: Запуск
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/scm-service/build/libs/*.jar app.jar

RUN groupadd -r nodeorb && useradd -r -g nodeorb nodeorb && \
    chown -R nodeorb:nodeorb /app
USER nodeorb

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
