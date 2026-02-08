# Сервис управления транспортом (Transportation Management Service - TMS)

## Overview
Сервис для планирования и оптимизации транспортных маршрутов с учетом географических ограничений для логистической платформы Nodeorb. Обеспечивает мультимодальную маршрутизацию и расчет углеродного следа.

## Structure

### Core Modules
- `com.tmsservice.core` - Основная бизнес-логика
- `com.tmsservice.data.entities` - JPA сущности для хранения данных
- `com.tmsservice.services` - Сервисный слой для бизнес-операций
- `com.tmsservice.controllers` - REST API контроллеры
- `com.tmsservice.integration` - Интеграции с внешними системами
- `com.tmsservice.config` - Конфигурационные классы

### Key Classes

#### Core Business Logic
- `RoutePlanningService.kt` - Планирование маршрутов
- `RouteOptimizationService.kt` - Оптимизация маршрутов
- `CarbonFootprintService.kt` - Расчет углеродного следа
- `GeofencingService.kt` - Управление геозаборами
- `TrafficMonitoringService.kt` - Мониторинг дорожного трафика

#### Data Entities
- `Route.kt` - Маршрут с точками
- `Waypoint.kt` - Точка маршрута
- `RouteOptimization.kt` - Результат оптимизации
- `CarbonFootprint.kt` - Углеродный след
- `Geofence.kt` - Геозабор

## Functionality

### Route Planning
- Планирование мультимодальных маршрутов
- Расчет времени и стоимости доставки
- Учет ограничений по типам транспорта
- Планирование маршрутов для грузов с особенностями

### Route Optimization
- Оптимизация загрузки транспортных средств
- Минимизация времени доставки
- Учет пробок и дорожных заторов
- Динамическое перемаршрутизация

### Carbon Footprint Calculation
- Мониторинг выбросов CO2
- Оптимизация для ESG compliance
- Отчетность по CSRD
- Учет углеродных квот

### Geofencing
- Региональные ограничения
- Зоны экологических ограничений
- Временные ограничения движения
- Стратегические безопасные зоны
- Блокировка доступа вне заданного коридора

### Traffic Monitoring
- Мониторинг дорожного трафика
- Пробки и задержки
- Интеграция с системами дорожного трафика
- Предупреждения о задержках

## Technology Stack
- Spring Boot 3.2.4
- Kotlin 1.9.23
- PostgreSQL 18
- PostGIS для геоданных
- HERE Maps API для маршрутизации
- Graph-based routing для оптимизации
- ML модели для прогнозирования
- WebSocket для real-time обновлений
- Docker для контейнеризации

## Configuration

### Application Properties
```properties
# Server configuration
server.port=8088

# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/tms_db
spring.datasource.username=nodeorb
spring.datasource.password=nodeorb_dev_password

# Maps integration configuration
maps.provider=HERE
maps.api.key=your-api-key

# Routing configuration
routing.algorithm=A*
routing.optimization.enabled=true
routing.traffic.consider=true

# Carbon calculation configuration
carbon.calculation.enabled=true
carbon.calculation.method=ghg-protocol

# Geofencing configuration
geofencing.refresh.interval=60000
```

## Running the Service

### Local Development
```bash
cd c:\Project\Nodeorb
./gradlew :tms-service:compileKotlin
./gradlew :tms-service:test
./gradlew :tms-service:bootRun
```

### Docker Deployment
```bash
cd c:\Project\Nodeorb
docker build -t tms-service:latest -f tms-service/Dockerfile .
docker run -d -p 8088:8088 --network nodeorb-network tms-service:latest
```

## API

### REST Endpoints

#### Route Planning
- `POST /api/v1/tms/routes` - Создать маршрут
- `GET /api/v1/tms/routes/{id}` - Получить маршрут
- `PUT /api/v1/tms/routes/{id}` - Обновить маршрут
- `DELETE /api/v1/tms/routes/{id}` - Удалить маршрут
- `GET /api/v1/tms/routes/optimize` - Оптимизировать маршрут

#### Carbon Footprint
- `POST /api/v1/tms/carbon/calculate` - Рассчитать углеродный след
- `GET /api/v1/tms/carbon/report` - Сгенерировать отчет
- `GET /api/v1/tms/carbon/history` - История рассчетов

#### Geofencing
- `GET /api/v1/tms/geofences` - Список геозаборов
- `POST /api/v1/tms/geofences` - Создать геозабор
- `GET /api/v1/tms/geofences/{id}` - Получить геозабор
- `PUT /api/v1/tms/geofences/{id}` - Обновить геозабор
- `DELETE /api/v1/tms/geofences/{id}` - Удалить геозабор
- `POST /api/v1/tms/geofences/check` - Проверить попадание в геозабор

#### Traffic Monitoring
- `GET /api/v1/tms/traffic` - Состояние дорожного трафика
- `GET /api/v1/tms/traffic/route/{id}` - Трафик по маршруту
- `GET /api/v1/tms/traffic/alerts` - Уведомления о трафике

## Integrations

### External Systems
- **HERE Maps / Google Maps API**: Картографические сервисы
- **GIS источники данных**: Геоданные и карты
- **Погодные сервисы**: Данные о погоде
- **Системы дорожного трафика**: Интеграция с камерами и датчиками
- **FMS**: Актуальное состояние флота

### Protocols
- **REST API**: Внешние API для интеграций
- **WebSocket**: Для real-time обновлений
- **MQTT**: Для связи с IoT устройствами

## Sustainability

### Carbon Footprint Management
- Мониторинг выбросов CO2
- Оптимизация для ESG compliance
- Отчетность по CSRD
- Учет углеродных квот

### Strategic Mode
- Приоритетные маршруты для экстренных поставок
- Обход политически нестабильных зон
- Резервные маршруты для критической инфраструктуры

## Security & Compliance

### Security Features
- Валидация всех координат
- Защита от DoS атак на routing
- Кодирование маршрутов
- Аудит изменений геозаборов

### Compliance Requirements
- Транспортное законодательство стран
- Экологические нормы (EU Green Deal)
- Требования безопасности цепочки поставок

## Contributing
Please follow the project guidelines for contributing. Create a feature branch, make your changes, and submit a pull request.

## License
MIT License - see LICENSE file for details.