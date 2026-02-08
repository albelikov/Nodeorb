# Сущности сервиса безопасности и соответствия (SCM - Security & Compliance Management) и их взаимодействие с сервисами Nodeorb

## Входные сущности (Input Entities)

### 1. Проверка соответствия
- **ComplianceCheck** - Взаимодействие с сервисами:
  - `Freight Marketplace` - Проверка предложения
  - `OMS (Order Management System)` - Проверка заказа
  - `TMS (Transportation Management System)` - Проверка маршрута
- **ManualEntryValidation** - Взаимодействие с сервисами:
  - `Freight Marketplace` - Проверка ручного ввода
  - `Analytics & Reporting` - Сбор статистики

### 2. Рекламации и апелляции
- **Appeal** - Взаимодействие с сервисами:
  - `User Portal` - Запрос апелляции
  - `Admin Panel` - Обработка апелляции
- **PriceReference** - Взаимодействие с сервисами:
  - `Marketplace Analytics` - Получение рыночных данных
  - `Business Intelligence` - Аналитика данных

### 3. Безопасность и контроль доступа
- **SecurityEvent** - Взаимодействие с сервисами:
  - `Security Event Bus` - Отправка события безопасности
  - `Admin Panel` - Мониторинг событий
- **Geofence** - Взаимодействие с сервисами:
  - `TMS` - Проверка маршрута
  - `FMS (Fleet Management System)` - Проверка местоположения

## Выходные сущности (Output Entities)

### 1. Проверка соответствия
- **ComplianceResult** - Взаимодействие с сервисами:
  - `Freight Marketplace` - Результат проверки
  - `OMS` - Обновление статуса заказа
- **ManualEntryValidationResult** - Взаимодействие с сервисами:
  - `Freight Marketplace` - Результат проверки
  - `User Portal` - Информация о результате

### 2. Рекламации и апелляции
- **AppealStatus** - Взаимодействие с сервисами:
  - `User Portal` - Информация о статусе апелляции
  - `Admin Panel` - Обновление статуса
- **PriceReferenceResult** - Взаимодействие с сервисами:
  - `Freight Marketplace` - Результат проверки
  - `User Portal` - Информация о рыночной цене

### 3. Безопасность и контроль доступа
- **SecurityEventStatus** - Взаимодействие с сервисами:
  - `Security Event Bus` - Обновление статуса события
  - `Admin Panel` - Мониторинг событий
- **GeofenceViolation** - Взаимодействие с сервисами:
  - `TMS` - Обновление статуса маршрута
  - `FMS` - Уведомление о нарушении

## Внутренние сущности (Internal Processing)

### 1. Проверка соответствия
- **ComplianceEngine** - Взаимодействие с сервисами:
  - `ComplianceCheck` - Проверка соответствия
  - `SCM Database` - Хранение результатов
- **ManualEntryValidator** - Взаимодействие с сервисами:
  - `ManualEntryValidation` - Проверка ручного ввода
  - `Market Price Medians` - Сравнение с рыночной ценой

### 2. Рекламации и апелляции
- **AppealProcessor** - Взаимодействие с сервисами:
  - `Appeal` - Обработка апелляции
  - `Admin Panel` - Отправка уведомления
- **PriceReferenceProcessor** - Взаимодействие с сервисами:
  - `PriceReference` - Получение рыночных данных
  - `Marketplace Analytics` - Анализ данных

### 3. Безопасность и контроль доступа
- **SecurityEventProcessor** - Взаимодействие с сервисами:
  - `SecurityEvent` - Обработка события
  - `Security Event Bus` - Отправка уведомления
- **GeofenceValidator** - Взаимодействие с сервисами:
  - `Geofence` - Проверка маршрута
  - `TMS` - Обновление статуса

## Сводная таблица взаимодействий с сервисами Nodeorb

| Сервис Nodeorb                  | Сущности, с которыми взаимодействует |
|---------------------------------|-------------------------------------|
| **Freight Marketplace** | ComplianceCheck, ManualEntryValidation, ComplianceResult, ManualEntryValidationResult, PriceReferenceResult |
| **OMS (Order Management System)** | ComplianceCheck, ComplianceResult |
| **TMS (Transportation Management System)** | ComplianceCheck, Geofence, GeofenceViolation |
| **FMS (Fleet Management System)** | Geofence, GeofenceViolation |
| **User Portal** | Appeal, AppealStatus, ManualEntryValidationResult, PriceReferenceResult |
| **Admin Panel** | Appeal, AppealStatus, SecurityEvent, SecurityEventStatus |
| **Security Event Bus** | SecurityEvent, SecurityEventStatus |
| **Marketplace Analytics** | PriceReference, PriceReferenceProcessor |
| **Business Intelligence** | PriceReference |
| **Analytics & Reporting** | ManualEntryValidation |
| **SCM Database** | ComplianceEngine |
| **Market Price Medians** | ManualEntryValidator |

## Сущности и их свойства

### Appeal (Апелляция)
- `id: UUID` - Идентификатор апелляции
- `recordHash: String` - Хэш записи (уникальный)
- `justification: String` - Обоснование апелляции
- `evidenceUrl: String` - URL на доказательства
- `status: String` - Статус апелляции (PENDING, APPROVED, REJECTED)
- `createdAt: LocalDateTime` - Время создания
- `reviewedAt: LocalDateTime?` - Время рассмотрения
- `reviewNotes: String?` - Комментарии к рассмотрению

### ManualEntryValidation (Проверка ручного ввода)
- `id: UUID` - Идентификатор проверки
- `entryType: String` - Тип записи
- `entryData: String` - Данные записи
- `validationResult: String` - Результат проверки
- `marketPrice: BigDecimal?` - Рыночная цена
- `priceDifference: BigDecimal?` - Разница в цене
- `justification: String?` - Обоснование
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### MarketPriceMedians (Рыночные медианные цены)
- `id: UUID` - Идентификатор записи
- `cargoType: String` - Тип груза
- `origin: String` - Место отправки
- `destination: String` - Место назначения
- `medianPrice: BigDecimal` - Медианная цена
- `priceRangeMin: BigDecimal` - Минимальная цена
- `priceRangeMax: BigDecimal` - Максимальная цена
- `dataPoints: Int` - Количество данных
- `lastUpdated: LocalDateTime` - Время последнего обновления

### SecurityEvent (Событие безопасности)
- `id: UUID` - Идентификатор события
- `eventType: String` - Тип события
- `description: String` - Описание события
- `severity: String` - Уровень серьезности
- `status: String` - Статус события
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### Geofence (Геоограничение)
- `id: UUID` - Идентификатор геозоны
- `name: String` - Название геозоны
- `description: String?` - Описание геозоны
- `geometry: String` - Геометрия геозоны (WKT)
- `type: String` - Тип геозоны
- `status: String` - Статус геозоны
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### ComplianceCheck (Проверка соответствия)
- `id: UUID` - Идентификатор проверки
- `checkType: String` - Тип проверки
- `targetId: UUID` - Идентификатор целевого объекта
- `targetType: String` - Тип целевого объекта
- `result: String` - Результат проверки
- `details: String?` - Детали проверки
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления