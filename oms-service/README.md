# Сервис управления заказами (Order Management Service - OMS)

## Overview
Центральный сервис для полного управления жизненным циклом заказов в логистике для платформы Nodeorb. Обладает возможностями создания, обработки и отслеживания заказов.

## Structure

### Core Modules
- `com.omsservice.core` - Основная бизнес-логика
- `com.omsservice.data.entities` - JPA сущности для хранения данных
- `com.omsservice.services` - Сервисный слой для бизнес-операций
- `com.omsservice.controllers` - REST API контроллеры
- `com.omsservice.integration` - Интеграции с внешними системами
- `com.omsservice.config` - Конфигурационные классы

### Key Classes

#### Core Business Logic
- `OrderManagementService.kt` - Управление жизненным циклом заказов
- `PricingService.kt` - Расчет стоимости доставки
- `RoutePlanningService.kt` - Выбор оптимального маршрута
- `TrackingService.kt` - Отслеживание статуса заказа
- `ReturnsManagementService.kt` - Обработка возвратов и рекламаций

#### Data Entities
- `Order.kt` - Заказ с деталями и статусом
- `OrderItem.kt` - Позиция заказа
- `Customer.kt` - Клиент
- `DeliveryAddress.kt` - Адрес доставки
- `Return.kt` - Возврат заказа

## Functionality

### Order Management
- Создание и обработка заказов
- Проверка доступности товаров
- Управление статусами заказов
- Отмена заказов и рефаунд

### Pricing & Quoting
- Расчет стоимости доставки
- Применение скидок и промокодов
- Генерация котировок
- Интеграция с тарифными системами

### Route Planning
- Выбор оптимального маршрута
- Расчет времени доставки
- Учет геогранических ограничений
- Интеграция с картографическими сервисами

### Tracking & Visibility
- Отслеживание статуса заказа в реальном времени
- Обновления через WebSocket
- Уведомления о изменениях статуса
- Интеграция с трекинговыми системами

### Returns & Refunds
- Обработка возвратов и рекламаций
- Возврат товаров в склад
- Решение споров
- Обратная связь с клиентами

## Technology Stack
- Spring Boot 3.2.4
- Kotlin 1.9.23
- PostgreSQL 18
- Spring Data JPA
- Apache Kafka для событийной архитектуры
- Redis для кэширования
- PostGIS для геоданных
- Docker для контейнеризации

## Configuration

### Application Properties
```properties
# Server configuration
server.port=8086

# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/oms_db
spring.datasource.username=nodeorb
spring.datasource.password=nodeorb_dev_password

# Kafka configuration
kafka.bootstrap.servers=localhost:9092
kafka.consumer.group-id=oms-group

# Redis configuration
spring.redis.host=localhost
spring.redis.port=6379

# Geolocation configuration
geo.integration=HERE Maps
geo.api.key=your-api-key
```

## Running the Service

### Local Development
```bash
cd c:\Project\Nodeorb
./gradlew :oms-service:compileKotlin
./gradlew :oms-service:test
./gradlew :oms-service:bootRun
```

### Docker Deployment
```bash
cd c:\Project\Nodeorb
docker build -t oms-service:latest -f oms-service/Dockerfile .
docker run -d -p 8086:8086 --network nodeorb-network oms-service:latest
```

## API

### REST Endpoints

#### Order Management
- `GET /api/v1/oms/orders` - Список заказов
- `POST /api/v1/oms/orders` - Создать заказ
- `GET /api/v1/oms/orders/{id}` - Получить заказ
- `PUT /api/v1/oms/orders/{id}` - Обновить заказ
- `DELETE /api/v1/oms/orders/{id}` - Удалить заказ
- `POST /api/v1/oms/orders/{id}/cancel` - Отменить заказ

#### Pricing
- `POST /api/v1/oms/pricing/calculate` - Рассчитать стоимость
- `POST /api/v1/oms/pricing/quote` - Получить котировку
- `GET /api/v1/oms/pricing/discounts` - Список скидок

#### Tracking
- `GET /api/v1/oms/tracking/orders/{id}` - Отслеживание заказа
- `GET /api/v1/oms/tracking/orders/{id}/events` - История событий
- `POST /api/v1/oms/tracking/events` - Добавить событие

#### Returns
- `GET /api/v1/oms/returns` - Список возвратов
- `POST /api/v1/oms/returns` - Создать возврат
- `GET /api/v1/oms/returns/{id}` - Получить возврат
- `PUT /api/v1/oms/returns/{id}` - Обновить возврат
- `POST /api/v1/oms/returns/{id}/approve` - Одобрить возврат

## Integrations

### Internal Services
- **WMS**: Управление складскими запасами
- **TMS**: Планирование маршрутов
- **FMS**: Управление флотом
- **SCM**: Безопасность и соответствие требованиям

### External Systems
- **ERP системы**: SAP, Oracle для интеграции с учетными системами
- **Payment gateways**: PayPal, Stripe для обработки платежей
- **E-commerce platforms**: Shopify, Magento для синхронизации заказов

## Processes

### Standard Order Flow
1. **Создание заказа** → OMS
2. **Резервирование запаса** → WMS
3. **Планирование маршрута** → TMS
4. **Назначение транспорта** → FMS
5. **Доставка и отслеживание** → OMS + TMS

### Strategic Mode
При активации переопределяет обычные маршруты для приоритетных грузов.

## Security & Compliance

### Security Features
- RBAC с ABAC для клиентских данных
- Шифрование конфиденциальной информации
- Полный аудит изменений заказа
- Режим стратегической устойчивости

### Compliance Requirements
- Consumer protection laws
- Supply chain security standards
- Data privacy regulations (GDPR, CCPA)

## Contributing
Please follow the project guidelines for contributing. Create a feature branch, make your changes, and submit a pull request.

## License
MIT License - see LICENSE file for details.