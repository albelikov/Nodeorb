# SCM Service (Security & Compliance Management)

Центральный сервис управления безопасностью и соответствием требованиям в логистической экосистеме Nodeorb.

## Архитектура

SCM является центральным узлом принятия решений (Policy Decision Point) и включает следующие компоненты:

### 1. Market Median Oracle & Manual Cost Validation
- **Файл**: `src/main/kotlin/com/internal/engine/validation/MarketOracle.kt`
- **Функция**: Валидация ручного ввода стоимостей материалов и работ
- **Логика**: Сравнение введенных данных с рыночной медианой из аналитической БД
- **Условия**:
  - Отклонение > 20%: флаг `audit_required`
  - Отклонение > 40%: статус `REJECTED` в gRPC-ответе
- **Цель**: ИИ предлагает "лучший вариант" (медиану) на основе анализа вводимых данных

### 2. External Compliance Adapters (Global Trade Management)
- **Файл**: `src/main/kotlin/com/internal/services/ExternalComplianceAdapter.kt`
- **Интерфейс**: `ExternalComplianceAdapter`
- **Реализации**:
  - `OfacComplianceAdapter` - проверка по санкционным спискам OFAC/UN
  - `ItarComplianceAdapter` - проверка ITAR/EAR ограничений
- **Функции**:
  - `checkSanctions()` - проверка контрагентов по спискам OFAC/UN
  - `checkDeniedParty()` - проверка Denied Party Screening
  - `getCheckStatus()` - получение статуса проверки

### 3. Dynamic Geofencing Logic
- **Файл**: `src/main/kotlin/com/internal/engine/validation/AccessValidator.kt`
- **Функция**: Проверка доступа к деталям груза на основе географического положения
- **Методы**:
  - `validateCargoAccess()` - проверка доступа к грузу с учетом геозон
  - `isWithinAllowedRoute()` - блокировка доступа вне заданного коридора маршрута
  - `detectGpsSpoofing()` - обнаружение подмены GPS-координат
  - `validateSensitiveDataAccess()` - проверка доступа к чувствительным данным

### 4. Evidence Package Generator
- **Файл**: `src/main/kotlin/com/internal/services/EvidencePackageGenerator.kt`
- **Функция**: Сборка "пакета доказательств" для страховых случаев
- **Возможности**:
  - Агрегация всех связанных событий по `order_id`
  - Построение цепочки хэшей (Hash Chaining)
  - Формирование подписанного JSON-объекта
  - Проверка целостности пакета
  - Экспорт/импорт пакетов

### 5. WORM Storage Layer (Infrastructure Level)
- **Файл**: `src/main/kotlin/com/internal/repository/WormStorageRepository.kt`
- **Функция**: Уровень абстракции БД с защитой от модификации
- **Характеристики**:
  - Write Once Read Many (WORM)
  - Автоматическое создание хэшей для целостности
  - Неизменяемые записи для аудита
  - Поддержка различных типов событий

### 6. Trust Score 2.0
- **Файл**: `src/main/kotlin/com/internal/services/TrustScoreService.kt`
- **Функция**: Динамический расчет Trust Score пользователя
- **Факторы**:
  - Точность ввода цен (30%)
  - Успешность апелляций (25%)
  - Биометрическое соответствие (20%)
  - Географическое соответствие (15%)
  - Временной фактор (10%)
- **Уровни доверия**: CRITICAL, LOW, MEDIUM, HIGH

## gRPC API

Обновленные методы в `api/grpc/trust_service.proto`:

### Новые методы:
- `ValidateManualEvent()` - валидация ручного ввода событий
- `CheckSanctions()` - проверка санкционных списков
- `CheckDeniedParty()` - проверка Denied Party Screening
- `ValidateCargoAccess()` - проверка доступа к грузу
- `GenerateEvidencePackage()` - генерация пакета доказательств
- `VerifyEvidencePackage()` - проверка целостности пакета
- `CalculateTrustScore()` - расчет динамического Trust Score
- `GetTrustScoreRecommendations()` - получение рекомендаций

## OpenAPI API

Новые эндпоинты в `api/openapi/scm-api.yaml`:

### Основные эндпоинты:
- `POST /api/v1/compliance/validate-event` - валидация ручного ввода
- `POST /api/v1/compliance/sanctions-check` - проверка санкций
- `POST /api/v1/compliance/denied-party-check` - проверка Denied Party
- `POST /api/v1/compliance/cargo-access` - проверка доступа к грузу
- `POST /api/v1/compliance/evidence-package` - генерация пакета доказательств
- `GET /api/v1/compliance/evidence-package/{package_id}/verify` - проверка пакета
- `POST /api/v1/compliance/trust-score/calculate` - расчет Trust Score
- `POST /api/v1/compliance/trust-score/recommendations` - рекомендации

## Интеграции

### Системы:
- **Keycloak** - аутентификация и управление пользователями
- **Marketplace** - проверка контрагентов перед авторизацией сделки
- **Analytics** - получение рыночной медианы для валидации цен
- **Security Event Bus** - отправка событий безопасности

### Протоколы:
- **gRPC** - внутренняя коммуникация между сервисами
- **REST** - внешние API для интеграций
- **WebAuthn** - биометрическая аутентификация

## Безопасность

### Особенности:
- **Zero Trust Architecture** - все запросы проходят строгую проверку
- **ABAC (Attribute-Based Access Control)** - гибкое управление доступом
- **Immutable Audit Logs** - неизменяемые логи для аудита
- **Digital Signatures** - подпись всех критических операций
- **Geofencing** - географические ограничения доступа

## Зависимости

### Основные:
- Spring Boot
- Kotlin
- gRPC
- PostgreSQL
- Redis (для кэширования)
- Kafka (для событий)

### Безопасность:
- Spring Security
- JWT
- WebAuthn
- FIPS 140-2 compliant encryption

## Запуск

```bash
# Сборка
./gradlew build

# Запуск
./gradlew bootRun

# Тестирование
./gradlew test
```

## Мониторинг

### Метрики:
- Уровень доверия пользователей
- Количество срабатываний геозон
- Статистика валидации цен
- Время ответа сервисов
- Количество апелляций

### Логирование:
- Все операции записываются в WORM хранилище
- События безопасности отправляются в Security Event Bus
- Аудит всех изменений Trust Score

## Лицензирование

Соответствует требованиям:
- **FedRAMP** - для государственных заказчиков
- **CMMC Level 3** - для оборонной промышленности
- **GDPR** - для европейских пользователей
- **ITAR/EAR** - для международной торговли

## Контакты

Для вопросов и поддержки:
- Email: security@nodeorb.com
- Slack: #scm-service
- Jira: NODEORB-SCM