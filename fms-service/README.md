# Сервис управления флотом (Fleet Management Service - FMS)

## Overview
Система управления транспортным флотом с трекингом в реальном времени для логистической платформы Nodeorb. Обеспечивает мониторинг транспорта, водителей и эффективность использования ресурсов.

## Structure

### Core Modules
- `com.fmsservice.core` - Основная бизнес-логика
- `com.fmsservice.data.entities` - JPA сущности для хранения данных
- `com.fmsservice.services` - Сервисный слой для бизнес-операций
- `com.fmsservice.controllers` - REST API контроллеры
- `com.fmsservice.integration` - Интеграции с внешними системами
- `com.fmsservice.config` - Конфигурационные классы

### Key Classes

#### Core Business Logic
- `VehicleManagementService.kt` - Управление транспортными средствами
- `DriverManagementService.kt` - Управление водителями
- `FuelMonitoringService.kt` - Контроль расхода топлива
- `MaintenanceService.kt` - Техническое обслуживание
- `TrackingService.kt` - Трекинг в реальном времени

#### Data Entities
- `Vehicle.kt` - Транспортное средство
- `Driver.kt` - Водитель
- `FuelTransaction.kt` - Транзакция топлива
- `MaintenanceRecord.kt` - Запись о техническом обслуживании
- `Location.kt` - Локация транспортного средства

## Functionality

### Vehicle Management
- Создание и редактирование транспортных средств
- Управление информацией о технике (марка, модель, год выпуска)
- Отслеживание состояния транспортных средств
- Управление страховкой и ПОГ

### Driver Management
- Регистрация и управление водителями
- Управление сменами и графиком работы
- Проверка прав и квалификаций
- Геймификация для мотивации водителей

### Fuel Monitoring
- Контроль расхода топлива
- Анализ эффективности использования топлива
- Уведомления о необычном расходе
- Интеграция с топливными картами

### Maintenance Management
- Планирование технического обслуживания
- Запись о выполненных работах
- Управление запасными частями
- Уведомления о необходимости обслуживания

### Real-Time Tracking
- Трекинг транспортных средств в реальном времени
- Интеграция с GPS трекерами
- Отслеживание маршрута и времени доставки
- Уведомления о нарушениях маршрута

## Technology Stack
- Spring Boot 3.2.4
- Kotlin 1.9.23
- PostgreSQL 18
- PostGIS для геоданных
- MQTT для IoT интеграции
- WebSocket для real-time обновлений
- Docker для контейнеризации

## Configuration

### Application Properties
```properties
# Server configuration
server.port=8084

# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/fms_db
spring.datasource.username=nodeorb
spring.datasource.password=nodeorb_dev_password

# GIS integration configuration
gis.integration=HERE Maps
maps.api.key=your-here-api-key

# MQTT configuration
mqtt.broker=localhost:1883
mqtt.username=mqtt_user
mqtt.password=mqtt_password

# Tracking configuration
tracking.update.interval=5000
tracking.history.retention.days=90
```

## Running the Service

### Local Development
```bash
cd c:\Project\Nodeorb
./gradlew :fms-service:compileKotlin
./gradlew :fms-service:test
./gradlew :fms-service:bootRun
```

### Docker Deployment
```bash
cd c:\Project\Nodeorb
docker build -t fms-service:latest -f fms-service/Dockerfile .
docker run -d -p 8084:8084 --network nodeorb-network fms-service:latest
```

## API

### REST Endpoints

#### Vehicle Management
- `GET /api/v1/fms/vehicles` - Список транспортных средств
- `POST /api/v1/fms/vehicles` - Создать транспортное средство
- `GET /api/v1/fms/vehicles/{id}` - Получить транспортное средство
- `PUT /api/v1/fms/vehicles/{id}` - Обновить транспортное средство
- `DELETE /api/v1/fms/vehicles/{id}` - Удалить транспортное средство
- `GET /api/v1/fms/vehicles/{id}/history` - Получить историю местоположения

#### Driver Management
- `GET /api/v1/fms/drivers` - Список водителей
- `POST /api/v1/fms/drivers` - Создать водителя
- `GET /api/v1/fms/drivers/{id}` - Получить водителя
- `PUT /api/v1/fms/drivers/{id}` - Обновить водителя
- `DELETE /api/v1/fms/drivers/{id}` - Удалить водителя
- `GET /api/v1/fms/drivers/{id}/trips` - Получить историю поездок

#### Fuel Monitoring
- `GET /api/v1/fms/fuel/transactions` - Список транзакций топлива
- `POST /api/v1/fms/fuel/transactions` - Создать транзакцию топлива
- `GET /api/v1/fms/fuel/vehicles/{id}/consumption` - Расход топлива по транспортному средству
- `GET /api/v1/fms/fuel/efficiency` - Эффективность использования топлива

#### Maintenance
- `GET /api/v1/fms/maintenance/records` - Список записей о обслуживании
- `POST /api/v1/fms/maintenance/records` - Создать запись о обслуживании
- `GET /api/v1/fms/maintenance/vehicles/{id}` - Обслуживание транспортного средства
- `POST /api/v1/fms/maintenance/schedule` - Планировать обслуживание

#### Tracking
- `GET /api/v1/fms/tracking/vehicles` - Текущее местоположение всех транспортных средств
- `GET /api/v1/fms/tracking/vehicles/{id}` - Текущее местоположение транспортного средства
- `GET /api/v1/fms/tracking/vehicles/{id}/route` - Маршрут транспортного средства

## Integrations

### External Systems
- **HERE Maps / OpenStreetMap**: Геоданные и картографические сервисы
- **IoT датчики**: Интеграция с датчиками транспортных средств
- **ГЛОНАСС/GPS трекеры**: Для трекинга местоположения
- **OBD-II системы**: Для мониторинга состояния двигателя
- **TMS**: Для маршрутизации и планирования

### Protocols
- **MQTT**: Для связи с IoT устройствами
- **WebSocket**: Для real-time обновлений
- **REST API**: Внешние API для интеграций

## Security & Compliance

### Security Features
- Аутентификация водителей
- Шифрование данных GPS
- Защита от взлома IoT
- Режим стратегической защиты

### Compliance Requirements
- FMCSA (Federal Motor Carrier Safety Administration)
- ATA (American Trucking Association)
- Европейские транспортные стандарты

## Contributing
Please follow the project guidelines for contributing. Create a feature branch, make your changes, and submit a pull request.

## License
MIT License - see LICENSE file for details.