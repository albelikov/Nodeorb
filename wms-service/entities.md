# Сущности складского сервиса (Warehouse Management System - WMS) и их взаимодействие с сервисами Nodeorb

## Входные сущности (Input Entities)

### 1. Управление запасами
- **InventoryItem** - Взаимодействие с сервисами:
  - `OMS (Order Management System)` - Проверка доступности товаров для заказа
  - `TMS (Transportation Management System)` - Проверка наличия товаров для отгрузки
- **InventoryUpdate** - Взаимодействие с сервисами:
  - `RFID Systems` - Обновление данных о запасах
  - `IoT Sensors` - Данные о состоянии товаров
- **InventoryRequest** - Взаимодействие с сервисами:
  - `Procurement System` - Запрос на пополнение запасов
  - `Analytics & Reporting` - Аналитика потребления

### 2. Заказы и поставки
- **Order** - Взаимодействие с сервисами:
  - `OMS` - Получение данных о заказе
  - `TMS` - Проверка готовности к отгрузке
- **OrderItem** - Взаимодействие с сервисами:
  - `OMS` - Детализация позиций заказа
  - `WMS` - Проверка доступности товаров

### 3. Складские операции
- **WarehouseOperation** - Взаимодействие с сервисами:
  - `WMS` - Отслеживание состояния операции
  - `Analytics & Reporting` - Аналитика производительности
- **StorageLocation** - Взаимодействие с сервисами:
  - `WMS` - Управление ячейками хранения
  - `TMS` - Планирование маршрута для отгрузки

### 4. Обратная логистика
- **ReturnRequest** - Взаимодействие с сервисами:
  - `OMS` - Получение данных о возврате
  - `Customer Portal` - Информация о статусе возврата
- **WarrantyCase** - Взаимодействие с сервисами:
  - `Customer Portal` - Управление гарантийными случаями
  - `Analytics & Reporting` - Аналитика гарантийных случаев

## Выходные сущности (Output Entities)

### 1. Управление запасами
- **InventoryReport** - Взаимодействие с сервисами:
  - `OMS` - Обновление статуса запасов для заказа
  - `Analytics & Reporting` - Сбор данных о запасах
  - `Customer Portal` - Информация о наличии товаров
- **InventoryAlert** - Взаимодействие с сервисами:
  - `Procurement System` - Уведомление о недостатке запасов
  - `WMS` - Обработка алерта

### 2. Заказы и поставки
- **OrderFulfillment** - Взаимодействие с сервисами:
  - `OMS` - Обновление статуса заказа
  - `TMS` - Проверка готовности к отгрузке
- **PickList** - Взаимодействие с сервисами:
  - `WMS` - Инструкции для сборки заказа
  - `Analytics & Reporting` - Аналитика сборки

### 3. Складские операции
- **WarehouseOperationStatus** - Взаимодействие с сервисами:
  - `OMS` - Обновление статуса заказа
  - `TMS` - Проверка готовности к отгрузке
- **StorageLocationStatus** - Взаимодействие с сервисами:
  - `WMS` - Обновление статуса ячейки хранения
  - `Analytics & Reporting` - Аналитика использования хранилища

### 4. Обратная логистика
- **ReturnStatus** - Взаимодействие с сервисами:
  - `OMS` - Обновление статуса возврата
  - `Customer Portal` - Информация о статусе возврата
- **ReturnAnalytics** - Взаимодействие с сервисами:
  - `Analytics & Reporting` - Аналитика возвратов
  - `Procurement System` - Аналитика поставок

## Внутренние сущности (Internal Processing)

### 1. Управление запасами
- **InventoryUpdateProcessor** - Взаимодействие с сервисами:
  - `InventoryUpdate` - Обработка обновлений запасов
  - `WMS` - Обновление данных о запасах
- **InventoryOptimization** - Взаимодействие с сервисами:
  - `Analytics & Reporting` - Анализ потребления
  - `WMS` - Оптимизация запасов

### 2. Заказы и поставки
- **OrderProcessing** - Взаимодействие с сервисами:
  - `Order` - Обработка заказа
  - `WMS` - Обновление статуса заказа
- **PickListGenerator** - Взаимодействие с сервисами:
  - `Order` - Генерация списка сборки
  - `WMS` - Оптимизация маршрута для сборки

### 3. Складские операции
- **WarehouseOperationTracker** - Взаимодействие с сервисами:
  - `WarehouseOperation` - Отслеживание состояния операции
  - `WMS` - Обновление статуса операции
- **StorageLocationOptimizer** - Взаимодействие с сервисами:
  - `StorageLocation` - Оптимизация ячеек хранения
  - `WMS` - Обновление данных о ячейках

### 4. Обратная логистика
- **ReturnProcessor** - Взаимодействие с сервисами:
  - `ReturnRequest` - Обработка возврата
  - `WMS` - Обновление статуса возврата
- **WarrantyCaseProcessor** - Взаимодействие с сервисами:
  - `WarrantyCase` - Обработка гарантийного случая
  - `WMS` - Обновление статуса гарантийного случая

## Сводная таблица взаимодействий с сервисами Nodeorb

| Сервис Nodeorb                  | Сущности, с которыми взаимодействует |
|---------------------------------|-------------------------------------|
| **OMS (Order Management System)** | Order, OrderItem, OrderFulfillment, ReturnStatus |
| **TMS (Transportation Management System)** | InventoryItem, Order, StorageLocation, OrderFulfillment, StorageLocationStatus |
| **Procurement System** | InventoryRequest, InventoryAlert, ReturnAnalytics |
| **Customer Portal** | ReturnRequest, WarrantyCase, InventoryReport, ReturnStatus |
| **Analytics & Reporting** | InventoryRequest, InventoryReport, OrderFulfillment, PickList, WarehouseOperation, StorageLocationStatus, ReturnAnalytics |
| **RFID Systems** | InventoryUpdate |
| **IoT Sensors** | InventoryUpdate |

## Сущности и их свойства

### InventoryItem (Товар на складе)
- `id: Long?` - Идентификатор товара
- `sku: String` - Артикул товара
- `name: String` - Название товара
- `description: String?` - Описание товара
- `quantity: Int` - Количество на складе
- `unit: String` - Единица измерения
- `locationId: Long` - Идентификатор ячейки хранения
- `category: String` - Категория товара
- `supplierId: Long` - Идентификатор поставщика
- `costPrice: BigDecimal` - Себестоимость
- `sellingPrice: BigDecimal` - Розничная цена
- `expiryDate: Instant?` - Срок годности
- `status: String` - Статус товара (в наличии, на заказ, недоступен)
- `createdAt: Instant` - Время создания
- `updatedAt: Instant` - Время последнего обновления

### InventoryUpdate (Обновление запасов)
- `id: Long?` - Идентификатор обновления
- `itemId: Long` - Идентификатор товара
- `quantityChange: Int` - Изменение количества
- `updateType: String` - Тип обновления (приход, расход)
- `reason: String?` - Причина обновления
- `locationId: Long?` - Идентификатор ячейки хранения
- `updatedAt: Instant` - Время обновления
- `processedAt: Instant?` - Время обработки

### InventoryReport (Отчет о запасах)
- `id: Long?` - Идентификатор отчета
- `generatedAt: Instant` - Время генерации
- `totalItems: Int` - Общее количество товаров
- `lowStockItems: Int` - Количество товаров с низким запасом
- `outOfStockItems: Int` - Количество товаров с нулевым запасом
- `totalValue: BigDecimal` - Общая стоимость запасов
- `categoryBreakdown: Map<String, Int>` - Разблок по категориям
- `supplierBreakdown: Map<Long, Int>` - Разблок по поставщикам

### OrderFulfillment (Выполнение заказа)
- `id: Long?` - Идентификатор выполнения
- `orderId: Long` - Идентификатор заказа
- `status: String` - Статус выполнения (в процессе, готово, отклонено)
- `pickedQuantity: Int` - Количество собранных товаров
- `shippedQuantity: Int` - Количество отгруженных товаров
- `returnedQuantity: Int` - Количество возвращенных товаров
- `pickedAt: Instant?` - Время сборки
- `shippedAt: Instant?` - Время отгрузки
- `deliveredAt: Instant?` - Время доставки
- `returnedAt: Instant?` - Время возврата

### PickList (Список сборки)
- `id: Long?` - Идентификатор списка
- `orderId: Long` - Идентификатор заказа
- `items: List<PickListItem>` - Позиции для сборки
- `createdAt: Instant` - Время создания
- `updatedAt: Instant` - Время последнего обновления

### PickListItem (Позиция в списке сборки)
- `id: Long?` - Идентификатор позиции
- `sku: String` - Артикул товара
- `name: String` - Название товара
- `quantity: Int` - Количество для сборки
- `location: String` - Место хранения
- `pickedQuantity: Int` - Количество собранного товара
- `status: String` - Статус позиции (в процессе, собран, отклонен)

### StorageLocation (Ячейка хранения)
- `id: Long?` - Идентификатор ячейки
- `locationCode: String` - Код ячейки
- `type: String` - Тип ячейки (паллет, коробка, полка)
- `capacity: Int` - Вместимость
- `occupiedQuantity: Int` - Количество занятых мест
- `status: String` - Статус ячейки (доступна, занята, не доступна)
- `createdAt: Instant` - Время создания
- `updatedAt: Instant` - Время последнего обновления

### ReturnStatus (Статус возврата)
- `id: Long?` - Идентификатор статуса
- `returnId: String` - Идентификатор возврата
- `status: ReturnStatus` - Статус возврата
- `updatedAt: Instant` - Время последнего обновления

### ReturnAnalytics (Аналитика возвратов)
- `returnRate: Double` - Процент возвратов
- `topReturnReasons: Map<String, Int>` - Топ причин возвратов
- `avgProcessingTime: Double` - Среднее время обработки

### WarrantyStatus (Статус гарантии)
- `id: Long?` - Идентификатор статуса
- `warrantyCaseId: String` - Идентификатор гарантийного случая
- `status: WarrantyStatus` - Статус гарантии
- `updatedAt: Instant` - Время последнего обновления