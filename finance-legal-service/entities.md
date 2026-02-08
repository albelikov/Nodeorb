# Сущности финансово-правового модуля (Finance-Legal Service) и их взаимодействие с сервисами Nodeorb

## Входные сущности (Input Entities)

### 1. Бухгалтерский учет
- **AccountingEntry** - Взаимодействие с сервисами:
  - `TMS (Transportation Management System)` - Получение данных о перевозках
  - `WMS (Warehouse Management System)` - Проверка наличия товара
  - `Billing & Pricing` - Расчет стоимости услуг
- **ChartOfAccounts** - Взаимодействие с сервисами:
  - `Financial System` - Планирование счетов
  - `Analytics & Reporting` - Генерация отчетности

### 2. Финансовая отчетность
- **FinancialReport** - Взаимодействие с сервисами:
  - `Analytics & Reporting` - Генерация отчетов
  - `User Portal` - Доступ к отчетам
- **TaxDeclaration** - Взаимодействие с сервисами:
  - `Financial System` - Подготовка деклараций
  - `Tax Service` - Отправка на проверку

### 3. Юридический модуль
- **Contract** - Взаимодействие с сервисами:
  - `OMS (Order Management System)` - Получение данных о заказе
  - `User Portal` - Доступ к договорам
- **LegalDocument** - Взаимодействие с сервисами:
  - `Archive System` - Архивирование
  - `Analytics & Reporting` - Сбор статистики

### 4. Страхование
- **InsurancePolicy** - Взаимодействие с сервисами:
  - `TMS` - Получение данных о маршруте
  - `Billing & Pricing` - Расчет стоимости полиса
- **InsuranceClaim** - Взаимодействие с сервисами:
  - `WMS` - Проверка наличия товара
  - `Financial System` - Выплата страховки


>>>>>>> 5. Таможенные службы (переведено в customs-service)
>>>>>>> - **CustomsDocument** - Взаимодействие с сервисами:
>>>>>>>   - `TMS` - Проверка маршрута
>>>>>>>   - `WMS` - Проверка наличия товара
>>>>>>> - **CustomsDeclaration** - Взаимодействие с сервисами:
>>>>>>>   - `OMS` - Получение данных о заказе
>>>>>>>   - `Customs Service` - Отправка на таможню

## Выходные сущности (Output Entities)

### 1. Бухгалтерский учет
- **AccountingEntryStatus** - Взаимодействие с сервисами:
  - `Financial System` - Обновление статуса записи
  - `User Portal` - Информация о статусе
- **ChartOfAccountsStatus** - Взаимодействие с сервисами:
  - `Financial System` - Обновление статуса счета
  - `Analytics & Reporting` - Сбор статистики

### 2. Финансовая отчетность
- **FinancialReportStatus** - Взаимодействие с сервисами:
  - `Analytics & Reporting` - Обновление статуса отчета
  - `User Portal` - Информация о статусе
- **TaxDeclarationStatus** - Взаимодействие с сервисами:
  - `Financial System` - Обновление статуса декларации
  - `User Portal` - Информация о статусе

### 3. Юридический модуль
- **ContractStatus** - Взаимодействие с сервисами:
  - `OMS` - Обновление статуса заказа
  - `User Portal` - Информация о статусе
- **LegalDocumentStatus** - Взаимодействие с сервисами:
  - `Archive System` - Обновление статуса архивирования
  - `Analytics & Reporting` - Сбор статистики

### 4. Страхование
- **InsurancePolicyStatus** - Взаимодействие с сервисами:
  - `Billing & Pricing` - Обновление статуса полиса
  - `User Portal` - Информация о статусе
- **InsuranceClaimStatus** - Взаимодействие с сервисами:
  - `Financial System` - Обновление статуса заявки
  - `User Portal` - Информация о статусе

### 5. Таможенные службы
- **CustomsDocumentStatus** - Взаимодействие с сервисами:
  - `TMS` - Обновление статуса маршрута
  - `User Portal` - Информация о документе
- **CustomsDeclarationStatus** - Взаимодействие с сервисами:
  - `OMS` - Обновление статуса заказа
  - `User Portal` - Информация о статусе

## Внутренние сущности (Internal Processing)

### 1. Бухгалтерский учет
- **AccountingEntryProcessor** - Взаимодействие с сервисами:
  - `AccountingEntry` - Обработка бухгалтерской записи
  - `Financial System` - Проверка баланса
- **ChartOfAccountsProcessor** - Взаимодействие с сервисами:
  - `ChartOfAccounts` - Обработка плана счетов
  - `Financial System` - Проверка корректности

### 2. Финансовая отчетность
- **FinancialReportGenerator** - Взаимодействие с сервисами:
  - `FinancialReport` - Генерация финансовых отчетов
  - `Analytics & Reporting` - Сбор данных
- **TaxDeclarationProcessor** - Взаимодействие с сервисами:
  - `TaxDeclaration` - Обработка налоговых деклараций
  - `Tax Service` - Проверка декларации

### 3. Юридический модуль
- **ContractProcessor** - Взаимодействие с сервисами:
  - `Contract` - Обработка договора
  - `Legal System` - Проверка юридической силы
- **LegalDocumentProcessor** - Взаимодействие с сервисами:
  - `LegalDocument` - Обработка юридической документации
  - `Archive System` - Архивирование

### 4. Страхование
- **InsurancePolicyProcessor** - Взаимодействие с сервисами:
  - `InsurancePolicy` - Обработка страхового полиса
  - `Insurance System` - Проверка условий
- **InsuranceClaimProcessor** - Взаимодействие с сервисами:
  - `InsuranceClaim` - Обработка страховой заявки
  - `Insurance System` - Проверка заявки

### 5. Таможенные службы
- **CustomsDocumentProcessor** - Взаимодействие с сервисами:
  - `CustomsDocument` - Обработка таможенного документа
  - `Customs Service` - Проверка документа
- **CustomsDeclarationProcessor** - Взаимодействие с сервисами:
  - `CustomsDeclaration` - Обработка таможенной декларации
  - `Customs Service` - Отправка на таможню

## Сводная таблица взаимодействий с сервисами Nodeorb

| Сервис Nodeorb                  | Сущности, с которыми взаимодействует |
|---------------------------------|-------------------------------------|
| **OMS (Order Management System)** | Contract, CustomsDeclaration, ContractStatus, CustomsDeclarationStatus |
| **TMS (Transportation Management System)** | AccountingEntry, InsurancePolicy, CustomsDocument, CustomsDocumentStatus |
| **WMS (Warehouse Management System)** | AccountingEntry, InsuranceClaim, CustomsDocument, CustomsDocumentStatus |
| **Billing & Pricing** | AccountingEntry, InsurancePolicy, InsurancePolicyStatus |
| **Financial System** | AccountingEntry, ChartOfAccounts, FinancialReport, TaxDeclaration, AccountingEntryStatus, ChartOfAccountsStatus, FinancialReportStatus, TaxDeclarationStatus, InsuranceClaimStatus |
| **Legal System** | Contract, LegalDocument, ContractStatus, LegalDocumentStatus |
| **Insurance System** | InsurancePolicy, InsuranceClaim, InsurancePolicyStatus, InsuranceClaimStatus |
| **Customs Service** | CustomsDocument, CustomsDeclaration, CustomsDocumentProcessor, CustomsDeclarationProcessor |
| **Tax Service** | TaxDeclaration, TaxDeclarationProcessor, TaxDeclarationStatus |
| **User Portal** | FinancialReport, TaxDeclaration, Contract, LegalDocument, InsurancePolicy, InsuranceClaim, CustomsDocument, CustomsDeclaration, FinancialReportStatus, TaxDeclarationStatus, ContractStatus, LegalDocumentStatus, InsurancePolicyStatus, InsuranceClaimStatus, CustomsDocumentStatus, CustomsDeclarationStatus |
| **Analytics & Reporting** | ChartOfAccounts, FinancialReport, LegalDocument, ChartOfAccountsStatus, FinancialReportStatus, LegalDocumentStatus |
| **Archive System** | LegalDocument, LegalDocumentStatus, LegalDocumentProcessor |

## Сущности и их свойства

### AccountingEntry (Бухгалтерская запись)
- `id: Long` - Идентификатор записи
- `entryDate: LocalDateTime` - Дата записи
- `description: String` - Описание
- `debitAccount: String` - Дебетовый счет
- `creditAccount: String` - Кредитный счет
- `amount: BigDecimal` - Сумма
- `currency: String` - Валюта
- `exchangeRate: BigDecimal?` - Курс валюты
- `referenceNumber: String?` - Номер справки
- `transactionType: String` - Тип транзакции
- `createdBy: String` - Создал
- `createdAt: LocalDateTime` - Время создания

### ChartOfAccounts (План счетов)
- `id: Long` - Идентификатор счета
- `accountNumber: String` - Номер счета
- `accountName: String` - Название счета
- `accountType: String` - Тип счета
- `description: String?` - Описание
- `parentAccount: String?` - Родительский счет
- `currency: String` - Валюта
- `isActive: Boolean` - Активный
- `createdBy: String` - Создал
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### Contract (Договор)
- `id: Long` - Идентификатор договора
- `contractNumber: String` - Номер договора
- `contractName: String` - Название договора
- `counterparty: String` - Контрагент
- `counterpartyId: Long?` - Идентификатор контрагента
- `startDate: LocalDateTime` - Дата начала
- `endDate: LocalDateTime?` - Дата окончания
- `contractType: String` - Тип договора
- `status: String` - Статус
- `totalAmount: BigDecimal?` - Общая сумма
- `currency: String` - Валюта
- `terms: String?` - Условия
- `signedBy: String?` - Подписал
- `documentUrl: String?` - URL документа
- `createdBy: String` - Создал
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### InsurancePolicy (Страховой полис)
- `id: Long` - Идентификатор полиса
- `policyNumber: String` - Номер полиса
- `policyType: String` - Тип полиса
- `insurer: String` - Страховая компания
- `insured: String` - Страхователь
- `startDate: LocalDateTime` - Дата начала
- `endDate: LocalDateTime` - Дата окончания
- `coverageAmount: BigDecimal` - Сумма страховки
- `premiumAmount: BigDecimal` - Страховая премия
- `currency: String` - Валюта
- `coverageDetails: String?` - Детали страховки
- `status: String` - Статус
- `documentUrl: String?` - URL документа
- `createdBy: String` - Создал
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### TaxDeclaration (Налоговая декларация)
- `id: Long` - Идентификатор декларации
- `declarationNumber: String` - Номер декларации
- `taxType: String` - Тип налога
- `taxPeriod: String` - Налоговый период
- `year: Int` - Год
- `period: String` - Период
- `totalAmount: BigDecimal` - Общая сумма
- `currency: String` - Валюта
- `status: String` - Статус
- `filingDate: LocalDateTime?` - Дата подачи
- `approvedDate: LocalDateTime?` - Дата утверждения
- `documentUrl: String?` - URL документа
- `createdBy: String` - Создал
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления

### CustomsDocument (Таможенный документ)
- `id: Long` - Идентификатор документа
- `documentNumber: String` - Номер документа
- `documentType: String` - Тип документа
- `customsCode: String?` - Таможенный код
- `goodsDescription: String?` - Описание товара
- `value: BigDecimal?` - Стоимость
- `currency: String` - Валюта
- `customsDuty: BigDecimal?` - Таможенная пошлина
- `vatAmount: BigDecimal?` - НДС
- `totalTaxes: BigDecimal?` - Общая сумма налогов
- `status: String` - Статус
- `issueDate: LocalDateTime?` - Дата выдачи
- `expiryDate: LocalDateTime?` - Дата истечения
- `documentUrl: String?` - URL документа
- `createdBy: String` - Создал
- `createdAt: LocalDateTime` - Время создания
- `updatedAt: LocalDateTime` - Время последнего обновления