# Сущности таможенного сервиса (Customs Service) и их взаимодействие с сервисами Nodeorb

## Входные сущности (Input Entities)

### 1. Таможенные декларации
- **CustomsDeclaration** - Взаимодействие с сервисами:
  - `OMS (Order Management System)` - Получение данных о заказе
  - `TMS (Transportation Management System)` - Проверка маршрута
  - `WMS (Warehouse Management System)` - Проверка наличия товара
- **GoodsClassification** - Взаимодействие с сервисами:
  - `OMS` - Получение данных о товаре
  - `TMS` - Проверка маршрута
  - `SCM (Safety & Compliance Management)` - Проверка соответствия

### 2. Таможенные платежы
- **CustomsPayment** - Взаимодействие с сервисами:
  - `Billing & Pricing` - Расчет стоимости
  - `Financial System` - Оплата
- **DutyCalculation** - Взаимодействие с сервисами:
  - `Billing & Pricing` - Расчет таможенных пошлин
  - `Financial System` - Проверка оплаты

### 3. Документы и архивирование
- **CustomsDocument** - Взаимодействие с сервисами:
  - `TMS` - Проверка маршрута
  - `WMS` - Проверка наличия товара
- **DocumentArchive** - Взаимодействие с сервисами:
  - `Archive System` - Архивирование
  - `Analytics & Reporting` - Сбор статистики

## Выходные сущности (Output Entities)

### 1. Таможенные декларации
- **CustomsDeclarationStatus** - Взаимодействие с сервисами:
  - `OMS` - Обновление статуса заказа
  - `User Portal` - Информация о статусе
- **GoodsClassificationResult** - Взаимодействие с сервисами:
  - `OMS` - Обновление данных о товаре
  - `User Portal` - Информация о классификации

### 2. Таможенные платежы
- **CustomsPaymentStatus** - Взаимодействие с сервисами:
  - `Billing & Pricing` - Обновление статуса оплаты
  - `User Portal` - Информация о статусе
- **DutyCalculationResult** - Взаимодействие с сервисами:
  - `Billing & Pricing` - Обновление данных о пошлине
  - `User Portal` - Информация о расчете

### 3. Документы и архивирование
- **CustomsDocumentStatus** - Взаимодействие с сервисами:
  - `TMS` - Обновление статуса маршрута
  - `User Portal` - Информация о документе
- **DocumentArchiveStatus** - Взаимодействие с сервисами:
  - `Archive System` - Обновление статуса архивирования
  - `Analytics & Reporting` - Сбор статистики

## Внутренние сущности (Internal Processing)

### 1. Таможенные декларации
- **DeclarationProcessor** - Взаимодействие с сервисами:
  - `CustomsDeclaration` - Обработка декларации
  - `Customs System` - Отправка на таможню
- **GoodsClassifier** - Взаимодействие с сервисами:
  - `GoodsClassification` - Классификация товара
  - `TNVED Database` - Проверка классификации

### 2. Таможенные платежы
- **PaymentProcessor** - Взаимодействие с сервисами:
  - `CustomsPayment` - Обработка оплаты
  - `Financial System` - Проверка статуса
- **DutyCalculator** - Взаимодействие с сервисами:
  - `DutyCalculation` - Расчет пошлины
  - `Billing & Pricing` - Обновление данных

### 3. Документы и архивирование
- **DocumentProcessor** - Взаимодействие с сервисами:
  - `CustomsDocument` - Обработка документа
  - `PDF Generation` - Генерация PDF
- **ArchiveProcessor** - Взаимодействие с сервисами:
  - `DocumentArchive` - Архивирование
  - `Archive System` - Сохранение

## Сводная таблица взаимодействий с сервисами Nodeorb

| Сервис Nodeorb                  | Сущности, с которыми взаимодействует |
|---------------------------------|-------------------------------------|
| **OMS (Order Management System)** | CustomsDeclaration, GoodsClassification, CustomsDeclarationStatus, GoodsClassificationResult |
| **TMS (Transportation Management System)** | CustomsDeclaration, GoodsClassification, CustomsDocument, CustomsDeclarationStatus, CustomsDocumentStatus |
| **WMS (Warehouse Management System)** | CustomsDeclaration, CustomsDocument, CustomsDeclarationStatus, CustomsDocumentStatus |
| **SCM (Safety & Compliance Management)** | GoodsClassification, GoodsClassificationResult |
| **Billing & Pricing** | CustomsPayment, DutyCalculation, CustomsPaymentStatus, DutyCalculationResult |
| **Financial System** | CustomsPayment, DutyCalculation, PaymentProcessor |
| **User Portal** | CustomsDeclarationStatus, GoodsClassificationResult, CustomsPaymentStatus, DutyCalculationResult, CustomsDocumentStatus, DocumentArchiveStatus |
| **Archive System** | DocumentArchive, ArchiveProcessor, DocumentArchiveStatus |
| **Analytics & Reporting** | DocumentArchive, DocumentArchiveStatus |
| **Customs System** | DeclarationProcessor |
| **TNVED Database** | GoodsClassifier |
| **PDF Generation** | DocumentProcessor |

## Сущности и их свойства

### CustomsDeclaration (Таможенная декларация)
- `id: UUID` - Идентификатор декларации
- `declarationNumber: String` - Номер декларации
- `shipperId: UUID` - Идентификатор отправителя
- `consigneeId: UUID` - Идентификатор получателя
- `orderId: UUID` - Идентификатор заказа
- `goods: List<CustomsGoods>` - Список товаров
- `totalValue: BigDecimal` - Общая стоимость
- `customsProcedure: String` - Таможенная процедура
- `status: String` - Статус декларации
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### CustomsGoods (Товар в декларации)
- `id: UUID` - Идентификатор товара
- `declarationId: UUID` - Идентификатор декларации
- `productCode: String` - Код товара
- `productName: String` - Название товара
- `quantity: Int` - Количество
- `unit: String` - Единица измерения
- `value: BigDecimal` - Стоимость
- `tnvedCode: String` - Код ТН ВЭД
- `countryOfOrigin: String` - Страна происхождения
- `countryOfDestination: String` - Страна назначения
- `customsTariff: BigDecimal` - Таможенная пошлина
- `vatRate: BigDecimal` - Ставка НДС

### CustomsPayment (Таможенный платеж)
- `id: UUID` - Идентификатор платежа
- `declarationId: UUID` - Идентификатор декларации
- `paymentNumber: String` - Номер платежа
- `amount: BigDecimal` - Сумма платежа
- `paymentDate: LocalDateTime` - Дата платежа
- `paymentMethod: String` - Метод оплаты
- `status: String` - Статус платежа
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### CustomsDocument (Таможенный документ)
- `id: UUID` - Идентификатор документа
- `declarationId: UUID` - Идентификатор декларации
- `documentType: String` - Тип документа
- `documentNumber: String` - Номер документа
- `documentDate: LocalDateTime` - Дата документа
- `fileUrl: String` - URL на файл
- `status: String` - Статус документа
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### DocumentArchive (Архивирование документов)
- `id: UUID` - Идентификатор архива
- `documentId: UUID` - Идентификатор документа
- `archivePath: String` - Путь в архиве
- `archiveDate: LocalDateTime` - Дата архивирования
- `status: String` - Статус архивирования
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления