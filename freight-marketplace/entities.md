# Сущности фрахтового маркетплейса (Freight Marketplace Service) и их взаимодействие с сервисами Nodeorb

## Входные сущности (Input Entities)

### 1. Фрахтовые заказы
- **FreightOrder** - Взаимодействие с сервисами:
  - `OMS (Order Management System)` - Создание и управление заказами
  - `TMS (Transportation Management System)` - Проверка доступности маршрута
  - `WMS (Warehouse Management System)` - Проверка наличия товара на складе
- **Bid** - Взаимодействие с сервисами:
  - `FMS (Fleet Management System)` - Проверка доступности транспорта
  - `SCM (Safety & Compliance Management)` - Проверка соответствия требованиям
  - `Billing & Pricing` - Расчет стоимости доставки

### 2. Профили пользователей
- **UserProfile** - Взаимодействие с сервисами:
  - `Keycloak` - Аутентификация и авторизация
  - `Analytics & Reporting` - Сбор статистики по пользователям
- **CarrierProfile** - Взаимодействие с сервисами:
  - `FMS` - Проверка наличия транспорта
  - `SCM` - Проверка соответствия требованиям

### 3. Отчеты и аналитика
- **ReportRequest** - Взаимодействие с сервисами:
  - `Analytics & Reporting` - Генерация отчета
  - `User Portal` - Запрос отчета пользователем
- **InsightRequest** - Взаимодействие с сервисами:
  - `Marketplace Analytics` - Анализ рынка
  - `Business Intelligence` - Аналитика данных

## Выходные сущности (Output Entities)

### 1. Фрахтовые заказы
- **FreightOrderStatus** - Взаимодействие с сервисами:
  - `OMS` - Обновление статуса заказа
  - `User Portal` - Информация о статусе заказа
- **BidStatus** - Взаимодействие с сервисами:
  - `User Portal` - Информация о статусе предложения
  - `Analytics & Reporting` - Сбор статистики по предложениям

### 2. Профили пользователей
- **UserProfile** - Взаимодействие с сервисами:
  - `Keycloak` - Обновление профиля
  - `User Portal` - Отображение профиля
- **CarrierProfile** - Взаимодействие с сервисами:
  - `FMS` - Обновление информации о транспорте
  - `SCM` - Проверка соответствия требованиям

### 3. Отчеты и аналитика
- **Report** - Взаимодействие с сервисами:
  - `User Portal` - Отображение отчета
  - `Analytics & Reporting` - Сохранение отчета
- **Insight** - Взаимодействие с сервисами:
  - `User Portal` - Отображение инсайта
  - `Business Intelligence` - Сохранение инсайта

## Внутренние сущности (Internal Processing)

### 1. Фрахтовые заказы
- **OrderMatching** - Взаимодействие с сервисами:
  - `FreightOrder` - Подбор подходящего предложения
  - `Bid` - Оценка предложения
- **BidProcessing** - Взаимодействие с сервисами:
  - `Bid` - Обработка предложения
  - `SCM` - Проверка соответствия требованиям

### 2. Профили пользователей
- **ProfileUpdate** - Взаимодействие с сервисами:
  - `UserProfile` - Обновление профиля
  - `Keycloak` - Обновление данных аутентификации
- **CarrierVerification** - Взаимодействие с сервисами:
  - `CarrierProfile` - Проверка профиля перевозчика
  - `SCM` - Проверка соответствия требованиям

### 3. Отчеты и аналитика
- **ReportGeneration** - Взаимодействие с сервисами:
  - `ReportRequest` - Генерация отчета
  - `Analytics & Reporting` - Сбор данных для отчета
- **InsightGeneration** - Взаимодействие с сервисами:
  - `InsightRequest` - Генерация инсайта
  - `Marketplace Analytics` - Анализ данных

## Сводная таблица взаимодействий с сервисами Nodeorb

| Сервис Nodeorb                  | Сущности, с которыми взаимодействует |
|---------------------------------|-------------------------------------|
| **OMS (Order Management System)** | FreightOrder, FreightOrderStatus |
| **TMS (Transportation Management System)** | FreightOrder |
| **WMS (Warehouse Management System)** | FreightOrder |
| **FMS (Fleet Management System)** | Bid, CarrierProfile |
| **SCM (Safety & Compliance Management)** | Bid, CarrierProfile, BidProcessing, CarrierVerification |
| **Billing & Pricing** | Bid |
| **Keycloak** | UserProfile, CarrierProfile, ProfileUpdate |
| **Analytics & Reporting** | UserProfile, BidStatus, Report, ReportGeneration |
| **Marketplace Analytics** | InsightRequest, InsightGeneration |
| **Business Intelligence** | InsightRequest, Insight |
| **User Portal** | FreightOrderStatus, BidStatus, UserProfile, CarrierProfile, ReportRequest, Report, InsightRequest, Insight |

## Сущности и их свойства

### FreightOrderEntity (Фрахтовый заказ)
- `id: UUID?` - Идентификатор заказа
- `shipperId: UUID` - Идентификатор отправителя
- `title: String` - Название заказа
- `description: String?` - Описание заказа
- `cargoType: CargoType` - Тип груза
- `weight: BigDecimal` - Вес груза (в кг)
- `volume: BigDecimal` - Объем груза (в м³)
- `pickupLocation: Point` - Место загрузки (геопозиция)
- `deliveryLocation: Point` - Место доставки (геопозиция)
- `pickupAddress: String` - Адрес загрузки
- `deliveryAddress: String` - Адрес доставки
- `requiredDeliveryDate: LocalDateTime` - Требуемая дата доставки
- `maxBidAmount: BigDecimal` - Максимальная цена предложения
- `status: OrderStatus` - Статус заказа
- `bids: MutableList<BidEntity>` - Список предложений
- `createdAt: LocalDateTime?` - Время создания заказа
- `updatedAt: LocalDateTime?` - Время последнего обновления заказа

### BidEntity (Предложение)
- `id: UUID?` - Идентификатор предложения
- `carrierId: UUID` - Идентификатор перевозчика
- `freightOrder: FreightOrderEntity?` - Фрахтовый заказ
- `masterOrder: MasterOrderEntity?` - Главный заказ
- `partialOrder: PartialOrderEntity?` - Частичный заказ
- `amount: BigDecimal` - Сумма предложения
- `proposedDeliveryDate: LocalDateTime` - Предлагаемая дата доставки
- `notes: String?` - Примечания
- `status: BidStatus` - Статус предложения
- `matchingScore: Double?` - Оценка соответствия
- `scoreBreakdown: String?` - Детализация оценки
- `createdAt: LocalDateTime?` - Время создания предложения
- `updatedAt: LocalDateTime?` - Время последнего обновления предложения

### UserProfileEntity (Профиль пользователя)
- `userId: UUID` - Идентификатор пользователя
- `companyName: String` - Название компании
- `rating: Double` - Рейтинг пользователя
- `totalOrders: Int` - Общее количество заказов
- `completedOrders: Int` - Количество выполненных заказов
- `joinedAt: LocalDateTime?` - Время регистрации
- `updatedAt: LocalDateTime?` - Время последнего обновления профиля

### MasterOrderEntity (Главный заказ)
- `id: UUID?` - Идентификатор главного заказа
- `title: String` - Название заказа
- `description: String?` - Описание заказа
- `cargoType: CargoType` - Тип груза
- `weight: BigDecimal` - Вес груза (в кг)
- `volume: BigDecimal` - Объем груза (в м³)
- `pickupLocation: Point` - Место загрузки (геопозиция)
- `deliveryLocation: Point` - Место доставки (геопозиция)
- `pickupAddress: String` - Адрес загрузки
- `deliveryAddress: String` - Адрес доставки
- `requiredDeliveryDate: LocalDateTime` - Требуемая дата доставки
- `maxBidAmount: BigDecimal` - Максимальная цена предложения
- `status: OrderStatus` - Статус заказа
- `createdAt: LocalDateTime?` - Время создания заказа
- `updatedAt: LocalDateTime?` - Время последнего обновления заказа

### PartialOrderEntity (Частичный заказ)
- `id: UUID?` - Идентификатор частичного заказа
- `masterOrder: MasterOrderEntity` - Главный заказ
- `title: String` - Название заказа
- `description: String?` - Описание заказа
- `cargoType: CargoType` - Тип груза
- `weight: BigDecimal` - Вес груза (в кг)
- `volume: BigDecimal` - Объем груза (в м³)
- `pickupLocation: Point` - Место загрузки (геопозиция)
- `deliveryLocation: Point` - Место доставки (геопозиция)
- `pickupAddress: String` - Адрес загрузки
- `deliveryAddress: String` - Адрес доставки
- `requiredDeliveryDate: LocalDateTime` - Требуемая дата доставки
- `maxBidAmount: BigDecimal` - Максимальная цена предложения
- `status: OrderStatus` - Статус заказа
- `createdAt: LocalDateTime?` - Время создания заказа
- `updatedAt: LocalDateTime?` - Время последнего обновления заказа