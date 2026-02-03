# Dockerfile для SCM-Service на Java 25
FROM eclipse-temurin:25-jdk-jammy AS builder

WORKDIR /app

# Установка Gradle 9.3.0
RUN wget -q https://services.gradle.org/distributions/gradle-9.3-bin.zip  \
    && unzip gradle-9.3-bin.zip \
    && rm gradle-9.3-bin.zip \
    && mv gradle-9.3 /opt/gradle

ENV GRADLE_HOME=/opt/gradle
ENV PATH=$PATH:$GRADLE_HOME/bin

# Копируем только файлы сборки для кэширования зависимостей
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Загружаем зависимости (кешируется отдельным слоем)
RUN gradle dependencies --no-daemon

# Копируем исходный код и собираем приложение
COPY src src
RUN gradle bootJar --no-daemon

# Финальный образ
FROM eclipse-temurin:25-jre-jammy

LABEL maintainer="SCM Team <scm@nodeorb.com>"
LABEL version="4.0.0"
LABEL description="SCM Service for Nodeorb Platform"

WORKDIR /app

# Создаем пользователя для безопасности
RUN groupadd -r scmuser && useradd -r -g scmuser -s /bin/false scmuser

# Копируем JAR из builder
COPY --from=builder /app/build/libs/scm-service.jar /app/scm-service.jar

# Настройки Java для Java 25
ENV JAVA_OPTS="\
    -XX:+UseZGC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UseStringDeduplication \
    -XX:+ExitOnOutOfMemoryError \
    -Xmx2048m \
    -Xms1024m \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    --enable-preview \
    -Dspring.profiles.active=prod"

# Настройки для Native Memory Tracking
ENV JAVA_TOOL_OPTIONS="-XX:NativeMemoryTracking=summary -XX:+UnlockDiagnosticVMOptions"

# Экспонируем порты
EXPOSE 8080 9090

# Настройка прав
RUN chown -R scmuser:scmuser /app
USER scmuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/scm/management/health-check || exit 1

# Запуск приложения
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/scm-service.jar"]
