# Серверная часть административной панели (Admin Backend)

## Overview
Сервис для управления административной частью логистической платформы Nodeorb. Предоставляет инструменты для мониторинга, управления пользователями и конфигурации системы.

## Structure

### Core Modules
- `com.adminbackend.core` - Основная бизнес-логика
- `com.adminbackend.data.entities` - JPA сущности для хранения данных
- `com.adminbackend.services` - Сервисный слой для бизнес-операций
- `com.adminbackend.controllers` - REST API контроллеры
- `com.adminbackend.config` - Конфигурационные классы

### Key Classes

#### Core Business Logic
- `UserManagementService.kt` - Управление пользователями и ролями
- `SystemMonitoringService.kt` - Мониторинг производительности системы
- `AnalyticsService.kt` - Статистика и аналитика операций
- `SecurityConfigService.kt` - Управление настройками безопасности
- `PerformanceTracker.kt` - Отслеживание производительности

#### Data Entities
- `User.kt` - Пользователь с ролями и правами
- `Role.kt` - Роль пользователя
- `Permission.kt` - Право доступа
- `SystemMetric.kt` - Метрика системы
- `SystemConfig.kt` - Настройка системы

## Functionality

### User Management
- Создание, обновление и удаление пользователей
- Управление ролями и правами доступа
- Аутентификация и авторизация через OAuth2/JWT
- Сброс паролей и двухфакторная аутентификация

### System Monitoring
- Мониторинг производительности сервисов
- Отслеживание использования ресурсов (CPU, память, диск)
- Логирование и аудит операций
- Уведомления о проблемах и алерты

### Analytics & Reporting
- Статистика операций в реальном времени
- Отчеты по использованию системы
- Анализ эффективности процессов
- Визуализация данных в дашбордах

### System Configuration
- Управление глобальными настройками
- Конфигурация интеграций с внешними системами
- Настройка безопасности и доступа
- Управление параметрами работы платформы

## Technology Stack
- Spring Boot 3.2.4
- Kotlin 1.9.23
- PostgreSQL 18
- Spring Data JPA
- OAuth2 + JWT для аутентификации
- Spring Security для авторизации
- Docker для контейнеризации

## Configuration

### Application Properties
```properties
# Server configuration
server.port=8081

# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/admin_db
spring.datasource.username=nodeorb
spring.datasource.password=nodeorb_dev_password

# Security configuration
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://keycloak:8080/realms/nodeorb/protocol/openid-connect/certs

# Logging configuration
logging.level.com.adminbackend=DEBUG
```

## Running the Service

### Local Development
```bash
cd c:\Project\Nodeorb
./gradlew :admin-backend:compileKotlin
./gradlew :admin-backend:test
./gradlew :admin-backend:bootRun
```

### Docker Deployment
```bash
cd c:\Project\Nodeorb
docker build -t admin-backend:latest -f admin-backend/Dockerfile .
docker run -d -p 8081:8081 --network nodeorb-network admin-backend:latest
```

## API

### REST Endpoints

#### User Management
- `GET /api/v1/users` - Список всех пользователей
- `POST /api/v1/users` - Создать нового пользователя
- `GET /api/v1/users/{id}` - Получить пользователя по ID
- `PUT /api/v1/users/{id}` - Обновить пользователя
- `DELETE /api/v1/users/{id}` - Удалить пользователя
- `GET /api/v1/users/{id}/roles` - Получить роли пользователя
- `PUT /api/v1/users/{id}/roles` - Обновить роли пользователя

#### System Monitoring
- `GET /api/v1/metrics` - Получить метрики системы
- `GET /api/v1/health` - Проверить здоровье системы
- `GET /api/v1/logs` - Получить логи операций
- `POST /api/v1/alerts` - Создать алерт
- `GET /api/v1/alerts` - Список алертов

#### Analytics
- `GET /api/v1/analytics/operations` - Статистика операций
- `GET /api/v1/analytics/performance` - Производительность системы
- `GET /api/v1/analytics/users` - Данные о пользователях
- `POST /api/v1/analytics/reports` - Сгенерировать отчет

#### System Configuration
- `GET /api/v1/config` - Получить конфигурацию
- `PUT /api/v1/config` - Обновить конфигурацию
- `GET /api/v1/config/integrations` - Интеграции с внешними системами
- `POST /api/v1/config/integrations` - Добавить интеграцию

## Contributing
Please follow the project guidelines for contributing. Create a feature branch, make your changes, and submit a pull request.

## License
MIT License - see LICENSE file for details.