# Сущности сервиса управления заказами (OMS - Order Management System) и их взаимодействие с сервисами Nodeorb

## Входные сущности (Input Entities)

### 1. Заказы и клиенты
- **Order** - Взаимодействие с сервисами:
  - `User Portal` - Создание заказа
  - `WMS (Warehouse Management System)` - Проверка наличия товара
  - `TMS (Transportation Management System)` - Проверка маршрута
- **Customer** - Взаимодействие с сервисами:
  - `Keycloak` - Аутентификация
  - `User Portal` - Информация о клиенте

### 2. Позиции заказа
- **OrderItem** - Взаимодействие с сервисами:
  - `WMS` - Проверка наличия товара
  - `Billing & Pricing` - Расчет стоимости
- **Product** - Взаимодействие с сервисами:
  - `WMS` - Проверка наличия товара
  - `Billing & Pricing` - Расчет стоимости

### 3. Платежи и возвраты
- **Payment** - Взаимодействие с сервисами:
  - `Financial System` - Оплата
  - `User Portal` - Информация о оплате
- **Return** - Взаимодействие с сервисами:
  - `WMS` - Проверка наличия товара
  - `User Portal` - Информация о возврате

## Выходные сущности (Output Entities)

### 1. Заказы и клиенты
- **OrderStatus** - Взаимодействие с сервисами:
  - `User Portal` - Информация о статусе
  - `TMS` - Проверка маршрута
- **CustomerProfile** - Взаимодействие с сервисами:
  - `Keycloak` - Аутентификация
  - `User Portal` - Информация о клиенте

### 2. Позиции заказа
- **OrderItemStatus** - Взаимодействие с сервисами:
  - `WMS` - Обновление статуса
  - `User Portal` - Информация о статусе
- **ProductAvailability** - Взаимодействие с сервисами:
  - `WMS` - Проверка наличия товара
  - `User Portal` - Информация о наличии

### 3. Платежи и возвраты
- **PaymentStatus** - Взаимодействие с сервисами:
  - `Financial System` - Обновление статуса
  - `User Portal` - Информация о статусе
- **ReturnStatus** - Взаимодействие с сервисами:
  - `WMS` - Обновление статуса
  - `User Portal` - Информация о статусе

## Внутренние сущности (Internal Processing)

### 1. Заказы и клиенты
- **OrderProcessor** - Взаимодействие с сервисами:
  - `Order` - Обработка заказа
  - `WMS` - Проверка наличия товара
- **CustomerManager** - Взаимодействие с сервисами:
  - `Customer` - Управление клиентом
  - `Keycloak` - Аутентификация

### 2. Позиции заказа
- **OrderItemProcessor** - Взаимодействие с сервисами:
  - `OrderItem` - Обработка позиции
  - `WMS` - Проверка наличия товара
- **ProductManager** - Взаимодействие с сервисами:
  - `Product` - Управление товаром
  - `WMS` - Проверка наличия товара

### 3. Платежи и возвраты
- **PaymentProcessor** - Взаимодействие с сервисами:
  - `Payment` - Обработка платежа
  - `Financial System` - Проверка статуса
- **ReturnProcessor** - Взаимодействие с сервисами:
  - `Return` - Обработка возврата
  - `WMS` - Проверка наличия товара

## Сводная таблица взаимодействий с сервисами Nodeorb

| Сервис Nodeorb                  | Сущности, с которыми взаимодействует |
|---------------------------------|-------------------------------------|
| **User Portal** | Order, Customer, Payment, Return, OrderStatus, CustomerProfile, OrderItemStatus, ProductAvailability, PaymentStatus, ReturnStatus |
| **WMS (Warehouse Management System)** | Order, OrderItem, Product, Return, OrderProcessor, OrderItemProcessor, ProductManager, ReturnProcessor |
| **TMS (Transportation Management System)** | Order, OrderStatus |
| **Keycloak** | Customer, CustomerManager |
| **Financial System** | Payment, PaymentProcessor, PaymentStatus |
| **Billing & Pricing** | OrderItem, Product |

## Сущности и их свойства

### Order (Заказ)
- `id: UUID` - Идентификатор заказа
- `customerId: UUID` - Идентификатор клиента
- `orderNumber: String` - Номер заказа
- `orderDate: LocalDateTime` - Дата заказа
- `status: String` - Статус заказа
- `totalAmount: BigDecimal` - Общая сумма
- `paymentMethod: String` - Метод оплаты
- `shippingAddress: String` - Адрес доставки
- `billingAddress: String` - Адрес инвойса
- `notes: String?` - Примечания
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### OrderItem (Позиция заказа)
- `id: UUID` - Идентификатор позиции
- `orderId: UUID` - Идентификатор заказа
- `productId: UUID` - Идентификатор товара
- `quantity: Int` - Количество
- `unitPrice: BigDecimal` - Цена за единицу
- `totalPrice: BigDecimal` - Общая цена
- `status: String` - Статус позиции
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### Product (Товар)
- `id: UUID` - Идентификатор товара
- `productCode: String` - Код товара
- `productName: String` - Название товара
- `description: String?` - Описание товара
- `unitPrice: BigDecimal` - Цена за единицу
- `category: String` - Категория
- `brand: String?` - Бренд
- `weight: Double?` - Вес
- `dimensions: String?` - Габариты
- `status: String` - Статус товара
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### Customer (Клиент)
- `id: UUID` - Идентификатор клиента
- `firstName: String` - Имя
- `lastName: String` - Фамилия
- `email: String` - Email
- `phoneNumber: String` - Телефон
- `address: String` - Адрес
- `city: String` - Город
- `country: String` - Страна
- `postalCode: String` - Почтовый индекс
- `status: String` - Статус клиента
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### Payment (Платеж)
- `id: UUID` - Идентификатор платежа
- `orderId: UUID` - Идентификатор заказа
- `paymentNumber: String` - Номер платежа
- `amount: BigDecimal` - Сумма платежа
- `paymentDate: LocalDateTime` - Дата платежа
- `paymentMethod: String` - Метод оплаты
- `status: String` - Статус платежа
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### Return (Возврат)
- `id: UUID` - Идентификатор возврата
- `orderId: UUID` - Идентификатор заказа
- `returnNumber: String` - Номер возврата
- `returnDate: LocalDateTime` - Дата возврата
- `reason: String` - Причина возврата
- `status: String` - Статус возврата
- `refundAmount: BigDecimal` - Сумма возврата
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления