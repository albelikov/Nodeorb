# Сервис управления складом (Warehouse Management Service - WMS)

## Overview
Полнофункциональная система управления складскими операциями с расширенными модулями для логистической платформы Nodeorb. Обеспечивает управление запасами, роботу склада и интеграцию с роботизированными системами.

## Structure

### Core Modules
- `com.wmsservice.core` - Основная бизнес-логика
- `com.wmsservice.data.entities` - JPA сущности для хранения данных
- `com.wmsservice.services` - Сервисный слой для бизнес-операций
- `com.wmsservice.controllers` - REST API контроллеры
- `com.wmsservice.integration` - Интеграции с внешними системами
- `com.wmsservice.config` - Конфигурационные классы

### Key Classes

#### Core Business Logic
- `InventoryManagementService.kt` - Управление запасами
- `WarehouseOperationsService.kt` - Основные операции склада
- `YardManagementService.kt` - Управление двором склада
- `ReverseLogisticsService.kt` - Обратная логистика
- `AGVIntegrationService.kt` - Интеграция с роботами

#### Data Entities
- `InventoryItem.kt` - Товар на складе
- `WarehouseLocation.kt` - Место на складе
- `WarehouseOperation.kt` - Операция склада
- `YardOperation.kt` - Операция на дворе склада
- `ReturnItem.kt` - Возвращенный товар

## Functionality

### Core WMS Operations
- Приемка и размещение товаров
- Комплектация и отгрузка
- Цикличная инвентаризация
- Управление зонами склада
- Подбор оптимального места для хранения
- Управление запасами в реальном времени

### Yard Management System (YMS)
- Оптимизация движения транспорта на территории
- Управление доками и шлюзами
- Контроль времени простоя транспорта
- Планирование разгрузки/погрузки
- Отслеживание местоположения транспортных средств

### Reverse Logistics
- Обработка возвратов от клиентов
- Управление гарантийными случаями
- Контроль качества возвращенных товаров
- Переработка и утилизация
- Возврат поставщикам (RMA)
- Аналитика возвратов

### AGV & Robotics Integration
- Управление автоматическимиguided vehicles
- Интеграция с роботизированными системами
- Планирование маршрутов для AGV
- Обработка исключений в работе роботов
- Мониторинг производительности роботов

### Inventory Management
- Отслеживание уровня запасов
- Анализ потребности в запасах
- Оптимизация запаса
- Предупреждения о низком уровне запасов
- Управление просроченными товарами

## Technology Stack
- Spring Boot 3.2.4
- Kotlin 1.9.23
- PostgreSQL 18 + Redis для кэширования
- Spring Data JPA
- IoT интеграция (RFID, AGV)
- Kotlin Multiplatform для мобильных приложений
- WebSocket для real-time обновлений
- Docker для контейнеризации

## Configuration

### Application Properties
```properties
# Server configuration
server.port=8090

# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/wms_db
spring.datasource.username=nodeorb
spring.datasource.password=nodeorb_dev_password

# Redis configuration
spring.redis.host=localhost
spring.redis.port=6379

# RFID integration configuration
rfid.integration.enabled=true
rfid.reader.endpoint=localhost:8080

# AGV integration configuration
agv.controller.endpoint=localhost:9000
agv.api.key=your-api-key

# Inventory configuration
inventory.update.interval=10000
inventory.low.stock.threshold=5
```

## Running the Service

### Local Development
```bash
cd c:\Project\Nodeorb
./gradlew :wms-service:compileKotlin
./gradlew :wms-service:test
./gradlew :wms-service:bootRun
```

### Docker Deployment
```bash
cd c:\Project\Nodeorb
docker build -t wms-service:latest -f wms-service/Dockerfile .
docker run -d -p 8090:8090 --network nodeorb-network wms-service:latest
```

## API

### REST Endpoints

#### Inventory Management
- `GET /api/v1/wms/inventory` - Список запасов
- `POST /api/v1/wms/inventory` - Добавить запас
- `GET /api/v1/wms/inventory/{id}` - Получить запас
- `PUT /api/v1/wms/inventory/{id}` - Обновить запас
- `DELETE /api/v1/wms/inventory/{id}` - Удалить запас
- `GET /api/v1/wms/inventory/low-stock` - Товары с низким запасом

#### Warehouse Operations
- `GET /api/v1/wms/operations` - Список операций
- `POST /api/v1/wms/operations/receive` - Приемка товара
- `POST /api/v1/wms/operations/pick` - Комплектация
- `POST /api/v1/wms/operations/ship` - Отгрузка
- `POST /api/v1/wms/operations/putaway` - Размещение на складе

#### Yard Management
- `GET /api/v1/wms/yard/operations` - Список операций на дворе
- `POST /api/v1/wms/yard/check-in` - Приемка транспорта
- `POST /api/v1/wms/yard/check-out` - Выпуск транспорта
- `GET /api/v1/wms/yard/locations` - Места на дворе

#### Reverse Logistics
- `GET /api/v1/wms/returns` - Список возвратов
- `POST /api/v1/wms/returns` - Создать возврат
- `GET /api/v1/wms/returns/{id}` - Получить возврат
- `PUT /api/v1/wms/returns/{id}` - Обновить возврат
- `POST /api/v1/wms/returns/{id}/process` - Обработать возврат

#### AGV Management
- `GET /api/v1/wms/agv/vehicles` - Список AGV
- `GET /api/v1/wms/agv/vehicles/{id}` - Получить AGV
- `POST /api/v1/wms/agv/tasks` - Создать задачу для AGV
- `GET /api/v1/wms/agv/tasks` - Список задач
- `GET /api/v1/wms/agv/performance` - Производительность AGV

## Integrations

### Internal Services
- **OMS**: Управление заказами
- **TMS**: Планирование маршрутов
- **FMS**: Управление флотом
- **SCM**: Безопасность и соответствие требованиям

### External Systems
- **RFID системы**: Автоматическое отслеживание
- **AGV (Automated Guided Vehicles)**: Роботизированные системы
- **Сканирующие системы**: Штрих-коды, QR
- **Системы весов и измерения**: Мерческие устройства
- **Промышленные IoT датчики**: Датчики состояния
- **Программируемые логистические контроллеры**: PLC

## Mobile Applications
- **Android/iOS приложения** для складских работников
- Операции в режиме офлайн
- Сканирование штрих-кодов
- Управление задачами в реальном времени
- Отслеживание местоположения

## Security & Compliance

### Security Features
- Контроль доступа к зонам склада
- Аудит всех операций с товарами
- Инвентаризация с подтверждением
- Защита от краж и потерь
- Шифрование данных о запасах

### Compliance Requirements
- Стандарты складской логистики
- Нормативы безопасности труда
- Экологические стандарты утилизации
- Требования к цепочке поставок

## Performance
- Поддержка крупных распределительных центров
- Обработка тысяч транзакций в час
- Real-time обновление запасов
- Оптимизированные алгоритмы размещения

## Contributing
Please follow the project guidelines for contributing. Create a feature branch, make your changes, and submit a pull request.

## License
MIT License - see LICENSE file for details.