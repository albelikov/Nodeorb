# Сущности сервиса управления автопарком (FMS - Fleet Management System) и их взаимодействие с сервисами Nodeorb

## Введение

FMS модуль предоставляет комплексный функционал для управления транспортным парком, водителями, техническим обслуживанием, топливным учетом и финансовой аналитикой. Система интегрирована с другими сервисами Nodeorb для обеспечения полноценной логистической цепочки.

## Входные сущности (Input Entities)

### 1. Управление транспортными средствами
- **Vehicle** - Транспортное средство с полным набором характеристик и документов
- **VehicleStatus** - Текущий статус и телематические данные ТС
- **VehicleMaintenanceRequest** - Заявка на техническое обслуживание или ремонт
- **FuelRefueling** - Заправка топливом

### 2. Управление водителями и персоналом
- **Driver** - Водитель с личными данными и квалификацией
- **DriverStatus** - Текущий статус водителя
- **DriverAssignment** - Назначение водителя на ТС или рейс
- **DriverSchedule** - График работы водителя

### 3. Планирование и диспетчеризация
- **TransportationRequest** - Заявка на перевозку
- **Route** - Маршрут перевозки
- **RouteWaypoint** - Точки маршрута
- **LocationUpdate** - Обновление местоположения ТС
- **RouteDeviation** - Отклонение от маршрута

### 4. Учет топлива и затрат
- **FuelConsumption** - Расход топлива
- **MaintenanceRecord** - Запись о выполненном ТО или ремонте
- **SparePart** - Запчасть или расходный материал
- **FinancialTransaction** - Финансовая транзакция

## Выходные сущности (Output Entities)

### 1. Управление транспортными средствами
- **VehicleAssignment** - Назначение ТС на рейс
- **VehicleMaintenance** - Плановое или текущее обслуживание
- **VehiclePerformanceReport** - Отчет по эффективности использования ТС

### 2. Управление водителями
- **DriverPerformanceReport** - Отчет по эффективности водителя
- **DriverRating** - Рейтинг водителя
- **DriverViolation** - Нарушение правил работы водителем

### 3. Планирование и диспетчеризация
- **RouteOptimizationResult** - Результат оптимизации маршрута
- **RouteDeviationAlert** - Уведомление о отклонении от маршрута
- **DispatchTask** - Задача для диспетчера
- **TransportationExecution** - Выполнение перевозки

### 4. Финансовый модуль
- **CostAnalysisReport** - Отчет по затратам
- **RevenueReport** - Отчет по доходам
- **ProfitabilityAnalysis** - Анализ рентабельности
- **PricingCalculation** - Расчет цены на перевозку

## Внутренние сущности (Internal Processing)

### 1. Управление транспортными средствами
- **VehicleAssignmentProcessor** - Обработка назначений ТС
- **VehicleMaintenanceProcessor** - Планирование и контроль обслуживания
- **VehiclePerformanceAnalyzer** - Анализ эффективности ТС

### 2. Управление водителями
- **DriverAssignmentProcessor** - Обработка назначений водителей
- **DriverScheduleProcessor** - Планирование расписания
- **DriverPerformanceAnalyzer** - Анализ эффективности водителей
- **DriverRatingCalculator** - Расчет рейтинга водителей

### 3. Планирование и диспетчеризация
- **RouteOptimizationProcessor** - Оптимизация маршрутов
- **LocationUpdateProcessor** - Обработка обновлений местоположения
- **RouteDeviationProcessor** - Обработка отклонений маршрута
- **DispatchTaskProcessor** - Обработка диспетчерских задач

### 4. Финансовый модуль
- **CostCalculator** - Расчет затрат
- **RevenueTracker** - Отслеживание доходов
- **ProfitabilityCalculator** - Расчет рентабельности
- **PricingEngine** - Двигатель ценообразования

## Сводная таблица взаимодействий с сервисами Nodeorb

| Сервис Nodeorb                  | Сущности, с которыми взаимодействует |
|---------------------------------|-------------------------------------|
| **TMS (Transportation Management System)** | Vehicle, Driver, LocationUpdate, RouteDeviation, VehicleAssignment, DriverAssignment, Route, RouteWaypoint, TransportationRequest, TransportationExecution |
| **SCM (Safety & Compliance Management)** | Vehicle, Driver, DriverViolation, VehicleMaintenance |
| **WMS (Warehouse Management System)** | Vehicle, TransportationRequest, TransportationExecution |
| **ERP System** | FinancialTransaction, CostAnalysisReport, RevenueReport, ProfitabilityAnalysis |
| **CRM System** | TransportationRequest, TransportationExecution |
| **User Portal** | VehicleStatus, DriverStatus, VehicleAssignment, VehicleMaintenance, DriverAssignment, DriverSchedule, LocationHistory, RouteDeviationAlert, DispatchTask |
| **Telematics System** | VehicleStatus, DriverStatus, LocationUpdate, FuelConsumption |
| **Maintenance System** | VehicleMaintenanceRequest, VehicleMaintenance, MaintenanceRecord, SparePart |

## Сущности и их свойства

### Vehicle (Транспортное средство)
- `id: UUID` - Идентификатор транспортного средства
- `vehicleNumber: String` - Номер транспортного средства
- `vehicleType: String` - Тип транспортного средства (автомобиль, спецтехника, строительная техника, ж/д транспорт, военная техника, мототехника)
- `make: String` - Марка
- `model: String` - Модель
- `year: Int` - Год выпуска
- `vin: String` - VIN номер
- `registrationNumber: String` - Номер регистрации
- `currentMileage: Int` - Текущий пробег
- `fuelType: String` - Тип топлива
- `fuelLevel: Int` - Уровень топлива
- `capacity: Double` - Грузоподъемность
- `volume: Double` - Объем кузова
- `bodyType: String` - Тип кузова
- `status: String` - Статус транспортного средства (в движении, на парковке, в ремонте, на техобслуживании, недоступен)
- `documents: List<VehicleDocument>` - Документы ТС
- `photos: List<String>` - Фотографии ТС
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### VehicleDocument (Документ транспортного средства)
- `id: UUID` - Идентификатор документа
- `vehicleId: UUID` - Идентификатор транспортного средства
- `documentType: String` - Тип документа (ПТС, СТС, ОСАГО, КАСКО, диагностическая карта)
- `documentNumber: String` - Номер документа
- `issueDate: LocalDateTime` - Дата выдачи
- `expiryDate: LocalDateTime` - Дата окончания действия
- `fileUrl: String` - Ссылка на файл
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### VehicleStatus (Статус транспортного средства)
- `id: UUID` - Идентификатор статуса
- `vehicleId: UUID` - Идентификатор транспортного средства
- `status: String` - Статус
- `latitude: Double` - Широта
- `longitude: Double` - Долгота
- `speed: Double` - Скорость
- `engineTemperature: Double` - Температура двигателя
- `fuelLevel: Int` - Уровень топлива
- `batteryVoltage: Double` - Напряжение батареи
- `engineRpm: Int` - Обороты двигателя
- `odometer: Int` - Пробег
- `lastUpdated: LocalDateTime` - Время последнего обновления

### VehicleMaintenanceRequest (Заявка на обслуживание)
- `id: UUID` - Идентификатор заявки
- `vehicleId: UUID` - Идентификатор транспортного средства
- `requestType: String` - Тип заявки (плановое ТО, ремонт)
- `description: String` - Описание проблемы
- `priority: String` - Приоритет
- `status: String` - Статус заявки
- `requestedBy: UUID` - Идентификатор автора заявки
- `scheduledDate: LocalDateTime` - Запланированная дата выполнения
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### VehicleMaintenance (Обслуживание транспортного средства)
- `id: UUID` - Идентификатор обслуживания
- `vehicleId: UUID` - Идентификатор транспортного средства
- `maintenanceType: String` - Тип обслуживания
- `description: String` - Описание работ
- `startDate: LocalDateTime` - Дата начала
- `endDate: LocalDateTime` - Дата окончания
- `cost: Double` - Стоимость
- `partsUsed: List<SparePart>` - Использованные запчасти
- `serviceCenter: String` - Сервисный центр
- `technician: String` - ФИО техника
- `status: String` - Статус
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### Driver (Водитель)
- `id: UUID` - Идентификатор водителя
- `firstName: String` - Имя
- `lastName: String` - Фамилия
- `middleName: String?` - Отчество
- `driverLicenseNumber: String` - Номер прав
- `driverLicenseCategory: String` - Категория прав
- `driverLicenseExpiryDate: LocalDateTime` - Срок действия прав
- `phoneNumber: String` - Телефон
- `email: String` - Email
- `medicalCertificateExpiry: LocalDateTime` - Срок действия медицинского справки
- `experience: Int` - Стаж вождения (в годах)
- `rating: Double` - Рейтинг водителя
- `status: String` - Статус водителя
- `assignedVehicleId: UUID?` - Идентификатор назначенного ТС
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### DriverStatus (Статус водителя)
- `id: UUID` - Идентификатор статуса
- `driverId: UUID` - Идентификатор водителя
- `status: String` - Статус
- `latitude: Double` - Широта
- `longitude: Double` - Долгота
- `speed: Double` - Скорость
- `workingHoursToday: Double` - Рабочие часы сегодня
- `restTimeRemaining: Double` - Оставшееся время отдыха
- `lastUpdated: LocalDateTime` - Время последнего обновления

### DriverAssignment (Назначение водителя)
- `id: UUID` - Идентификатор назначения
- `driverId: UUID` - Идентификатор водителя
- `routeId: UUID` - Идентификатор маршрута
- `vehicleId: UUID` - Идентификатор транспортного средства
- `startTime: LocalDateTime` - Время начала
- `endTime: LocalDateTime` - Время окончания
- `status: String` - Статус назначения
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### DriverSchedule (График работы водителя)
- `id: UUID` - Идентификатор расписания
- `driverId: UUID` - Идентификатор водителя
- `weekSchedule: Map<String, String>` - Еженедельное расписание
- `workingHours: Double` - Норма рабочих часов в неделю
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### DriverPerformanceReport (Отчет по эффективности водителя)
- `id: UUID` - Идентификатор отчета
- `driverId: UUID` - Идентификатор водителя
- `period: String` - Период отчета
- `totalTrips: Int` - Общее количество рейсов
- `totalDistance: Double` - Общий пробег
- `averageSpeed: Double` - Средняя скорость
- `fuelConsumption: Double` - Расход топлива
- `efficiencyRating: Double` - Рейтинг эффективности
- `punctualityRate: Double` - Процент пунктуальности
- `violations: List<DriverViolation>` - Нарушения
- `createdAt: LocalDateTime` - Время создания

### DriverViolation (Нарушение водителем)
- `id: UUID` - Идентификатор нарушения
- `driverId: UUID` - Идентификатор водителя
- `violationType: String` - Тип нарушения (превышение скорости, резкое торможение, нарушение режима отдыха)
- `violationDate: LocalDateTime` - Дата нарушения
- `location: String` - Место нарушения
- `severity: String` - Степень серьезности
- `penalty: Double` - Штраф
- `createdAt: LocalDateTime` - Время создания

### TransportationRequest (Заявка на перевозку)
- `id: UUID` - Идентификатор заявки
- `customerId: UUID` - Идентификатор клиента
- `requestType: String` - Тип заявки (грузоперевозка, пассажирская перевозка, специальный рейс)
- `cargoType: String` - Тип груза
- `cargoWeight: Double` - Вес груза
- `cargoVolume: Double` - Объем груза
- `pickupLocation: String` - Место забора
- `deliveryLocation: String` - Место доставки
- `pickupTime: LocalDateTime` - Время забора
- `deliveryTime: LocalDateTime` - Время доставки
- `priority: String` - Приоритет
- `status: String` - Статус заявки
- `price: Double` - Цена
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### Route (Маршрут)
- `id: UUID` - Идентификатор маршрута
- `routeNumber: String` - Номер маршрута
- `startLocation: String` - Начальная точка
- `endLocation: String` - Конечная точка
- `waypoints: List<RouteWaypoint>` - Точки маршрута
- `totalDistance: Double` - Общая длина
- `estimatedTime: Double` - Пред估计 время в пути
- `optimizationStatus: String` - Статус оптимизации
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### RouteWaypoint (Точка маршрута)
- `id: UUID` - Идентификатор точки
- `routeId: UUID` - Идентификатор маршрута
- `sequenceNumber: Int` - Порядковый номер
- `location: String` - Место
- `latitude: Double` - Широта
- `longitude: Double` - Долгота
- `arrivalTime: LocalDateTime` - Время прибытия
- `departureTime: LocalDateTime` - Время отъезда
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### LocationUpdate (Обновление местоположения)
- `id: UUID` - Идентификатор обновления
- `vehicleId: UUID` - Идентификатор транспортного средства
- `driverId: UUID` - Идентификатор водителя
- `latitude: Double` - Широта
- `longitude: Double` - Долгота
- `speed: Double` - Скорость
- `timestamp: LocalDateTime` - Время обновления
- `accuracy: Double` - Точность
- `createdAt: LocalDateTime` - Время создания

### RouteDeviation (Отклонение от маршрута)
- `id: UUID` - Идентификатор отклонения
- `vehicleId: UUID` - Идентификатор транспортного средства
- `driverId: UUID` - Идентификатор водителя
- `routeId: UUID` - Идентификатор маршрута
- `deviationDistance: Double` - Расстояние отклонения
- `deviationTime: Int` - Время отклонения
- `currentLatitude: Double` - Текущая широта
- `currentLongitude: Double` - Текущая долгота
- `expectedLatitude: Double` - Ожидаемая широта
- `expectedLongitude: Double` - Ожидаемая долгота
- `status: String` - Статус отклонения
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### FuelRefueling (Заправка топливом)
- `id: UUID` - Идентификатор заправки
- `vehicleId: UUID` - Идентификатор транспортного средства
- `driverId: UUID` - Идентификатор водителя
- `fuelType: String` - Тип топлива
- `volume: Double` - Объем
- `cost: Double` - Стоимость
- `location: String` - Место заправки
- `timestamp: LocalDateTime` - Время заправки
- `createdAt: LocalDateTime` - Время создания

### FuelConsumption (Расход топлива)
- `id: UUID` - Идентификатор записи
- `vehicleId: UUID` - Идентификатор транспортного средства
- `driverId: UUID` - Идентификатор водителя
- `fuelType: String` - Тип топлива
- `consumptionRate: Double` - Норма расхода
- `actualConsumption: Double` - Фактический расход
- `distance: Double` - Пробег
- `tripId: UUID` - Идентификатор рейса
- `timestamp: LocalDateTime` - Время записи
- `createdAt: LocalDateTime` - Время создания

### MaintenanceRecord (Запись о обслуживании)
- `id: UUID` - Идентификатор записи
- `vehicleId: UUID` - Идентификатор транспортного средства
- `maintenanceType: String` - Тип обслуживания
- `description: String` - Описание работ
- `cost: Double` - Стоимость
- `partsUsed: List<SparePart>` - Использованные запчасти
- `serviceCenter: String` - Сервисный центр
- `technician: String` - ФИО техника
- `date: LocalDateTime` - Дата выполнения
- `createdAt: LocalDateTime` - Время создания

### SparePart (Запчасть)
- `id: UUID` - Идентификатор запчасти
- `partNumber: String` - Номер запчасти
- `name: String` - Название
- `description: String` - Описание
- `quantity: Int` - Количество на складе
- `cost: Double` - Стоимость
- `supplier: String` - Поставщик
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### FinancialTransaction (Финансовая транзакция)
- `id: UUID` - Идентификатор транзакции
- `transactionType: String` - Тип транзакции (доход, расход)
- `amount: Double` - Сумма
- `currency: String` - Валюта
- `description: String` - Описание
- `referenceId: UUID?` - Идентификатор связанной сущности
- `referenceType: String?` - Тип связанной сущности
- `transactionDate: LocalDateTime` - Дата транзакции
- `createdAt: LocalDateTime` - Время создания

### CostAnalysisReport (Отчет по затратам)
- `id: UUID` - Идентификатор отчета
- `period: String` - Период отчета
- `totalCosts: Double` - Общие затраты
- `fuelCosts: Double` - Затраты на топливо
- `maintenanceCosts: Double` - Затраты на обслуживание
- `insuranceCosts: Double` - Затраты на страхование
- `taxCosts: Double` - Налоги
- `otherCosts: Double` - Другие затраты
- `createdAt: LocalDateTime` - Время создания

### RevenueReport (Отчет по доходам)
- `id: UUID` - Идентификатор отчета
- `period: String` - Период отчета
- `totalRevenue: Double` - Общий доход
- `tripRevenue: Double` - Доход от рейсов
- `otherRevenue: Double` - Дополнительный доход
- `createdAt: LocalDateTime` - Время создания

### ProfitabilityAnalysis (Анализ рентабельности)
- `id: UUID` - Идентификатор анализа
- `period: String` - Период анализа
- `totalProfit: Double` - Общая прибыль
- `profitMargin: Double` - Маржа прибыли
- `vehicleProfitability: Map<UUID, Double>` - Рентабельность по ТС
- `driverProfitability: Map<UUID, Double>` - Рентабельность по водителям
- `routeProfitability: Map<UUID, Double>` - Рентабельность по маршрутам
- `createdAt: LocalDateTime` - Время создания