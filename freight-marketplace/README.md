# Freight Marketplace Service

Микросервис для управления фрахтовым маркетплейсом в логистической экосистеме Nodeorb. Сервис обеспечивает создание заказов на перевозку, размещение ставок перевозчиками и автоматическое сопоставление заказов.

## Архитектура

### Основные компоненты
- **FreightOrderService**: Управление заказами на перевозку
- **BidMatchingAlgorithm**: Алгоритм автоматического сопоставления ставок
- **FreightOrderController**: REST API для работы с заказами и ставками

### Технологический стек
- Kotlin + Spring Boot
- PostgreSQL + PostGIS для геопространственных данных
- Kafka для событийной архитектуры
- JWT аутентификация через OAuth2
- Kubernetes для оркестрации

## Функциональность

### Основные возможности
- Создание заказов на перевозку с геопространственными данными
- Размещение ставок перевозчиками
- Автоматическое сопоставление заказов и ставок
- Поиск и фильтрация открытых заказов
- Управление статусами заказов и ставок

### Бизнес``` логика
- Весовое сопоставление ставок (цена, репутация, близость)
- Автоматическое присвоение заказов при достижении порогового значения
- Валидация геофенсов и маршрутов

## API Endpoints

### Заказы
- `POST /api/v1/freight-marketplace/orders` - Создать заказ
- `GET /api/v1/freight-marketplace/orders/{id}` - Получить заказ
- `GET /api/v1/freight-marketplace/orders` - Мои заказы
- `GET /api/v1/freight-marketplace/orders/open` - Открытые заказы

### Ставки
- `POST /api/v1/freight-marketplace/orders/{orderId}/bids` - Разместить ставку
- `GET /api/v1/freight-marketplace/orders/{orderId}/bids` - Ставки по заказу
- `POST /api/v1/freight-marketplace/orders/{orderId}/bids/{bidId}/award` - Присвоить заказ
- `GET /api/v1/freight-marketplace/bids/my` - Мои ставки

## Конфигурация

### Настройки приложения
```yaml
freight:
  marketplace:
    auction:
      bid-expiration-hours: 24
      max-bids-per-order: 10
      auto-award-threshold: 0.8
    matching:
      algorithm: weighted
      price-weight: 0.4
      reputation-weight: 0.3
      proximity-weight: 0.3
      min-match-score: 0.6
```

### База данных
- PostgreSQL для основных данных
- PostGIS для геопространственных операций
- Миграции через Hibernate ddl-auto

### Kafka топики
- `freight-marketplace.order.created`
- `freight-marketplace.bid.placed`
- `freight-marketplace.order.awarded`

## Развертывание

### Локальная разработка
```bash
# Запуск с Docker Compose
cd freight-marketplace
docker-compose up -d

# Запуск приложения
./gradlew bootRun
```

### Контейнеризация
```bash
# Сборка образа
docker build -t freight-marketplace:latest .

# Запуск контейнера
docker run -p 8084:8084 freight-marketplace:latest
```

### Kubernetes
```bash
# Применение конфигурации
kubectl apply -f k8s/deployment.yml
```

## Тестирование

### Unit тесты
```bash
./gradlew test
```

### Интеграционные тесты
```bash
./gradlew integrationTest
```

## Безопасность

### Аутентификация
- JWT токены через Keycloak
- RBAC с контекстно-зависимыми политиками
- Принцип минимальных привилегий

### Соответствие требованиям
- GDPR для данных ЕС
- FedRAMP для государственных операций
- CMMC Level 3 для защиты данных

## Мониторинг

### Health checks
- `/actuator/health` - статус сервиса
- `/actuator/metrics` - метрики производительности

### Логирование
- Структурированные логи в JSON формате
- Аудит всех операций с заказами

## Интеграции

### Внутренние сервисы
- OMS для управления заказами
- TMS для планирования маршрутов
- FMS для управления флотом
- SCM для безопасности

### Внешние системы
- SAP/Oracle для ERP интеграции
- Shopify для электронной коммерции
- Правительственные системы для compliance

## Производительность

### Ожидаемые нагрузки
- До 1000 заказов в день
- До 5000 ставок в день
- Отклик API < 200ms (p95)

### Балансировка нагрузки
- Автоматическое масштабирование в Kubernetes
- Кеширование Redis для часто запрашиваемых данных

## Поддержка

### Документация
- OpenAPI спецификация: `/api-docs`
- Документация архитектуры в Confluence

### Контакты
- Разработка: engineering-team@nodeorb.com
- Безопасность: security-team@nodeorb.com
- Операции: platform-team@nodeorb.com

## Лицензия

Проприетарная лицензия Nodeorb Logistics Platform