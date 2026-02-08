FROM openjdk:25-jdk-slim

WORKDIR /app

# Copy build artifacts
COPY build/libs/*.jar app.jar

# Copy configuration
COPY src/main/resources/application.conf application.conf

# Expose gRPC port
EXPOSE 50055

# Health check
HEALTHCHECK --interval=30s --timeout=30s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:50055/health || exit 1

# Entry point
ENTRYPOINT ["java", "-jar", "app.jar"]