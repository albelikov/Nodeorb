 ==== admin-backend ==== 
# Серверная часть административной панели (Admin Backend)
## Overview
## Structure
### Core Modules
### Key Classes
#### Core Business Logic
#### Data Entities
## Functionality
### User Management
### System Monitoring
### Analytics & Reporting
### System Configuration
## Technology Stack
## Configuration
### Application Properties
# Server configuration
# Database configuration
# Security configuration
# Logging configuration
## Running the Service
### Local Development
### Docker Deployment
## API
### REST Endpoints
#### User Management
#### System Monitoring
#### Analytics
#### System Configuration
## Contributing
## License
 
 ==== admin-frontend ==== 
# Клиентская часть административной панели (Admin Frontend)
## Overview
## Structure
### Core Modules
### Key Components
#### Common Logic
#### Web Version (React)
## Functionality
### Dashboard & Monitoring
### User Management
### Analytics & Reporting
### System Configuration
## Technology Stack
## Configuration
### Web Version
## Running the Service
### Local Development
#### Web Version (React)
#### Desktop Version
#### Build for All Platforms
### Docker Deployment
## API Integration
### REST API
### WebSocket
## Contributing
## License
 
 ==== autonomous-ops ==== 
# Autonomous Ops Service
## Overview
## Structure
### Core Modules
### Key Classes
#### Core Business Logic
#### Data Entities
#### Services
## Functionality
### Mission Management
### Autonomous Execution
### Decision Making
## Technology Stack
## Configuration
### Application Properties
# Database configuration
# gRPC configuration
# Kafka configuration
# Logging configuration
## Running the Service
### Local Development
### Docker Deployment
## API
### gRPC Services
### REST Endpoints
## Contributing
## License
 
 ==== customs-service ==== 
# Таможенный сервис (Customs Service)
## Overview
## Structure
### Core Modules
### Key Classes
#### Core Business Logic
#### Data Entities
## Functionality
### Customs Declaration
### Tariff Calculation
### Cargo Classification
### Document Management
### Compliance Checks
## Technology Stack
## Configuration
### Application Properties
# Server configuration
# Database configuration
# Customs integration configuration
# Document generation configuration
## Running the Service
### Local Development
### Docker Deployment
## API
### REST Endpoints
#### Customs Declarations
#### Tariff Calculation
#### Cargo Classification
#### Document Management
#### Compliance Checks
## Integrations
### External Systems
### Protocols
## Security & Compliance
### Security Features
### Compliance Requirements
## Contributing
## License
 
 ==== fms-service ==== 
# Сервис управления флотом (Fleet Management Service - FMS)
## Overview
## Structure
### Core Modules
### Key Classes
#### Core Business Logic
#### Data Entities
## Functionality
### Vehicle Management
### Driver Management
### Fuel Monitoring
### Maintenance Management
### Real-Time Tracking
## Technology Stack
## Configuration
### Application Properties
# Server configuration
# Database configuration
# GIS integration configuration
# MQTT configuration
# Tracking configuration
## Running the Service
### Local Development
### Docker Deployment
## API
### REST Endpoints
#### Vehicle Management
#### Driver Management
#### Fuel Monitoring
#### Maintenance
#### Tracking
## Integrations
### External Systems
### Protocols
## Security & Compliance
### Security Features
### Compliance Requirements
## Contributing
## License
 
 ==== freight-marketplace ==== 
# Freight Marketplace Service
## Архитектура
### Основные компоненты
### Технологический стек
## Функциональность
### Основные возможности
### Бизнес``` логика
## API Endpoints
### Заказы
### Ставки
## Конфигурация
### Настройки приложения
### База данных
### Kafka топики
## Развертывание
### Локальная разработка
# Запуск с Docker Compose
# Запуск приложения
### Контейнеризация
# Сборка образа
# Запуск контейнера
### Kubernetes
# Применение конфигурации
## Тестирование
### Unit тесты
### Интеграционные тесты
## Безопасность
### Аутентификация
### Соответствие требованиям
## Мониторинг
### Health checks
### Логирование
## Интеграции
### Внутренние сервисы
### Внешние системы
## Производительность
### Ожидаемые нагрузки
### Балансировка нагрузки
## Поддержка
### Документация
### Контакты
## Лицензия
 
 ==== oms-service ==== 
# Сервис управления заказами (Order Management Service - OMS)
## Overview
## Structure
### Core Modules
### Key Classes
#### Core Business Logic
#### Data Entities
## Functionality
### Order Management
### Pricing & Quoting
### Route Planning
### Tracking & Visibility
### Returns & Refunds
## Technology Stack
## Configuration
### Application Properties
# Server configuration
# Database configuration
# Kafka configuration
# Redis configuration
# Geolocation configuration
## Running the Service
### Local Development
### Docker Deployment
## API
### REST Endpoints
#### Order Management
#### Pricing
#### Tracking
#### Returns
## Integrations
### Internal Services
### External Systems
## Processes
### Standard Order Flow
### Strategic Mode
## Security & Compliance
### Security Features
### Compliance Requirements
## Contributing
## License
 
 ==== scm-service ==== 
# SCM Service (Security & Compliance Management)
## Архитектура
### 1. Market Median Oracle & Manual Cost Validation
### 2. External Compliance Adapters (Global Trade Management)
### 3. Dynamic Geofencing Logic
### 4. Evidence Package Generator
### 5. WORM Storage Layer (Infrastructure Level)
### 6. Trust Score 2.0
## gRPC API
### Новые методы:
## OpenAPI API
### Основные эндпоинты:
## Интеграции
### Системы:
### Протоколы:
## Безопасность
### Особенности:
## Зависимости
### Основные:
### Безопасность:
## Запуск
# Сборка
# Запуск
# Тестирование
## Мониторинг
### Метрики:
### Логирование:
## Лицензирование
## Контакты
- Slack: #scm-service
 
 ==== tms-service ==== 
# Сервис управления транспортом (Transportation Management Service - TMS)
## Overview
## Structure
### Core Modules
### Key Classes
#### Core Business Logic
#### Data Entities
## Functionality
### Route Planning
### Route Optimization
### Carbon Footprint Calculation
### Geofencing
### Traffic Monitoring
## Technology Stack
## Configuration
### Application Properties
# Server configuration
# Database configuration
# Maps integration configuration
# Routing configuration
# Carbon calculation configuration
# Geofencing configuration
## Running the Service
### Local Development
### Docker Deployment
## API
### REST Endpoints
#### Route Planning
#### Carbon Footprint
#### Geofencing
#### Traffic Monitoring
## Integrations
### External Systems
### Protocols
## Sustainability
### Carbon Footprint Management
### Strategic Mode
## Security & Compliance
### Security Features
### Compliance Requirements
## Contributing
## License
 
 ==== wms-service ==== 
# Сервис управления складом (Warehouse Management Service - WMS)
## Overview
## Structure
### Core Modules
### Key Classes
#### Core Business Logic
#### Data Entities
## Functionality
### Core WMS Operations
### Yard Management System (YMS)
### Reverse Logistics
### AGV & Robotics Integration
### Inventory Management
## Technology Stack
## Configuration
### Application Properties
# Server configuration
# Database configuration
# Redis configuration
# RFID integration configuration
# AGV integration configuration
# Inventory configuration
## Running the Service
### Local Development
### Docker Deployment
## API
### REST Endpoints
#### Inventory Management
#### Warehouse Operations
#### Yard Management
#### Reverse Logistics
#### AGV Management
## Integrations
### Internal Services
### External Systems
## Mobile Applications
## Security & Compliance
### Security Features
### Compliance Requirements
## Performance
## Contributing
## License
 
