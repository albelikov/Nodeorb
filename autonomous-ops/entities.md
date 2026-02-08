# Сущности Autonomous Ops и их взаимодействие с сервисами Nodeorb

## Входные сущности (Input Entities)

### 1. Mission & Task Management
- **Mission** - Взаимодействие с сервисами:
  - `FMS (Fleet Management System)` - Получение информации о доступных роботах
  - `TMS (Transportation Management System)` - Получение данных о маршрутах и ограничениях
  - `WMS (Warehouse Management System)` - Получение информации о складах и грузах
- **Task** - Взаимодействие с сервисами:
  - `FMS` - Управление статусами подзадач для роботов
  - `TMS` - Проверка доступности маршрутов для подзадач
- **PayloadSpec** - Взаимодействие с сервисами:
  - `WMS` - Получение информации о грузе
- **MissionConstraints** - Взаимодействие с сервисами:
  - `TMS` - Проверка ограничений маршрута
  - `SCM (Safety & Compliance Management)` - Проверка соответствия стандартам

### 2. Node (Robot/Drone) Profiles
- **NodeProfile** - Взаимодействие с сервисами:
  - `FMS` - Получение и обновление информации о роботах
  - `TMS` - Проверка совместимости робота с маршрутом
  - `Predictive Maintenance (внутренний)` - Оценка состояния оборудования
- **NodeCapabilities** - Взаимодействие с сервисами:
  - `FMS` - Получение информации о возможностях робота
  - `TMS` - Проверка совместимости с маршрутом
- **HardwareSpecs** - Взаимодействие с сервисами:
  - `FMS` - Получение технической информации о роботе
- **NodeCostParameters** - Взаимодействие с сервисами:
  - `OMS (Order Management System)` - Получение стоимостных данных
  - `Billing & Pricing` - Подсчет стоимости использования робота

### 3. Environmental Context
- **WeatherCondition** - Взаимодействие с сервисами:
  - `Weather API` - Получение погодных данных
  - `TMS` - Проверка влияния погоды на маршрут
- **AirspaceRestriction** - Взаимодействие с сервисами:
  - `TMS` - Проверка маршрута на соответствие ограничениям
  - `SCM` - Проверка соответствия правилам управления воздушным пространством
- **TrafficData** - Взаимодействие с сервисами:
  - `TMS` - Получение данных о трафике
  - `External Traffic APIs` - Интеграция с системами прогноза трафика

### 4. Economic Parameters
- **CostParameters** - Взаимодействие с сервисами:
  - `OMS` - Получение стоимостных параметров
  - `Billing & Pricing` - Подсчет стоимости миссии
- **IndustryPreset** - Взаимодействие с сервисами:
  - `OMS` - Получение пресетов для отраслей

## Выходные сущности (Output Entities)

### 1. Optimized Mission Plan
- **MissionPlan** - Взаимодействие с сервисами:
  - `FMS` - Назначение робота для миссии
  - `TMS` - Отправка плана маршрута
  - `WMS` - Подготовка склада для миссии
- **Route** - Взаимодействие с сервисами:
  - `TMS` - Отправка маршрута для робота
- **CostBreakdown** - Взаимодействие с сервисами:
  - `OMS` - Подсчет стоимости миссии
  - `Billing & Pricing` - Генерация счета
- **RiskAssessment** - Взаимодействие с сервисами:
  - `SCM` - Проверка рисков
- **AlternativePlan** - Взаимодействие с сервисами:
  - `OMS` - Предложение альтернативных планов
- **PlanExplanation** - Взаимодействие с сервисами:
  - `Explainable AI` - Объяснение решения AI

### 2. Execution Status & Telemetry
- **MissionExecutionStatus** - Взаимодействие с сервисами:
  - `FMS` - Отправка статуса миссии
  - `TMS` - Обновление статуса маршрута
  - `WMS` - Обновление статуса склада
- **Alert** - Взаимодействие с сервисами:
  - `FMS` - Отправка алертов о роботе
  - `TMS` - Отправка алертов о маршруте
  - `SCM` - Отправка алертов о безопасности
- **TelemetryData** - Взаимодействие с сервисами:
  - `FMS` - Получение телеметрии с робота
  - `TMS` - Обновление позиции робота

### 3. Performance Metrics & Analytics
- **MissionPerformanceMetrics** - Взаимодействие с сервисами:
  - `Analytics & Reporting` - Сбор и анализ метрик
- **NodeAnalytics** - Взаимодействие с сервисами:
  - `FMS` - Сбор данных о роботах
  - `Predictive Maintenance` - Анализ состояния оборудования

### 4. Decision Logs & Audit Trail
- **AIDecisionLog** - Взаимодействие с сервисами:
  - `SCM` - Аудит и логирование
  - `Analytics & Reporting` - Анализ решений AI

## Внутренние сущности (Internal Processing)

### 1. Optimization Models & Scenarios
- **OptimizationScenario** - Взаимодействие с сервисами:
  - `TMS` - Проверка сценариев маршрута
- **SwarmMission** - Взаимодействие с сервисами:
  - `FMS` - Координация группы роботов

### 2. Swarm Coordination
- **SwarmMission** - Взаимодействие с сервисами:
  - `FMS` - Координация группы роботов
- **A2ANegotiation** - Взаимодействие с сервисами:
  - `FMS` - Переговоры между роботами
  - `TMS` - Обмен информацией о маршрутах

## Сводная таблица взаимодействий с сервисами Nodeorb

| Сервис Nodeorb                  | Сущности, с которыми взаимодействует |
|---------------------------------|-------------------------------------|
| **FMS (Fleet Management System)** | Mission, Task, NodeProfile, NodeCapabilities, HardwareSpecs, MissionExecutionStatus, Alert, TelemetryData, SwarmMission, A2ANegotiation |
| **TMS (Transportation Management System)** | Mission, Task, NodeProfile, NodeCapabilities, WeatherCondition, AirspaceRestriction, TrafficData, MissionPlan, Route, MissionExecutionStatus, Alert, TelemetryData, SwarmMission, A2ANegotiation |
| **WMS (Warehouse Management System)** | Mission, Task, PayloadSpec, MissionExecutionStatus |
| **OMS (Order Management System)** | CostParameters, IndustryPreset, MissionPlan, CostBreakdown, AlternativePlan |
| **SCM (Safety & Compliance Management)** | MissionConstraints, AirspaceRestriction, RiskAssessment, Alert, AIDecisionLog |
| **Predictive Maintenance (внутренний)** | NodeProfile, HardwareSpecs, WeatherCondition, NodeAnalytics |
| **Billing & Pricing** | NodeCostParameters, CostParameters, CostBreakdown |
| **Analytics & Reporting** | MissionPerformanceMetrics, NodeAnalytics, AIDecisionLog |
| **Weather API** | WeatherCondition |
| **External Traffic APIs** | TrafficData |
| **Explainable AI** | MissionPlan, PlanExplanation, AIDecisionLog |