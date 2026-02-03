FROM eclipse-temurin:25-jdk

# Установка необходимых пакетов
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Копируем JAR-файл
COPY freight-marketplace/build/libs/freight-marketplace-0.1.0-SNAPSHOT.jar app.jar

# Запускаем приложение
CMD ["java", "-jar", "app.jar"]