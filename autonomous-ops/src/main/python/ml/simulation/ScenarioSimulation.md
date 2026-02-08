# Scenario Simulation Subsystem

## Overview
Scenario Simulation subsystem provides Monte Carlo-based simulation of logistics operations with risk modeling, weather, traffic, and system uncertainty. It supports deterministic seed for audit purposes and produces structured outputs for UI and audit trails.

## Design Principles
1. **Monte Carlo Simulation** - 1000+ runs for statistical significance
2. **Risk Modeling** - Probability × Impact for comprehensive risk assessment
3. **Realistic Uncertainty** - Weather, traffic, and system variability
4. **Deterministic Seeding** - Reproducible simulations for audits
5. **Structured Outputs** - Standardized schemas for UI and audit trails
6. **Parallel Execution** - Efficient processing of large simulation runs

## Architecture

### Simulation Pipeline
```
┌─────────────────────────────────────────────────────────────────┐
│                          Simulation Pipeline                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Input      │  │   Parameter  │  │   Scenario   │          │
│  │  Validation  │  │  Distribution│  │  Generation  │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
│         │                 │                 │                   │
│         └────────────┬────┴─────────────────┘                   │
│                      ▼                                           │
│              ┌──────────────┐                                    │
│              │   Parallel   │                                    │
│              │  Execution   │                                    │
│              └──────┬───────┘                                    │
│                     │                                            │
│         ┌───────────┴───────────┐                               │
│         ▼                       ▼                               │
│  ┌──────────────┐      ┌──────────────┐                          │
│  │   Risk       │      │   Cost       │                          │
│  │  Assessment  │      │  Calculation │                          │
│  └──────┬───────┘      └──────┬───────┘                          │
│         │                     │                                  │
│         └──────────┬──────────┘                                  │
│                    ▼                                             │
│            ┌──────────────┐                                       │
│            │   Results    │                                       │
│            │  Aggregation │                                       │
│            └──────┬───────┘                                       │
│                   │                                               │
│         ┌──────────┴──────────┐                                  │
│         ▼                      ▼                                 │
│  ┌──────────────┐       ┌──────────────┐                        │
│  │   Output     │       │   Audit      │                        │
│  │  Generation  │       │  Generation  │                        │
│  └──────┬───────┘       └──────┬───────┘                        │
│         │                      │                                 │
│         └──────────┬──────────┘                                 │
│                    ▼                                             │
│            ┌──────────────┐                                      │
│            │   Storage    │                                      │
│            └──────────────┘                                      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Core Components

#### 1. Input Validation
```kotlin
class InputValidator {
    fun validate(scenario: SimulationScenario): ValidationResult {
        require(scenario.iterations >= 1000) { "At least 1000 iterations required" }
        require(scenario.parameters.isNotEmpty()) { "No parameters specified" }
        validateParameterDistributions(scenario.parameters)
        validateConstraints(scenario.constraints)
        return ValidationResult(success = true)
    }
    
    private fun validateParameterDistributions(params: Map<String, ParameterDistribution>) {
        params.forEach { (name, dist) ->
            require(dist.min <= dist.max) { "Min must be <= max for $name" }
            require(dist.distribution in VALID_DISTRIBUTIONS) { "Invalid distribution: ${dist.distribution}" }
            if (dist.distribution == "NORMAL") {
                require(dist.stdDev > 0) { "Standard deviation must be positive for normal distribution" }
            }
        }
    }
    
    private fun validateConstraints(constraints: Map<String, Constraint>) {
        constraints.forEach { (name, constraint) ->
            require(constraint.operator in VALID_OPERATORS) { "Invalid operator: ${constraint.operator}" }
            require(constraint.value > 0) { "Constraint value must be positive" }
        }
    }
}
```

#### 2. Parameter Distribution
```kotlin
data class ParameterDistribution(
    val min: Double,
    val max: Double,
    val distribution: String = "UNIFORM",
    val stdDev: Double? = null,
    val mean: Double? = null
)

enum class DistributionType {
    UNIFORM,
    NORMAL,
    LOGNORMAL,
    EXPONENTIAL,
    TRIANGULAR
}
```

#### 3. Scenario Generation
```kotlin
class ScenarioGenerator(private val random: Random) {
    fun generate(scenario: SimulationScenario): List<SimulatedScenario> {
        val scenarios = mutableListOf<SimulatedScenario>()
        val baseInput = scenario.baseInput
        
        repeat(scenario.iterations) { iteration ->
            val variableValues = scenario.parameters.mapValues { (name, dist) ->
                generateRandomValue(dist)
            }
            
            scenarios.add(
                SimulatedScenario(
                    iterationId = iteration,
                    baseInput = baseInput,
                    variableValues = variableValues,
                    seed = random.nextLong()
                )
            )
        }
        
        return scenarios
    }
    
    private fun generateRandomValue(dist: ParameterDistribution): Double {
        return when (dist.distribution) {
            "UNIFORM" -> random.nextDouble(dist.min, dist.max)
            "NORMAL" -> {
                val mean = dist.mean ?: (dist.min + dist.max) / 2
                val stdDev = dist.stdDev ?: (dist.max - dist.min) / 6
                random.nextGaussian() * stdDev + mean
            }
            "LOGNORMAL" -> {
                val mean = dist.mean ?: (dist.min + dist.max) / 2
                val stdDev = dist.stdDev ?: (dist.max - dist.min) / 6
                exp(random.nextGaussian() * stdDev + mean)
            }
            "EXPONENTIAL" -> {
                val lambda = 1.0 / (dist.mean ?: (dist.min + dist.max) / 2)
                -ln(1 - random.nextDouble()) / lambda
            }
            "TRIANGULAR" -> {
                val mode = dist.mean ?: (dist.min + dist.max) / 2
                val u = random.nextDouble()
                if (u <= (mode - dist.min) / (dist.max - dist.min)) {
                    dist.min + sqrt(u * (dist.max - dist.min) * (mode - dist.min))
                } else {
                    dist.max - sqrt((1 - u) * (dist.max - dist.min) * (dist.max - mode))
                }
            }
            else -> throw IllegalArgumentException("Unknown distribution: ${dist.distribution}")
        }
    }
}
```

#### 4. Parallel Execution
```kotlin
class SimulationExecutor(private val economicEngine: EconomicEngine) {
    fun execute(scenarios: List<SimulatedScenario>): List<SimulationResult> {
        return withContext(Dispatchers.IO) {
            val chunkSize = max(1, scenarios.size / Runtime.getRuntime().availableProcessors())
            scenarios.chunked(chunkSize).map { chunk ->
                async {
                    chunk.map { scenario ->
                        executeSingleScenario(scenario)
                    }
                }
            }.awaitAll().flatten()
        }
    }
    
    private suspend fun executeSingleScenario(scenario: SimulatedScenario): SimulationResult {
        val operationInput = scenario.baseInput.copy(
            operationId = "SIM-${scenario.iterationId}",
            fuelInfo = scenario.baseInput.fuelInfo.copy(
                price = scenario.variableValues["fuel_price"] ?: scenario.baseInput.fuelInfo.price
            ),
            routeInfo = scenario.baseInput.routeInfo.copy(
                trafficFactor = scenario.variableValues["traffic_factor"] ?: scenario.baseInput.routeInfo.trafficFactor,
                riskFactor = scenario.variableValues["risk_factor"] ?: scenario.baseInput.routeInfo.riskFactor
            ),
            supplyDemand = scenario.baseInput.supplyDemand.copy(
                demandFactor = scenario.variableValues["demand_factor"] ?: scenario.baseInput.supplyDemand.demandFactor,
                supplyFactor = scenario.variableValues["supply_factor"] ?: scenario.baseInput.supplyDemand.supplyFactor
            )
        )
        
        val costResult = economicEngine.calculateCost(operationInput)
        
        return SimulationResult(
            iterationId = scenario.iterationId,
            costResult = costResult,
            variableValues = scenario.variableValues,
            constraintsSatisfied = checkConstraints(costResult, scenario.constraints)
        )
    }
}
```

#### 5. Risk Assessment
```kotlin
class RiskAssessor {
    fun assessRisks(results: List<SimulationResult>): RiskAssessment {
        val allCosts = results.map { it.costResult.totalCost }
        val allProfits = results.map { it.costResult.profit }
        val allMargins = results.map { it.costResult.margin }
        
        return RiskAssessment(
            costRisk = assessCostRisk(allCosts),
            profitRisk = assessProfitRisk(allProfits),
            marginRisk = assessMarginRisk(allMargins),
            riskBreakdown = assessVariableRisks(results),
            overallRiskLevel = calculateOverallRiskLevel(allCosts, allProfits)
        )
    }
    
    private fun assessCostRisk(costs: List<Double>): CostRisk {
        val meanCost = costs.average()
        val stdDevCost = sqrt(costs.map { (it - meanCost) * (it - meanCost) }.average())
        val percentile95 = costs.sorted()[(costs.size * 0.95).toInt()]
        val percentile99 = costs.sorted()[(costs.size * 0.99).toInt()]
        
        return CostRisk(
            meanCost = meanCost,
            stdDevCost = stdDevCost,
            percentile95Cost = percentile95,
            percentile99Cost = percentile99,
            costVariability = stdDevCost / meanCost
        )
    }
    
    private fun assessProfitRisk(profits: List<Double>): ProfitRisk {
        val meanProfit = profits.average()
        val stdDevProfit = sqrt(profits.map { (it - meanProfit) * (it - meanProfit) }.average())
        val negativeProfitCount = profits.count { it < 0 }
        
        return ProfitRisk(
            meanProfit = meanProfit,
            stdDevProfit = stdDevProfit,
            negativeProfitProbability = negativeProfitCount.toDouble() / profits.size,
            profitVariability = stdDevProfit / meanProfit
        )
    }
    
    private fun assessMarginRisk(margins: List<Double>): MarginRisk {
        val meanMargin = margins.average()
        val stdDevMargin = sqrt(margins.map { (it - meanMargin) * (it - meanMargin) }.average())
        val belowMinMarginCount = margins.count { it < MIN_MARGIN }
        
        return MarginRisk(
            meanMargin = meanMargin,
            stdDevMargin = stdDevMargin,
            belowMinMarginProbability = belowMinMarginCount.toDouble() / margins.size
        )
    }
    
    private fun assessVariableRisks(results: List<SimulationResult>): Map<String, VariableRisk> {
        val variableRisks = mutableMapOf<String, VariableRisk>()
        
        // Get all variable names from first result
        val variableNames = results.firstOrNull()?.variableValues?.keys ?: emptySet()
        
        variableNames.forEach { variableName ->
            val values = results.map { it.variableValues[variableName] ?: 0.0 }
            val costs = results.map { it.costResult.totalCost }
            
            // Calculate correlation between variable and cost
            val correlation = calculateCorrelation(values, costs)
            
            variableRisks[variableName] = VariableRisk(
                name = variableName,
                correlation = correlation,
                sensitivity = calculateSensitivity(values, costs)
            )
        }
        
        return variableRisks
    }
    
    private fun calculateOverallRiskLevel(costs: List<Double>, profits: List<Double>): RiskLevel {
        val costRiskScore = assessCostRisk(costs).costVariability
        val profitRiskScore = assessProfitRisk(profits).negativeProfitProbability
        val marginRiskScore = assessMarginRisk(profits.map { (it * 100) / (costs.first() + it) }).belowMinMarginProbability
        
        val totalScore = costRiskScore * 0.3 + profitRiskScore * 0.4 + marginRiskScore * 0.3
        
        return when {
            totalScore < 0.15 -> RiskLevel.LOW
            totalScore < 0.35 -> RiskLevel.MEDIUM
            totalScore < 0.6 -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }
    }
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
```

#### 6. Results Aggregation
```kotlin
class ResultsAggregator {
    fun aggregate(results: List<SimulationResult>): AggregatedResults {
        val successfulResults = results.filter { it.constraintsSatisfied }
        
        return AggregatedResults(
            totalIterations = results.size,
            successfulIterations = successfulResults.size,
            costMetrics = calculateCostMetrics(successfulResults),
            profitMetrics = calculateProfitMetrics(successfulResults),
            marginMetrics = calculateMarginMetrics(successfulResults),
            constraintViolations = calculateConstraintViolations(results),
            topScenarios = findTopScenarios(successfulResults, 10),
            riskAssessment = RiskAssessor().assessRisks(successfulResults)
        )
    }
    
    private fun calculateCostMetrics(results: List<SimulationResult>): MetricStatistics {
        return calculateMetrics(results.map { it.costResult.totalCost })
    }
    
    private fun calculateProfitMetrics(results: List<SimulationResult>): MetricStatistics {
        return calculateMetrics(results.map { it.costResult.profit })
    }
    
    private fun calculateMarginMetrics(results: List<SimulationResult>): MetricStatistics {
        return calculateMetrics(results.map { it.costResult.margin })
    }
    
    private fun calculateMetrics(values: List<Double>): MetricStatistics {
        val sortedValues = values.sorted()
        val mean = sortedValues.average()
        val stdDev = sqrt(sortedValues.map { (it - mean) * (it - mean) }.average())
        
        return MetricStatistics(
            mean = mean,
            stdDev = stdDev,
            min = sortedValues.first(),
            max = sortedValues.last(),
            percentiles = mapOf(
                "25" to sortedValues[(sortedValues.size * 0.25).toInt()],
                "50" to sortedValues[(sortedValues.size * 0.5).toInt()],
                "75" to sortedValues[(sortedValues.size * 0.75).toInt()],
                "95" to sortedValues[(sortedValues.size * 0.95).toInt()],
                "99" to sortedValues[(sortedValues.size * 0.99).toInt()]
            )
        )
    }
    
    private fun calculateConstraintViolations(results: List<SimulationResult>): Map<String, Int> {
        val violations = mutableMapOf<String, Int>()
        
        results.forEach { result ->
            result.constraintViolations.forEach { (constraintName, violated) ->
                if (violated) {
                    violations[constraintName] = violations.getOrDefault(constraintName, 0) + 1
                }
            }
        }
        
        return violations
    }
    
    private fun findTopScenarios(results: List<SimulationResult>, limit: Int): List<TopScenario> {
        return results
            .sortedByDescending { it.costResult.profit }
            .take(limit)
            .mapIndexed { index, result ->
                TopScenario(
                    rank = index + 1,
                    iterationId = result.iterationId,
                    profit = result.costResult.profit,
                    margin = result.costResult.margin,
                    variableValues = result.variableValues
                )
            }
    }
}
```

## Risk Modeling

### Probability × Impact Model
```math
Risk = Probability \times Impact
```

#### Cost Risk
```math
CostRisk = P(C > C_{target}) \times (E[C | C > C_{target}] - C_{target})
```
- $P(C > C_{target})$ - Probability of cost exceeding target
- $E[C | C > C_{target}]$ - Expected cost given cost exceeds target

#### Profit Risk
```math
ProfitRisk = P(P < 0) \times |E[P | P < 0]|
```
- $P(P < 0)$ - Probability of negative profit
- $|E[P | P < 0]|$ - Expected loss magnitude

#### Margin Risk
```math
MarginRisk = P(M < M_{min}) \times (M_{min} - E[M | M < M_{min}])
```
- $P(M < M_{min})$ - Probability of margin below minimum
- $M_{min} - E[M | M < M_{min}]$ - Expected margin shortfall

### Variable Sensitivity Analysis
```math
Sensitivity = \frac{\Delta \text{Output}}{\Delta \text{Input}}
```

## Output Schema

### Simulation Results Schema
```json
{
  "simulationId": "SIM-2024-001",
  "timestamp": "2024-02-06T22:45:00Z",
  "config": {
    "iterations": 1000,
    "deterministicSeed": "123456789",
    "scenarioName": "Winter Weather Impact",
    "description": "Simulation of winter weather impact on fuel consumption and traffic"
  },
  "execution": {
    "startTime": "2024-02-06T22:45:00Z",
    "endTime": "2024-02-06T22:47:30Z",
    "duration": 150,
    "parallelism": 8
  },
  "results": {
    "totalIterations": 1000,
    "successfulIterations": 985,
    "costMetrics": {
      "mean": 1250.50,
      "stdDev": 150.75,
      "min": 980.20,
      "max": 1750.80,
      "percentiles": {
        "25": 1150.00,
        "50": 1240.00,
        "75": 1350.00,
        "95": 1550.00,
        "99": 1700.00
      }
    },
    "profitMetrics": {
      "mean": 285.67,
      "stdDev": 45.23,
      "min": 150.00,
      "max": 420.00,
      "percentiles": {
        "25": 250.00,
        "50": 280.00,
        "75": 320.00,
        "95": 380.00,
        "99": 410.00
      }
    },
    "marginMetrics": {
      "mean": 23.5,
      "stdDev": 3.2,
      "min": 15.0,
      "max": 30.0,
      "percentiles": {
        "25": 21.0,
        "50": 23.0,
        "75": 26.0,
        "95": 29.0,
        "99": 30.0
      }
    },
    "constraintViolations": {
      "maxCost": 15,
      "minMargin": 0
    },
    "topScenarios": [
      {
        "rank": 1,
        "iterationId": 456,
        "profit": 418.50,
        "margin": 29.8,
        "variableValues": {
          "fuel_price": 1.45,
          "traffic_factor": 1.15,
          "risk_factor": 0.10,
          "demand_factor": 0.18,
          "supply_factor": 0.08
        }
      }
    ],
    "riskAssessment": {
      "costRisk": {
        "meanCost": 1250.50,
        "stdDevCost": 150.75,
        "percentile95Cost": 1550.00,
        "percentile99Cost": 1700.00,
        "costVariability": 0.12
      },
      "profitRisk": {
        "meanProfit": 285.67,
        "stdDevProfit": 45.23,
        "negativeProfitProbability": 0.00,
        "profitVariability": 0.16
      },
      "marginRisk": {
        "meanMargin": 23.5,
        "stdDevMargin": 3.2,
        "belowMinMarginProbability": 0.00
      },
      "riskBreakdown": {
        "fuel_price": {
          "name": "fuel_price",
          "correlation": 0.85,
          "sensitivity": 120.5
        },
        "traffic_factor": {
          "name": "traffic_factor",
          "correlation": 0.72,
          "sensitivity": 95.3
        },
        "risk_factor": {
          "name": "risk_factor",
          "correlation": 0.45,
          "sensitivity": 45.2
        }
      },
      "overallRiskLevel": "MEDIUM"
    }
  },
  "auditTrail": {
    "calculationMethod": "economic-engine-v1",
    "inputVersion": "v1.0",
    "parameters": {
      "base_price": 300,
      "per_km_price": 1.5,
      "per_kg_price": 0.04
    },
    "dataSources": [
      "SCM:PriceReference",
      "TMS:RouteInfo",
      "MarketData:FuelPrice"
    ],
    "randomSeed": "123456789",
    "executionEnvironment": {
      "javaVersion": "17",
      "os": "Linux",
      "cpuCount": 8,
      "memory": "16GB"
    }
  },
  "recommendations": [
    {
      "type": "PRICE_ADJUSTMENT",
      "value": "+5%",
      "reason": "Reduce high cost scenarios in 95th percentile",
      "confidence": 0.92,
      "impact": "Expected 15% reduction in high cost risk"
    },
    {
      "type": "FUEL_HEDGING",
      "value": "Lock in fuel prices for winter",
      "reason": "Fuel price volatility drives 85% of cost variability",
      "confidence": 0.88,
      "impact": "Expected 20% reduction in fuel cost uncertainty"
    }
  ]
}
```

### Audit Trail Schema
```json
{
  "simulationId": "SIM-2024-001",
  "auditId": "AUD-2024-001",
  "timestamp": "2024-02-06T22:45:00Z",
  "userId": "admin@nodeorb.com",
  "action": "RUN_SIMULATION",
  "description": "Winter Weather Impact Simulation",
  "parameters": {
    "iterations": 1000,
    "deterministicSeed": "123456789",
    "scenarioName": "Winter Weather Impact"
  },
  "executionDetails": {
    "startTime": "2024-02-06T22:45:00Z",
    "endTime": "2024-02-06T22:47:30Z",
    "duration": 150,
    "parallelism": 8,
    "cpuCount": 8,
    "memory": "16GB"
  },
  "resultsSummary": {
    "totalIterations": 1000,
    "successfulIterations": 985,
    "meanCost": 1250.50,
    "meanProfit": 285.67,
    "meanMargin": 23.5,
    "overallRiskLevel": "MEDIUM"
  },
  "dataSources": [
    "SCM:PriceReference",
    "TMS:RouteInfo",
    "MarketData:FuelPrice"
  ],
  "calculationMethod": "economic-engine-v1",
  "randomSeed": "123456789",
  "hash": "sha256:abc123..."
}
```

### UI Output Schema
```json
{
  "simulationId": "SIM-2024-001",
  "name": "Winter Weather Impact",
  "status": "COMPLETED",
  "duration": 150,
  "riskLevel": "MEDIUM",
  "profit": {
    "mean": 285.67,
    "stdDev": 45.23,
    "min": 150.00,
    "max": 420.00,
    "percentiles": {
      "25": 250.00,
      "50": 280.00,
      "75": 320.00,
      "95": 380.00,
      "99": 410.00
    }
  },
  "cost": {
    "mean": 1250.50,
    "stdDev": 150.75,
    "min": 980.20,
    "max": 1750.80,
    "percentiles": {
      "25": 1150.00,
      "50": 1240.00,
      "75": 1350.00,
      "95": 1550.00,
      "99": 1700.00
    }
  },
  "margin": {
    "mean": 23.5,
    "stdDev": 3.2,
    "min": 15.0,
    "max": 30.0,
    "percentiles": {
      "25": 21.0,
      "50": 23.0,
      "75": 26.0,
      "95": 29.0,
      "99": 30.0
    }
  },
  "violations": {
    "maxCost": 15,
    "minMargin": 0
  },
  "topScenarios": [
    {
      "rank": 1,
      "profit": 418.50,
      "margin": 29.8,
      "variables": {
        "fuel_price": 1.45,
        "traffic_factor": 1.15,
        "risk_factor": 0.10,
        "demand_factor": 0.18,
        "supply_factor": 0.08
      }
    }
  ],
  "riskBreakdown": [
    {
      "variable": "fuel_price",
      "correlation": 0.85,
      "sensitivity": 120.5
    },
    {
      "variable": "traffic_factor",
      "correlation": 0.72,
      "sensitivity": 95.3
    },
    {
      "variable": "risk_factor",
      "correlation": 0.45,
      "sensitivity": 45.2
    }
  ],
  "recommendations": [
    {
      "type": "PRICE_ADJUSTMENT",
      "value": "+5%",
      "reason": "Reduce high cost scenarios in 95th percentile",
      "confidence": 0.92,
      "impact": "Expected 15% reduction in high cost risk"
    }
  ]
}
```

## Configuration

### Simulation Configuration
```yaml
logi:
  autonomous:
    simulation:
      enabled: true
      defaultIterations: 1000
      minIterations: 100
      maxIterations: 10000
      # Parallel execution
      parallelism: auto
      chunkSize: 100
      # Deterministic seeding
      deterministic: true
      defaultSeed: "123456789"
      # Risk assessment
      riskLevelThresholds:
        low: 0.15
        medium: 0.35
        high: 0.60
      minMargin: 0.15
      maxCost: 2000
      # Storage
      resultRetentionDays: 30
      auditRetentionDays: 365
      # Monitoring
      metrics:
        enabled: true
        updateInterval: 30000
```

## API Contract

### Simulation Service API
```proto
// Scenario Simulation gRPC API
syntax = "proto3";

package com.autonomous.simulation;

import "google/protobuf/timestamp.proto";
import "google/protobuf/wrappers.proto";

// Scenario Simulation service definition
service ScenarioSimulationService {
    // Run scenario simulation
    rpc RunSimulation(RunSimulationRequest) returns (RunSimulationResponse);

    // Get simulation status
    rpc GetSimulationStatus(GetSimulationStatusRequest) returns (GetSimulationStatusResponse);

    // Get simulation results
    rpc GetSimulationResults(GetSimulationResultsRequest) returns (GetSimulationResultsResponse);

    // Get simulation audit trail
    rpc GetSimulationAudit(GetSimulationAuditRequest) returns (GetSimulationAuditResponse);

    // Cancel simulation
    rpc CancelSimulation(CancelSimulationRequest) returns (CancelSimulationResponse);

    // List completed simulations
    rpc ListSimulations(ListSimulationsRequest) returns (ListSimulationsResponse);
}

// Run Simulation Request
message RunSimulationRequest {
    string scenario_name = 1;
    string description = 2;
    int32 iterations = 3;
    google.protobuf.StringValue deterministic_seed = 4;
    BaseInput base_input = 5;
    map<string, ParameterDistribution> parameters = 6;
    map<string, Constraint> constraints = 7;
    SimulationOutputConfig output = 8;
}

// Base Input for simulation
message BaseInput {
    string operation_type = 1;
    string material_type = 2;
    double weight = 3;
    double volume = 4;
    double distance = 5;
    double time = 6;
    string urgency = 7;
    RouteInfo route_info = 8;
    FuelInfo fuel_info = 9;
    SupplyDemandInfo supply_demand = 10;
    repeated SpecialRequirement special_requirements = 11;
    google.protobuf.Timestamp timestamp = 12;
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

// Parameter Distribution
message ParameterDistribution {
    double min = 1;
    double max = 2;
    string distribution = 3;
    google.protobuf.DoubleValue std_dev = 4;
    google.protobuf.DoubleValue mean = 5;
}

// Constraint
message Constraint {
    string operator = 1;
    double value = 2;
}

// Simulation Output Configuration
message SimulationOutputConfig {
    repeated string metrics = 1;
    double confidence_level = 2;
    int32 top_scenarios = 3;
}

// Run Simulation Response
message RunSimulationResponse {
    string simulation_id = 1;
    string status = 2;
    google.protobuf.Timestamp start_time = 3;
    string message = 4;
}

// Get Simulation Status Request
message GetSimulationStatusRequest {
    string simulation_id = 1;
}

// Get Simulation Status Response
message GetSimulationStatusResponse {
    string simulation_id = 1;
    string status = 2;
    google.protobuf.Timestamp start_time = 3;
    google.protobuf.Timestamp end_time = 4;
    int32 completed_iterations = 5;
    int32 total_iterations = 6;
    string message = 7;
}

// Get Simulation Results Request
message GetSimulationResultsRequest {
    string simulation_id = 1;
    google.protobuf.BoolValue include_detailed = 2;
    google.protobuf.Int32Value limit = 3;
}

// Get Simulation Results Response
message GetSimulationResultsResponse {
    string simulation_id = 1;
    string status = 2;
    AggregatedResults results = 3;
    repeated DetailedResult detailed_results = 4;
}

// Aggregated Results
message AggregatedResults {
    int32 total_iterations = 1;
    int32 successful_iterations = 2;
    MetricStatistics cost_metrics = 3;
    MetricStatistics profit_metrics = 4;
    MetricStatistics margin_metrics = 5;
    map<string, int32> constraint_violations = 6;
    repeated TopScenario top_scenarios = 7;
    RiskAssessment risk_assessment = 8;
}

// Metric Statistics
message MetricStatistics {
    double mean = 1;
    double std_dev = 2;
    double min = 3;
    double max = 4;
    map<string, double> percentiles = 5;
}

// Top Scenario
message TopScenario {
    int32 rank = 1;
    int32 iteration_id = 2;
    double profit = 3;
    double margin = 4;
    map<string, double> variable_values = 5;
}

// Risk Assessment
message RiskAssessment {
    CostRisk cost_risk = 1;
    ProfitRisk profit_risk = 2;
    MarginRisk margin_risk = 3;
    map<string, VariableRisk> risk_breakdown = 4;
    string overall_risk_level = 5;
}

// Cost Risk
message CostRisk {
    double mean_cost = 1;
    double std_dev_cost = 2;
    double percentile95_cost = 3;
    double percentile99_cost = 4;
    double cost_variability = 5;
}

// Profit Risk
message ProfitRisk {
    double mean_profit = 1;
    double std_dev_profit = 2;
    double negative_profit_probability = 3;
    double profit_variability = 4;
}

// Margin Risk
message MarginRisk {
    double mean_margin = 1;
    double std_dev_margin = 2;
    double below_min_margin_probability = 3;
}

// Variable Risk
message VariableRisk {
    string name = 1;
    double correlation = 2;
    double sensitivity = 3;
}

// Detailed Result
message DetailedResult {
    int32 iteration_id = 1;
    CostBreakdown cost_breakdown = 2;
    map<string, double> variable_values = 3;
    bool constraints_satisfied = 4;
    map<string, bool> constraint_violations = 5;
}

// Cost Breakdown
message CostBreakdown {
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
}

// Get Simulation Audit Request
message GetSimulationAuditRequest {
    string simulation_id = 1;
}

// Get Simulation Audit Response
message GetSimulationAuditResponse {
    string audit_id = 1;
    string simulation_id = 2;
    google.protobuf.Timestamp timestamp = 3;
    string user_id = 4;
    string action = 5;
    string description = 6;
    AuditExecutionDetails execution_details = 7;
    AuditResultsSummary results_summary = 8;
    repeated string data_sources = 9;
    string calculation_method = 10;
    string random_seed = 11;
    string hash = 12;
}

// Audit Execution Details
message AuditExecutionDetails {
    google.protobuf.Timestamp start_time = 1;
    google.protobuf.Timestamp end_time = 2;
    int32 duration = 3;
    int32 parallelism = 4;
    int32 cpu_count = 5;
    string memory = 6;
}

// Audit Results Summary
message AuditResultsSummary {
    int32 total_iterations = 1;
    int32 successful_iterations = 2;
    double mean_cost = 3;
    double mean_profit = 4;
    double mean_margin = 5;
    string overall_risk_level = 6;
}

// Cancel Simulation Request
message CancelSimulationRequest {
    string simulation_id = 1;
}

// Cancel Simulation Response
message CancelSimulationResponse {
    bool success = 1;
    string message = 2;
}

// List Simulations Request
message ListSimulationsRequest {
    google.protobuf.Timestamp start_time = 1;
    google.protobuf.Timestamp end_time = 2;
    repeated string statuses = 3;
    google.protobuf.Int32Value limit = 4;
    google.protobuf.Int32Value offset = 5;
}

// List Simulations Response
message ListSimulationsResponse {
    int32 total_count = 1;
    repeated SimulationSummary simulations = 2;
}

// Simulation Summary
message SimulationSummary {
    string simulation_id = 1;
    string scenario_name = 2;
    string description = 3;
    string status = 4;
    google.protobuf.Timestamp start_time = 5;
    google.protobuf.Timestamp end_time = 6;
    int32 iterations = 7;
    string overall_risk_level = 8;
    double mean_profit = 9;
    double mean_cost = 10;
}
```

## REST API Endpoints

### Run Simulation
```
POST /api/v1/simulation/run
Request: RunSimulationRequest
Response: RunSimulationResponse
```

### Get Simulation Status
```
GET /api/v1/simulation/{id}/status
Response: GetSimulationStatusResponse
```

### Get Simulation Results
```
GET /api/v1/simulation/{id}/results
Query Parameters:
- include_detailed: boolean (default: false)
- limit: integer (default: 100)
Response: GetSimulationResultsResponse
```

### Get Simulation Audit
```
GET /api/v1/simulation/{id}/audit
Response: GetSimulationAuditResponse
```

### Cancel Simulation
```
POST /api/v1/simulation/{id}/cancel
Response: CancelSimulationResponse
```

### List Simulations
```
GET /api/v1/simulation/list
Query Parameters:
- start_time: ISO8601 date (optional)
- end_time: ISO8601 date (optional)
- statuses: comma-separated string (optional)
- limit: integer (default: 100)
- offset: integer (default: 0)
Response: ListSimulationsResponse
```

## Metrics

### Prometheus Metrics
```
# Simulation Metrics
autonomous_ops_simulation_runs_total
autonomous_ops_simulation_iterations_total
autonomous_ops_simulation_successful_iterations_total
autonomous_ops_simulation_failed_iterations_total
autonomous_ops_simulation_duration_seconds
autonomous_ops_simulation_parallelism
autonomous_ops_simulation_memory_usage_bytes
autonomous_ops_simulation_cpu_usage_percent
autonomous_ops_simulation_top_scenario_profit_avg
autonomous_ops_simulation_risk_level_distribution
```

## Logging

### Log Levels
```
DEBUG: Detailed execution information, iteration results
INFO: Simulation start/end, summary results
WARN: Degraded performance, high iteration failure rate
ERROR: Execution failures, invalid inputs
```

### Log Fields
```json
{
  "timestamp": "2024-02-06T22:45:00Z",
  "level": "INFO",
  "simulationId": "SIM-2024-001",
  "scenarioName": "Winter Weather Impact",
  "status": "COMPLETED",
  "iterations": 1000,
  "successfulIterations": 985,
  "duration": 150,
  "meanCost": 1250.50,
  "meanProfit": 285.67,
  "meanMargin": 23.5,
  "overallRiskLevel": "MEDIUM",
  "parallelism": 8
}
```

## Implementation Considerations

### Performance Optimization
1. **Parallel Execution** - Use Kotlin coroutines with Dispatchers.IO for parallel processing
2. **Chunking** - Process iterations in chunks to manage memory usage
3. **Thread Pool Management** - Configure thread pool based on available CPU cores
4. **Result Streaming** - Stream results to avoid loading all iterations into memory

### Storage
1. **Aggregated Results** - Store in PostgreSQL for querying and visualization
2. **Detailed Results** - Store in ClickHouse for fast analytics
3. **Audit Trails** - Store in WORM (Write Once Read Many) storage for compliance
4. **Cache** - Redis for frequently accessed results

### Error Handling
1. **Input Validation** - Validate all inputs before execution
2. **Iteration Recovery** - Continue execution if some iterations fail
3. **Resource Management** - Clean up resources on cancellation
4. **Retry Logic** - Retry failed iterations with backoff

## Usage Example

### Simple Scenario
```json
{
  "scenarioName": "Fuel Price Impact",
  "description": "Simulation of fuel price impact on profitability",
  "iterations": 1000,
  "deterministicSeed": "123456789",
  "baseInput": {
    "operationType": "TRANSPORTATION",
    "materialType": "GENERAL",
    "weight": 5000,
    "volume": 15,
    "distance": 250,
    "time": 5,
    "urgency": "NORMAL",
    "routeInfo": {
      "trafficFactor": 1.1,
      "roadFactor": 1.0,
      "riskFactor": 0.15
    },
    "fuelInfo": {
      "price": 1.5,
      "consumption": 0.3
    },
    "supplyDemand": {
      "demandFactor": 0.2,
      "supplyFactor": 0.1
    },
    "specialRequirements": [
      {
        "type": "TEMPERATURE_CONTROL",
        "quantity": 1,
        "cost": 100
      }
    ]
  },
  "parameters": {
    "fuel_price": {
      "min": 1.2,
      "max": 1.8,
      "distribution": "NORMAL",
      "stdDev": 0.15,
      "mean": 1.5
    },
    "traffic_factor": {
      "min": 1.0,
      "max": 1.5,
      "distribution": "LOGNORMAL",
      "mean": 1.2
    },
    "risk_factor": {
      "min": 0.1,
      "max": 0.2,
      "distribution": "UNIFORM"
    }
  },
  "constraints": {
    "maxCost": 1500,
    "minMargin": 20
  },
  "output": {
    "metrics": ["profit", "margin", "cost"],
    "confidenceLevel": 0.95,
    "topScenarios": 10
  }
}
```

## Conclusion
Scenario Simulation subsystem provides comprehensive Monte Carlo-based simulation of logistics operations with probabilistic risk assessment, weather and traffic uncertainty modeling, and deterministic seeding for audit purposes. The structured outputs support both UI visualization and compliance audit trails.