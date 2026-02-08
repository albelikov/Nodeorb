# Сущности транспортного сервиса (Transportation Management Service - TMS) и их взаимодействие с сервисами Nodeorb

## Входные сущности (Input Entities)

### 1. Заказы и поставки
- **Order** - Взаимодействие с сервисами:
  - `OMS (Order Management System)` - Получение данных о заказе
  - `WMS (Warehouse Management System)` - Проверка наличия товаров на складе
- **Shipment** - Взаимодействие с сервисами:
  - `FMS (Fleet Management System)` - Назначение транспортного средства и водителя
  - `OMS` - Связь с заказом
  - `WMS` - Проверка наличия груза на складе

### 2. Маршруты и планирование
- **Route** - Взаимодействие с сервисами:
  - `FMS` - Проверка доступности транспортных средств для маршрута
  - `Analytics & Reporting` - Анализ исторических данных о маршрутах
- **RouteWaypoint** - Взаимодействие с сервисами:
  - `WMS` - Координаты склада для загрузки/разгрузки
  - `Customer Portal` - Информация о точках маршрута для клиента

### 3. Груз и его характеристики
- **CargoDetails** - Взаимодействие с сервисами:
  - `WMS` - Получение данных о грузе
  - `FMS` - Проверка соответствия груза транспортному средству
  - `SCM (Safety & Compliance Management)` - Проверка на наличие опасных грузов

### 4. Обновления местоположения
- **LocationHistory** - Взаимодействие с сервисами:
  - `FMS` - Получение данных о местоположении транспортного средства
  - `Customer Portal` - Отображение истории местоположения для клиента
  - `Analytics & Reporting` - Анализ данных о движении транспортных средств

## Выходные сущности (Output Entities)

### 1. Планирование маршрутов и доставки
- **Route** - Взаимодействие с сервисами:
  - `FMS` - Отправка маршрута для транспортного средства
  - `Customer Portal` - Информация о маршруте для клиента
  - `Analytics & Reporting` - Сбор данных о маршрутах
- **Shipment** - Взаимодействие с сервисами:
  - `OMS` - Обновление статуса заказа
  - `Customer Portal` - Отображение статуса доставки

### 2. Документы и подтверждения
- **TransportDocument** - Взаимодействие с сервисами:
  - `WMS` - Отправка документов для разгрузки
  - `Customer Portal` - Предоставление документов клиенту
  - `Archive System` - Архивное хранение документов

### 3. Аналитика и отчетность
- **RouteAnalytics** - Взаимодействие с сервисами:
  - `Analytics & Reporting` - Сбор данных о эффективности маршрутов
  - `FMS` - Аналитика использования транспортных средств

## Внутренние сущности (Internal Processing)

### 1. Планирование и оптимизация
- **RouteCalculationParameters** - Взаимодействие с сервисами:
  - `FMS` - Параметры транспортных средств
  - `WMS` - Координаты складов
  - `Analytics & Reporting` - Исторические данные о маршрутах
- **RouteOptimizationResult** - Взаимодействие с сервисами:
  - `Route` - Обновление маршрута
  - `Analytics & Reporting` - Сбор данных о оптимизации

### 2. Обновления местоположения
- **LocationUpdateProcessor** - Взаимодействие с сервисами:
  - `FMS` - Обработка обновлений местоположения
  - `Customer Portal` - Отправка уведомлений клиенту

## Сводная таблица взаимодействий с сервисами Nodeorb

| Сервис Nodeorb                  | Сущности, с которыми взаимодействует |
|---------------------------------|-------------------------------------|
| **OMS (Order Management System)** | Order, Shipment |
| **FMS (Fleet Management System)** | Shipment, Route, CargoDetails, LocationHistory, RouteCalculationParameters |
| **WMS (Warehouse Management System)** | Shipment, RouteWaypoint, CargoDetails, TransportDocument, RouteCalculationParameters |
| **SCM (Safety & Compliance Management)** | CargoDetails |
| **Customer Portal** | Route, Shipment, RouteWaypoint, LocationHistory, TransportDocument |
| **Analytics & Reporting** | Route, LocationHistory, RouteAnalytics, RouteCalculationParameters, RouteOptimizationResult |
| **Archive System** | TransportDocument |

## Сущности и их свойства

### Shipment (Перевозка)
- `id: Long?` - Идентификатор перевозки
- `shipmentNumber: String` - Номер перевозки (уникальный)
- `orderId: Long?` - Идентификатор заказа
- `route: Route?` - Маршрут перевозки
- `carrierId: Long?` - Идентификатор перевозчика
- `vehicleId: Long?` - Идентификатор транспортного средства
- `driverId: Long?` - Идентификатор водителя
- `pickupAddress: String` - Адрес загрузки
- `pickupLatitude: Double` - Широта адреса загрузки
- `pickupLongitude: Double` - Долгота адреса загрузки
- `pickupDateTimeStart: Instant` - Время начала загрузки
- `pickupDateTimeEnd: Instant` - Время окончания загрузки
- `actualPickupDateTime: Instant?` - Фактическое время загрузки
- `deliveryAddress: String` - Адрес доставки
- `deliveryLatitude: Double` - Широта адреса доставки
- `deliveryLongitude: Double` - Долгота адреса доставки
- `deliveryDateTimeStart: Instant` - Время начала доставки
- `deliveryDateTimeEnd: Instant` - Время окончания доставки
- `actualDeliveryDateTime: Instant?` - Фактическое время доставки
- `cargo: CargoDetails` - Детали груза
- `baseRate: BigDecimal?` - Базовая ставка
- `fuelSurcharge: BigDecimal?` - Надбавка за топливо
- `accessorialCharges: BigDecimal?` - Дополнительные расходы
- `totalCost: BigDecimal?` - Общая стоимость
- `status: ShipmentStatus` - Статус перевозки
- `currentLatitude: Double?` - Текущая широта
- `currentLongitude: Double?` - Текущая долгота
- `lastLocationUpdate: Instant?` - Время последнего обновления местоположения
- `estimatedArrival: Instant?` - Ожидаемое время прибытия
- `hasProofOfDelivery: Boolean` - Наличие подтверждения доставки
- `hasBillOfLading: Boolean` - Наличие накладной
- `hasCMR: Boolean` - Наличие СMR
- `createdAt: Instant` - Время создания
- `updatedAt: Instant` - Время последнего обновления

### Route (Маршрут)
- `id: Long?` - Идентификатор маршрута
- `routeNumber: String` - Номер маршрута (уникальный)
- `originAddress: String` - Адрес начала маршрута
- `originLatitude: Double` - Широта начала маршрута
- `originLongitude: Double` - Долгота начала маршрута
- `destinationAddress: String` - Адрес конца маршрута
- `destinationLatitude: Double` - Широта конца маршрута
- `destinationLongitude: Double` - Долгота конца маршрута
- `totalDistance: Double?` - Общая длина маршрута (в км)
- `totalDuration: Int?` - Общая продолжительность маршрута (в минутах)
- `estimatedCost: Double?` - Ожидаемая стоимость маршрута
- `fuelConsumption: Double?` - Расход топлива
- `co2Emissions: Double?` - Выбросы CO2
- `geometry: String?` - Геометрия маршрута (PostGIS LineString)
- `vehicleType: String?` - Тип транспортного средства
- `optimizationType: String?` - Тип оптимизации
- `status: String?` - Статус маршрута
- `calculatedAt: Instant` - Время расчета маршрута
- `createdBy: Long?` - Идентификатор пользователя, создавшего маршрут

### CargoDetails (Детали груза)
- `weight: BigDecimal` - Вес груза (в кг)
- `volume: BigDecimal` - Объем груза (в м³)
- `packageCount: Int` - Количество упаковок
- `cargoType: CargoType` - Тип груза
- `description: String?` - Описание груза
- `specialHandling: String?` - Специальные условия обработки
- `temperatureMin: BigDecimal?` - Минимальная температура
- `temperatureMax: BigDecimal?` - Максимальная температура
- `hazmat: Boolean` - Наличие опасных грузов
- `hazmatClass: String?` - Класс опасных грузов

### RouteWaypoint (Точка маршрута)
- `id: Long?` - Идентификатор точки маршрута
- `route: Route` - Маршрут
- `sequenceNumber: Int` - Номер точки в последовательности
- `address: String` - Адрес точки
- `latitude: Double` - Широта точки
- `longitude: Double` - Долгота точки
- `type: WaypointType` - Тип точки (загрузка, разгрузка, промежуточная)
- `name: String?` - Название точки
- `timeWindowStart: Instant?` - Время начала окна доступности
- `timeWindowEnd: Instant?` - Время окончания окна доступности

### LocationHistory (История местоположения)
- `id: Long?` - Идентификатор записи
- `shipment: Shipment` - Перевозка
- `latitude: Double` - Широта
- `longitude: Double` - Долгота
- `timestamp: Instant` - Время записи
- `accuracy: Double?` - Точность записи
- `speed: Double?` - Скорость транспортного средства