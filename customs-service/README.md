# Таможенный сервис (Customs Service)

## Overview
Сервис для работы с таможенными органами и оформления таможенной документации в логистической платформе Nodeorb. Обеспечивает электронный документооборот и интеграцию с международными таможенными системами.

## Structure

### Core Modules
- `com.customsservice.core` - Основная бизнес-логика
- `com.customsservice.data.entities` - JPA сущности для хранения данных
- `com.customsservice.services` - Сервисный слой для бизнес-операций
- `com.customsservice.controllers` - REST API контроллеры
- `com.customsservice.integration` - Интеграции с внешними системами
- `com.customsservice.config` - Конфигурационные классы

### Key Classes

#### Core Business Logic
- `CustomsDeclarationService.kt` - Оформление таможенных деклараций
- `TariffCalculationService.kt` - Расчет таможенных платежей
- `CargoClassificationService.kt` - Классификация ТН ВЭД
- `DocumentGenerationService.kt` - Генерация PDF документов
- `ComplianceCheckService.kt` - Проверка соответствия требованиям

#### Data Entities
- `CustomsDeclaration.kt` - Таможенная декларация
- `TariffRate.kt` - Таможенная ставка
- `CargoClassification.kt` - Классификация груза по ТН ВЭД
- `CustomsDocument.kt` - Таможенный документ
- `ComplianceCheck.kt` - Результат проверки соответствия

## Functionality

### Customs Declaration
- Создание и редактирование таможенных деклараций
- Электронное подписание и отправка деклараций
- Отслеживание статуса деклараций
- Архивление и хранение деклараций

### Tariff Calculation
- Расчет таможенных платежей
- Применение льгот и скидок
- Расчет НДС и других налогов
- Генерация детального расчета

### Cargo Classification
- Классификация товаров по ТН ВЭД
- Проверка ограничений и запретов
- Расчет таможенных ставок по кодам
- Интеграция с базами данных ТН ВЭД

### Document Management
- Генерация PDF деклараций
- Электронный документооборот
- Подпись документов с помощью ЭЦП
- Архивное хранение документов

### Compliance Checks
- Проверка соответствия таможенным правилам
- Проверка на наличие запрещенных товаров
- Проверка ограничений по странам отправителя/получателя
- Генерация отчетов о соответствии

## Technology Stack
- Spring Boot 3.2.4
- Kotlin 1.9.23
- PostgreSQL 18
- Spring Data JPA
- REST API + EDI для интеграции
- PDF generation (iText)
- Docker для контейнеризации

## Configuration

### Application Properties
```properties
# Server configuration
server.port=8083

# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/customs_db
spring.datasource.username=nodeorb
spring.datasource.password=nodeorb_dev_password

# Customs integration configuration
customs.integration.endpoint=https://customs-api.gov
edi.format=EDIFACT

# Document generation configuration
pdf.generation.template.path=/templates
pdf.generation.font.path=/fonts
```

## Running the Service

### Local Development
```bash
cd c:\Project\Nodeorb
./gradlew :customs-service:compileKotlin
./gradlew :customs-service:test
./gradlew :customs-service:bootRun
```

### Docker Deployment
```bash
cd c:\Project\Nodeorb
docker build -t customs-service:latest -f customs-service/Dockerfile .
docker run -d -p 8083:8083 --network nodeorb-network customs-service:latest
```

## API

### REST Endpoints

#### Customs Declarations
- `GET /api/v1/customs/declarations` - Список деклараций
- `POST /api/v1/customs/declarations` - Создать декларацию
- `GET /api/v1/customs/declarations/{id}` - Получить декларацию
- `PUT /api/v1/customs/declarations/{id}` - Обновить декларацию
- `DELETE /api/v1/customs/declarations/{id}` - Удалить декларацию
- `POST /api/v1/customs/declarations/{id}/submit` - Отправить декларацию
- `GET /api/v1/customs/declarations/{id}/status` - Получить статус

#### Tariff Calculation
- `POST /api/v1/customs/tariff/calculate` - Рассчитать таможенные платежи
- `GET /api/v1/customs/tariff/rates` - Список таможенных ставок
- `GET /api/v1/customs/tariff/{code}` - Получить ставку по коду ТН ВЭД

#### Cargo Classification
- `POST /api/v1/customs/classify` - Классифицировать груз по ТН ВЭД
- `GET /api/v1/customs/classification/{code}` - Получить информацию по коду

#### Document Management
- `GET /api/v1/customs/documents/{id}` - Получить документ
- `POST /api/v1/customs/documents/{id}/sign` - Подписать документ
- `POST /api/v1/customs/documents/{id}/archive` - Архивить документ

#### Compliance Checks
- `POST /api/v1/customs/compliance/check` - Проверить соответствие
- `GET /api/v1/customs/compliance/report` - Генерировать отчет

## Integrations

### External Systems
- **ACE (US Customs)**: Интеграция с американскими таможенными органами
- **ЕАЭС**: Интеграция с таможенными системами Евразийского экономического союза
- **Налоговые службы**: Интеграция с национальными таможенными порталами

### Protocols
- **EDI (EDIFACT)**: Электронный обмен данными
- **REST API**: Внешние API для интеграций
- **SOAP**: Для работы с устаревшими системами

## Security & Compliance

### Security Features
- Поддержка CUI (Controlled Unclassified Information)
- Шифрование FIPS 140-2
- Полный аудит операций
- Роли и права доступа

### Compliance Requirements
- Таможенное законодательство стран
- ФТС/Федеральная таможенная служба
- Цифровые стандарты документооборота

## Contributing
Please follow the project guidelines for contributing. Create a feature branch, make your changes, and submit a pull request.

## License
MIT License - see LICENSE file for details.