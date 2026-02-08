# Economic Engine of Autonomous-OP

## Overview
Economic Engine предоставляет интеллектуальное управление стоимостями и динамической ценовой политикой для логистических операций. Он включает формальные стоимостные функции, обработку граничных случаев и детерминированную логику fallback.

## Design Principles
1. **Explainability** - Все расчеты должны быть понятны и трассируемы
2. **Traceability** - Каждый результат имеет прозрачные входные параметры
3. **Simulation Support** - Поддержка симуляционного режима для анализа сценариев
4. **Real-time Operations** - Работа в реальном времени для принятия оперативных решений
5. **Robustness** - Детерминированная логика fallback при отсутствии данных

## Mathematical Model

### 1. Total Logistics Cost Function
Формальная функция для расчета полной стоимости логистической операции:

```math
C_{total} = C_{base} + C_{distance} + C_{weight} + C_{volume} + C_{fuel} + C_{time} + C_{risk} + C_{special}
```

#### Base Cost (C_base)
```math
C_{base} = P_{base} \times D_{factor}
```
- $P_{base}$ - Базовая цена (из PriceReference)
- $D_{factor}$ - Фактор базовой стоимости (зависит от типа операции)

#### Distance Cost (C_distance)
```math
C_{distance} = P_{km} \times D \times F_{traffic} \times F_{road}
```
- $P_{km}$ - Стоимость за км
- $D$ - Расстояние (км)
- $F_{traffic}$ - Фактор трафика (1.0 - 2.0)
- $F_{road}$ - Фактор качества дороги (0.8 - 1.2)

#### Weight Cost (C_weight)
```math
C_{weight} = \begin{cases} 
P_{kg} \times W & W \leq W_{max} \\
P_{kg} \times W_{max} + P_{kg_{over}} \times (W - W_{max}) & W > W_{max}
\end{cases}
```
- $P_{kg}$ - Стоимость за кг
- $P_{kg_{over}}$ - Стоимость за кг сверх нормы
- $W$ - Вес груза (кг)
- $W_{max}$ - Максимальный допустимый вес

#### Volume Cost (C_volume)
```math
C_{volume} = \begin{cases} 
P_{m3} \times V & V \leq V_{max} \\
P_{m3} \times V_{max} + P_{m3_{over}} \times (V - V_{max}) & V > V_{max}
\end{cases}
```
- $P_{m3}$ - Стоимость за м³
- $P_{m3_{over}}$ - Стоимость за м³ сверх нормы
- $V$ - Объем груза (м³)
- $V_{max}$ - Максимальный допустимый объем

#### Fuel Cost (C_fuel)
```math
C_{fuel} = F_{price} \times (D \times F_{consumption} \times F_{load})
```
- $F_{price}$ - Цена топлива (USD/л)
- $F_{consumption}$ - Потребление топлива (л/км)
- $F_{load}$ - Фактор нагрузки (1.0 - 1.5)

#### Time Cost (C_time)
```math
C_{time} = P_{hour} \times T \times F_{urgency}
```
- $P_{hour}$ - Стоимость за час
- $T$ - Время доставки (часы)
- $F_{urgency}$ - Фактор срочности (1.0 - 3.0)

#### Risk Cost (C_risk)
```math
C_{risk} = C_{base} \times R_{factor}
```
- $R_{factor}$ - Фактор риска (0.0 - 0.5)

#### Special Requirements Cost (C_special)
```math
C_{special} = \sum (S_i \times Q_i)
```
- $S_i$ - Стоимость специального требования
- $Q_i$ - Количество/вес специального требования

### 2. Dynamic Pricing Function
Функция динамической цены с учетом спроса и предложения:

```math
P_{dynamic} = C_{total} \times (1 + M_{target} + F_{demand} - F_{supply})
```
- $M_{target}$ - Целевая маржа
- $F_{demand}$ - Фактор спроса (0.0 - 1.0)
- $F_{supply}$ - Фактор предложения (0.0 - 1.0)

### 3. Profitability Function
Расчет прибыльности операции:

```math
Profit = P_{dynamic} - C_{total}
Margin = \frac{Profit}{C_{total}} \times 100\%
```

## Data Schemas

### Input Data Schema
```json
{
  "operation_id": "OP-2024-001",
  "operation_type": "TRANSPORTATION",
  "material_type": "GENERAL",
  "weight": 5000,
  "volume": 15,
  "distance": 250,
  "time": 5,
  "urgency": "NORMAL",
  "route_info": {
    "traffic_factor": 1.1,
    "road_factor": 1.0,
    "risk_factor": 0.15
  },
  "fuel_info": {
    "price": 1.5,
    "consumption": 0.3
  },
  "supply_demand": {
    "demand_factor": 0.2,
    "supply_factor": 0.1
  },
  "special_requirements": [
    {
      "type": "TEMPERATURE_CONTROL",
      "quantity": 1,
      "cost": 100
    }
  ],
  "timestamp": "2024-02-06T22:45:00Z"
}
```

### Cost Breakdown Schema
```json
{
  "operation_id": "OP-2024-001",
  "total_cost": 1250.50,
  "base_cost": 300.00,
  "distance_cost": 412.50,
  "weight_cost": 200.00,
  "volume_cost": 150.00,
  "fuel_cost": 112.50,
  "time_cost": 50.00,
  "risk_cost": 45.00,
  "special_cost": 30.50,
  "dynamic_price": 1563.13,
  "profit": 312.63,
  "margin": 25.0,
  "timestamp": "2024-02-06T22:45:00Z",
  "traceability": {
    "input_version": "v1.0",
    "calculation_method": "economic-engine-v1",
    "parameters": {
      "base_price": 300,
      "per_km_price": 1.5,
      "per_kg_price": 0.04,
      "fuel_price": 1.5
    },
    "data_sources": [
      "SCM:PriceReference",
      "TMS:RouteInfo",
      "MarketData:FuelPrice"
    ]
  }
}
```

## Update Strategies for Dynamic Pricing

### 1. Real-time Updates
```
Strategy: EVENT_DRIVEN
Trigger: Kafka events from TMS, FMS, Marketplace
Frequency: Every 30 seconds
Scope: Route-specific prices
```

### 2. Batch Updates
```
Strategy: SCHEDULED
Trigger: Cron job (every hour)
Frequency: 60 minutes
Scope: Global price references
```

### 3. Demand-Supply Balancing
```
Strategy: PREDICTIVE
Trigger: ML model predictions
Frequency: Every 15 minutes
Scope: Region-specific dynamic factors
```

### 4. Emergency Updates
```
Strategy: MANUAL_OVERRIDE
Trigger: Admin API call
Frequency: On-demand
Scope: Global or regional prices
```

## Edge Cases Handling

### 1. Missing Data
```
Scenario: Price reference not found for material type
Fallback: Use category average + 20% buffer
Data Source: SCM PriceReference history
```

### 2. Stale Prices
```
Scenario: Fuel price data > 6 hours old
Fallback: Use last known price + inflation adjustment (0.1% per hour)
Data Source: Market data cache + inflation model
```

### 3. Inconsistent Data
```
Scenario: Conflicting distance calculations from TMS and GPS
Fallback: Use weighted average (TMS: 70%, GPS: 30%)
Data Source: TMS + GPS data
```

### 4. Network Failures
```
Scenario: Cannot connect to SCM service
Fallback: Use local cache (last 24 hours data)
Data Source: Redis cache
```

### 5. Invalid Inputs
```
Scenario: Negative weight or volume
Fallback: Set to minimum value (1 kg / 0.01 m³)
Validation: Input sanitization before calculation
```

## Deterministic Fallback Logic

### Fallback Hierarchy
```
1. Primary Data Source (SCM, TMS, FMS)
2. Secondary Data Source (Cache, History)
3. Default Values (Category Averages)
4. Static Fallbacks (Hardcoded minimums/maximums)
```

### Fallback Configuration
```yaml
fallback:
  enabled: true
  timeout: 5000
  strategies:
    - name: cache
      priority: 1
      ttl: 86400
    - name: history
      priority: 2
      days_back: 7
    - name: default
      priority: 3
      factor: 1.2
    - name: static
      priority: 4
      minimum: 50
```

## Simulation Mode

### Simulation Configuration
```json
{
  "simulation_id": "SIM-2024-001",
  "mode": "MONTE_CARLO",
  "iterations": 1000,
  "variables": {
    "fuel_price": { "min": 1.2, "max": 1.8, "distribution": "NORMAL" },
    "demand_factor": { "min": 0.0, "max": 0.5, "distribution": "UNIFORM" },
    "traffic_factor": { "min": 1.0, "max": 1.5, "distribution": "LOGNORMAL" }
  },
  "constraints": {
    "max_cost": 2000,
    "min_margin": 15
  },
  "output": {
    "metrics": ["profit", "margin", "risk"],
    "confidence_level": 0.95
  }
}
```

### Simulation Results
```json
{
  "simulation_id": "SIM-2024-001",
  "total_iterations": 1000,
  "successful_iterations": 985,
  "results": {
    "profit": {
      "mean": 285.67,
      "std": 45.23,
      "min": 150.00,
      "max": 420.00,
      "percentiles": {
        "25": 250.00,
        "50": 280.00,
        "75": 320.00,
        "95": 380.00
      }
    },
    "margin": {
      "mean": 23.5,
      "std": 3.2,
      "min": 15.0,
      "max": 30.0
    },
    "risk": {
      "low": 750,
      "medium": 225,
      "high": 10
    }
  },
  "violations": {
    "max_cost": 15,
    "min_margin": 0
  },
  "recommendations": [
    {
      "type": "PRICE_ADJUSTMENT",
      "value": "+5%",
      "reason": "Reduce high risk scenarios",
      "confidence": 0.92
    }
  ]
}
```

## Pseudocode

### Economic Engine Core
```
class EconomicEngine:
    def __init__(self, scm_client, tms_client, fms_client, cache):
        self.scm_client = scm_client
        self.tms_client = tms_client
        self.fms_client = fms_client
        self.cache = cache
        self.logger = Logger()

    async def calculate_cost(self, operation_input):
        try:
            # Validate inputs
            if not self._validate_inputs(operation_input):
                raise InputValidationError("Invalid inputs")

            # Get base prices from SCM
            base_prices = await self._get_base_prices(operation_input.material_type)

            # Calculate cost components
            cost_breakdown = self._calculate_cost_components(operation_input, base_prices)

            # Calculate dynamic price
            dynamic_price = self._calculate_dynamic_price(cost_breakdown, operation_input.supply_demand)

            # Add profitability metrics
            cost_breakdown["dynamic_price"] = dynamic_price
            cost_breakdown["profit"] = dynamic_price - cost_breakdown["total_cost"]
            cost_breakdown["margin"] = (cost_breakdown["profit"] / cost_breakdown["total_cost"]) * 100

            # Add traceability
            cost_breakdown["traceability"] = self._generate_traceability(operation_input, base_prices)

            return cost_breakdown

        except Exception as e:
            self.logger.error(f"Cost calculation failed: {e}")
            return self._fallback_calculation(operation_input, e)

    def _validate_inputs(self, input):
        if input.weight <= 0 or input.volume <= 0 or input.distance <= 0:
            return False
        return True

    async def _get_base_prices(self, material_type):
        try:
            return await self.scm_client.get_price_reference(material_type)
        except:
            return self._get_fallback_base_prices(material_type)

    def _get_fallback_base_prices(self, material_type):
        # Try cache
        cached = self.cache.get(f"base_prices:{material_type}")
        if cached:
            return cached

        # Try historical average
        history = self._get_price_history(material_type)
        if history:
            avg = sum(history) / len(history)
            self.cache.set(f"base_prices:{material_type}", avg, 3600)
            return avg

        # Default fallback
        return self._get_category_average(material_type)

    def _calculate_cost_components(self, input, base_prices):
        cost = {
            "base_cost": self._calculate_base_cost(base_prices, input),
            "distance_cost": self._calculate_distance_cost(base_prices, input),
            "weight_cost": self._calculate_weight_cost(base_prices, input),
            "volume_cost": self._calculate_volume_cost(base_prices, input),
            "fuel_cost": self._calculate_fuel_cost(input),
            "time_cost": self._calculate_time_cost(base_prices, input),
            "risk_cost": self._calculate_risk_cost(base_prices, input),
            "special_cost": self._calculate_special_cost(input)
        }

        cost["total_cost"] = sum(cost.values())
        return cost

    def _fallback_calculation(self, input, error):
        fallback_cost = self._calculate_minimal_cost(input)
        fallback_cost["traceability"] = {
            "calculation_method": "fallback-v1",
            "error": str(error),
            "data_sources": ["Fallback"]
        }
        return fallback_cost

    def simulate_scenario(self, scenario_config):
        results = []
        for _ in range(scenario_config.iterations):
            # Generate random variables
            simulated_input = self._generate_simulated_input(scenario_config)

            # Calculate cost
            cost_result = self.calculate_cost(simulated_input)

            # Apply constraints
            if self._apply_constraints(cost_result, scenario_config.constraints):
                results.append(cost_result)

        return self._analyze_simulation_results(results, scenario_config)
```

### Cost Calculation Components
```
class CostCalculator:
    def _calculate_base_cost(self, base_prices, input):
        return base_prices.base_cost * self._get_base_factor(input.operation_type)

    def _calculate_distance_cost(self, base_prices, input):
        return base_prices.per_km * input.distance * input.route_info.traffic_factor * input.route_info.road_factor

    def _calculate_weight_cost(self, base_prices, input):
        if input.weight <= base_prices.max_weight:
            return base_prices.per_kg * input.weight
        else:
            over = input.weight - base_prices.max_weight
            return base_prices.per_kg * base_prices.max_weight + base_prices.per_kg_over * over

    def _calculate_volume_cost(self, base_prices, input):
        if input.volume <= base_prices.max_volume:
            return base_prices.per_m3 * input.volume
        else:
            over = input.volume - base_prices.max_volume
            return base_prices.per_m3 * base_prices.max_volume + base_prices.per_m3_over * over

    def _calculate_fuel_cost(self, input):
        consumption = input.fuel_info.consumption
        load_factor = self._get_load_factor(input.weight, input.volume)
        fuel_amount = input.distance * consumption * load_factor
        return fuel_amount * input.fuel_info.price

    def _calculate_time_cost(self, base_prices, input):
        urgency_factor = self._get_urgency_factor(input.urgency)
        return base_prices.per_hour * input.time * urgency_factor

    def _calculate_risk_cost(self, base_prices, input):
        return base_prices.base_cost * input.route_info.risk_factor

    def _calculate_special_cost(self, input):
        return sum(sr.cost * sr.quantity for sr in input.special_requirements)

    def _calculate_dynamic_price(self, cost_breakdown, supply_demand):
        target_margin = self._get_target_margin()
        price = cost_breakdown["total_cost"] * (1 + target_margin)
        price = price * (1 + supply_demand.demand_factor - supply_demand.supply_factor)
        return max(price, cost_breakdown["total_cost"] * 1.05)  # Minimum 5% margin
```

## API Contract

### Cost Calculation API
```proto
// Economic Engine gRPC API
syntax = "proto3";

package com.autonomous.economic;

import "google/protobuf/timestamp.proto";
import "google/protobuf/wrappers.proto";

// Economic Engine service definition
service EconomicEngineService {
    // Calculate total cost for an operation
    rpc CalculateCost(CalculateCostRequest) returns (CostBreakdownResponse);

    // Calculate dynamic price for an operation
    rpc CalculateDynamicPrice(CalculateDynamicPriceRequest) returns (DynamicPriceResponse);

    // Get price reference for material type
    rpc GetPriceReference(PriceReferenceRequest) returns (PriceReferenceResponse);

    // Run simulation
    rpc RunSimulation(SimulationRequest) returns (SimulationResponse);

    // Get cost history
    rpc GetCostHistory(CostHistoryRequest) returns (CostHistoryResponse);

    // Update price references
    rpc UpdatePriceReference(UpdatePriceReferenceRequest) returns (UpdatePriceReferenceResponse);
}

// Calculate Cost Request
message CalculateCostRequest {
    string operation_id = 1;
    string operation_type = 2;
    string material_type = 3;
    double weight = 4;
    double volume = 5;
    double distance = 6;
    double time = 7;
    string urgency = 8;
    RouteInfo route_info = 9;
    FuelInfo fuel_info = 10;
    SupplyDemandInfo supply_demand = 11;
    repeated SpecialRequirement special_requirements = 12;
    google.protobuf.Timestamp timestamp = 13;
}

// Route Information
message RouteInfo {
    double traffic_factor = 1;
    double road_factor = 2;
    double risk_factor = 3;
}

// Fuel Information
message FuelInfo {
    double price = 1;
    double consumption = 2;
}

// Supply and Demand Information
message SupplyDemandInfo {
    double demand_factor = 1;
    double supply_factor = 2;
}

// Special Requirement
message SpecialRequirement {
    string type = 1;
    int32 quantity = 2;
    double cost = 3;
}

// Cost Breakdown Response
message CostBreakdownResponse {
    string operation_id = 1;
    double total_cost = 2;
    double base_cost = 3;
    double distance_cost = 4;
    double weight_cost = 5;
    double volume_cost = 6;
    double fuel_cost = 7;
    double time_cost = 8;
    double risk_cost = 9;
    double special_cost = 10;
    double dynamic_price = 11;
    double profit = 12;
    double margin = 13;
    google.protobuf.Timestamp timestamp = 14;
    Traceability traceability = 15;
}

// Traceability Information
message Traceability {
    string input_version = 1;
    string calculation_method = 2;
    map<string, string> parameters = 3;
    repeated string data_sources = 4;
}

// Calculate Dynamic Price Request
message CalculateDynamicPriceRequest {
    string operation_id = 1;
    double total_cost = 2;
    SupplyDemandInfo supply_demand = 3;
}

// Dynamic Price Response
message DynamicPriceResponse {
    string operation_id = 1;
    double dynamic_price = 2;
    double profit = 3;
    double margin = 4;
    google.protobuf.Timestamp timestamp = 5;
}

// Price Reference Request
message PriceReferenceRequest {
    string material_type = 1;
}

// Price Reference Response
message PriceReferenceResponse {
    string material_type = 1;
    double base_cost = 2;
    double per_km = 3;
    double per_kg = 4;
    double per_kg_over = 5;
    double per_m3 = 6;
    double per_m3_over = 7;
    double per_hour = 8;
    double max_weight = 9;
    double max_volume = 10;
}

// Update Price Reference Request
message UpdatePriceReferenceRequest {
    string material_type = 1;
    google.protobuf.DoubleValue base_cost = 2;
    google.protobuf.DoubleValue per_km = 3;
    google.protobuf.DoubleValue per_kg = 4;
    google.protobuf.DoubleValue per_kg_over = 5;
    google.protobuf.DoubleValue per_m3 = 6;
    google.protobuf.DoubleValue per_m3_over = 7;
    google.protobuf.DoubleValue per_hour = 8;
    google.protobuf.DoubleValue max_weight = 9;
    google.protobuf.DoubleValue max_volume = 10;
}

// Update Price Reference Response
message UpdatePriceReferenceResponse {
    bool success = 1;
    string message = 2;
    PriceReferenceResponse price_reference = 3;
}

// Simulation Request
message SimulationRequest {
    string simulation_id = 1;
    string mode = 2;
    int32 iterations = 3;
    map<string, VariableDistribution> variables = 4;
    map<string, double> constraints = 5;
    SimulationOutputConfig output = 6;
}

// Variable Distribution
message VariableDistribution {
    double min = 1;
    double max = 2;
    string distribution = 3;
}

// Simulation Output Configuration
message SimulationOutputConfig {
    repeated string metrics = 1;
    double confidence_level = 2;
}

// Simulation Response
message SimulationResponse {
    string simulation_id = 1;
    int32 total_iterations = 2;
    int32 successful_iterations = 3;
    SimulationResults results = 4;
    map<string, int32> violations = 5;
    repeated Recommendation recommendations = 6;
}

// Simulation Results
message SimulationResults {
    map<string, MetricStatistics> metrics = 1;
}

// Metric Statistics
message MetricStatistics {
    double mean = 1;
    double std = 2;
    double min = 3;
    double max = 4;
    map<string, double> percentiles = 5;
}

// Recommendation
message Recommendation {
    string type = 1;
    string value = 2;
    string reason = 3;
    double confidence = 4;
}

// Cost History Request
message CostHistoryRequest {
    string operation_type = 1;
    string material_type = 2;
    google.protobuf.Timestamp start_time = 3;
    google.protobuf.Timestamp end_time = 4;
    int32 limit = 5;
}

// Cost History Response
message CostHistoryResponse {
    repeated CostBreakdownResponse items = 1;
    int32 total_count = 2;
}
```

## REST API Endpoints

### Cost Calculation
```
POST /api/v1/economic/calculate-cost
Request: CalculateCostRequest
Response: CostBreakdownResponse
```

### Dynamic Price Calculation
```
POST /api/v1/economic/calculate-dynamic-price
Request: CalculateDynamicPriceRequest
Response: DynamicPriceResponse
```

### Price Reference
```
GET /api/v1/economic/price-reference?material_type={type}
Response: PriceReferenceResponse

PUT /api/v1/economic/price-reference
Request: UpdatePriceReferenceRequest
Response: UpdatePriceReferenceResponse
```

### Simulation
```
POST /api/v1/economic/simulation
Request: SimulationRequest
Response: SimulationResponse
```

### Cost History
```
GET /api/v1/economic/history
Query Parameters:
- operation_type (optional)
- material_type (optional)
- start_time (optional)
- end_time (optional)
- limit (optional, default: 100)
Response: CostHistoryResponse
```

## Configuration

### Economic Engine Configuration
```yaml
logi:
  autonomous:
    economic:
      enabled: true
      mode: realtime  # or simulation
      # Price calculation settings
      base_margin: 0.25
      min_margin: 0.05
      max_margin: 0.75
      # Dynamic pricing settings
      demand_sensitivity: 0.3
      supply_sensitivity: 0.2
      # Fallback settings
      fallback:
        enabled: true
        cache_ttl: 86400
        history_days: 7
        default_factor: 1.2
      # Simulation settings
      simulation:
        default_iterations: 1000
        default_confidence: 0.95
        max_iterations: 10000
      # Monitoring
      metrics:
        enabled: true
        update_interval: 30000
```

## Metrics

### Prometheus Metrics
```
# Economic Engine Metrics
autonomous_ops_economic_calculations_total
autonomous_ops_economic_calculation_time_seconds
autonomous_ops_economic_fallbacks_total
autonomous_ops_economic_simulation_runs_total
autonomous_ops_economic_simulation_iterations_total
autonomous_ops_economic_dynamic_price_updates_total
autonomous_ops_economic_margin_avg
```

## Logging

### Log Levels
```
DEBUG: Cost calculation details, traceability information
INFO: Successful calculations, simulation results
WARN: Fallback operations, stale data
ERROR: Calculation failures, invalid inputs
```

### Log Fields
```json
{
  "timestamp": "2024-02-06T22:45:00Z",
  "level": "INFO",
  "operation_id": "OP-2024-001",
  "calculation_method": "economic-engine-v1",
  "total_cost": 1250.50,
  "dynamic_price": 1563.13,
  "margin": 25.0,
  "data_sources": ["SCM", "TMS", "MarketData"],
  "processing_time": 0.125
}
```

## Conclusion
Economic Engine обеспечивает точный и трассируемый расчет стоимостей для логистических операций с поддержкой динамической ценовой политики и симуляционного анализа. Логика fallback гарантирует работу в условиях отсутствия или недостоверности данных, а формальные стоимостные функции обеспечивают объяснимость результатов.