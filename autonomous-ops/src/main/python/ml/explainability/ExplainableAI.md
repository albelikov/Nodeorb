# Explainable AI (XAI) Layer

## Overview
Explainable AI layer provides human-readable reasoning for decisions made by the Economic Engine, along with machine-verifiable structure suitable for audit and blockchain logging. It bridges the gap between complex machine learning models and human understanding.

## Design Principles
1. **Human Readability** - Decisions explained in plain business language
2. **Machine Verifiability** - Structured JSON format with semantic meaning
3. **Audit Readiness** - Complete traceability from input to decision
4. **Blockchain Compatibility** - Hash-verifiable and immutable structure
5. **Decision Tree Mapping** - Visual representation of decision logic
6. **Cost Transparency** - Clear mapping between decisions and cost changes

## Architecture

### XAI Pipeline
```
┌─────────────────────────────────────────────────────────────────┐
│                     XAI Explanation Pipeline                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Decision   │  │   Feature    │  │   Cost       │          │
│  │  Capture    │  │  Importance  │  │  Attribution │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
│         │                 │                 │                   │
│         └────────────┬────┴─────────────────┘                   │
│                      ▼                                           │
│              ┌──────────────┐                                    │
│              │   Reasoning  │                                    │
│              │  Generation  │                                    │
│              └──────┬───────┘                                    │
│                     │                                            │
│         ┌───────────┴───────────┐                               │
│         ▼                       ▼                               │
│  ┌──────────────┐      ┌──────────────┐                          │
│  │   Decision   │      │   Cost       │                          │
│  │  Tree Map   │      │  Delta Map   │                          │
│  └──────┬───────┘      └──────┬───────┘                          │
│         │                     │                                  │
│         └──────────┬──────────┘                                  │
│                    ▼                                             │
│            ┌──────────────┐                                       │
│            │   Human      │                                       │
│            │  Explanation │                                       │
│            └──────┬───────┘                                       │
│                   │                                               │
│         ┌──────────┴──────────┐                                  │
│         ▼                      ▼                                 │
│  ┌──────────────┐       ┌──────────────┐                        │
│  │   Machine    │       │   Blockchain │                        │
│  │  Verifiable  │       │  Log Entry   │                        │
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

## Explanation Schema

### Complete Explanation Structure
```json
{
  "explanationId": "XAI-2024-001",
  "decisionId": "OP-2024-001",
  "timestamp": "2024-02-06T22:45:00Z",
  "decisionType": "PRICE_CALCULATION",
  "status": "APPROVED",
  "calculationMethod": "economic-engine-v1",
  "traceability": {
    "inputVersion": "v1.0",
    "parameters": {
      "base_price": 300,
      "per_km_price": 1.5,
      "per_kg_price": 0.04,
      "fuel_price": 1.45
    },
    "dataSources": [
      "SCM:PriceReference",
      "TMS:RouteInfo",
      "MarketData:FuelPrice"
    ]
  },
  "reasoning": {
    "summary": "Price calculation approved based on standard rates and market conditions",
    "detailedSteps": [
      {
        "step": 1,
        "description": "Validated base price for GENERAL material type",
        "result": "Base price 300 USD within normal range",
        "confidence": 0.98,
        "dataSource": "SCM:PriceReference"
      },
      {
        "step": 2,
        "description": "Calculated distance cost using per-km rate",
        "result": "Distance cost: 375 USD (250 km × 1.5 USD/km)",
        "confidence": 0.95,
        "dataSource": "TMS:RouteInfo"
      },
      {
        "step": 3,
        "description": "Calculated fuel cost based on current price",
        "result": "Fuel cost: 110.63 USD (1.45 USD/l × 250 km × 0.3 l/km)",
        "confidence": 0.92,
        "dataSource": "MarketData:FuelPrice"
      },
      {
        "step": 4,
        "description": "Applied demand-supply adjustment",
        "result": "Price adjusted by +10% (demand: 0.2, supply: 0.1)",
        "confidence": 0.88,
        "dataSource": "MarketData:DemandSupply"
      },
      {
        "step": 5,
        "description": "Verified minimum margin requirement",
        "result": "Calculated margin: 24.5%, which meets minimum 15% requirement",
        "confidence": 0.96,
        "dataSource": "EconomicEngine:MarginCheck"
      }
    ],
    "keyFactors": [
      {
        "factor": "Fuel Price",
        "importance": 0.45,
        "contribution": "Positive impact on cost"
      },
      {
        "factor": "Distance",
        "importance": 0.30,
        "contribution": "Positive impact on cost"
      },
      {
        "factor": "Demand-Supply",
        "importance": 0.15,
        "contribution": "Positive impact on price"
      },
      {
        "factor": "Base Price",
        "importance": 0.10,
        "contribution": "Positive impact on cost"
      }
    ]
  },
  "decisionTree": {
    "root": {
      "name": "Price Calculation Decision",
      "type": "ROOT",
      "result": "APPROVED",
      "confidence": 0.94,
      "children": [
        {
          "name": "Input Validation",
          "type": "VALIDATION",
          "result": "PASS",
          "confidence": 0.99,
          "children": [
            {
              "name": "Base Price Validation",
              "type": "PRICE_CHECK",
              "result": "PASS",
              "confidence": 0.98,
              "value": "300 USD"
            },
            {
              "name": "Distance Validation",
              "type": "RANGE_CHECK",
              "result": "PASS",
              "confidence": 0.99,
              "value": "250 km"
            }
          ]
        },
        {
          "name": "Cost Calculation",
          "type": "CALCULATION",
          "result": "VALID",
          "confidence": 0.96,
          "children": [
            {
              "name": "Base Cost",
              "type": "LINEAR",
              "result": "300 USD",
              "confidence": 0.98
            },
            {
              "name": "Distance Cost",
              "type": "LINEAR",
              "result": "375 USD",
              "confidence": 0.95
            },
            {
              "name": "Fuel Cost",
              "type": "COMPLEX",
              "result": "110.63 USD",
              "confidence": 0.92
            }
          ]
        },
        {
          "name": "Margin Check",
          "type": "THRESHOLD",
          "result": "PASS",
          "confidence": 0.96,
          "threshold": "15%",
          "actual": "24.5%"
        }
      ]
    }
  },
  "costDeltas": [
    {
      "component": "Base Cost",
      "calculated": 300.00,
      "base": 300.00,
      "delta": 0.00,
      "deltaPercent": 0.00,
      "reason": "Standard rate for GENERAL material"
    },
    {
      "component": "Distance Cost",
      "calculated": 375.00,
      "base": 375.00,
      "delta": 0.00,
      "deltaPercent": 0.00,
      "reason": "Per-km rate applied correctly"
    },
    {
      "component": "Fuel Cost",
      "calculated": 110.63,
      "base": 112.50,
      "delta": -1.87,
      "deltaPercent": -1.66,
      "reason": "Fuel price lower than expected (1.45 vs 1.50 USD/l)"
    },
    {
      "component": "Dynamic Price Adjustment",
      "calculated": 110.00,
      "base": 0.00,
      "delta": 110.00,
      "deltaPercent": 10.00,
      "reason": "Demand-supply adjustment (+10%)"
    }
  ],
  "riskAssessment": {
    "overallRiskLevel": "MEDIUM",
    "risks": [
      {
        "risk": "Fuel Price Volatility",
        "probability": 0.35,
        "impact": 0.65,
        "severity": "MEDIUM",
        "mitigation": "Fuel price hedging strategy"
      },
      {
        "risk": "Traffic Delays",
        "probability": 0.25,
        "impact": 0.45,
        "severity": "LOW",
        "mitigation": "Alternative route planning"
      }
    ]
  },
  "auditTrail": {
    "userId": "admin@nodeorb.com",
    "calculationId": "CALC-2024-001",
    "executionEnvironment": {
      "javaVersion": "17",
      "os": "Linux",
      "cpuCount": 8,
      "memory": "16GB"
    },
    "version": "1.0",
    "hash": "sha256:abc123..."
  },
  "blockchainMetadata": {
    "transactionId": "tx_1234567890abcdef",
    "blockNumber": 1054321,
    "timestamp": "2024-02-06T22:45:00Z",
    "hash": "0x1a2b3c4d5e6f7890abcdef1234567890abcdef1234567890abcdef1234567890",
    "validator": "node-01",
    "signature": "0x9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"
  }
}
```

### Rejection Case Example
```json
{
  "explanationId": "XAI-2024-002",
  "decisionId": "OP-2024-002",
  "timestamp": "2024-02-06T22:45:00Z",
  "decisionType": "PRICE_CALCULATION",
  "status": "REJECTED",
  "calculationMethod": "economic-engine-v1",
  "traceability": {
    "inputVersion": "v1.0",
    "parameters": {
      "base_price": 300,
      "per_km_price": 1.5,
      "per_kg_price": 0.04,
      "fuel_price": 2.10
    },
    "dataSources": [
      "SCM:PriceReference",
      "TMS:RouteInfo",
      "MarketData:FuelPrice"
    ]
  },
  "reasoning": {
    "summary": "Price calculation rejected due to insufficient margin",
    "detailedSteps": [
      {
        "step": 1,
        "description": "Validated base price for GENERAL material type",
        "result": "Base price 300 USD within normal range",
        "confidence": 0.98,
        "dataSource": "SCM:PriceReference"
      },
      {
        "step": 2,
        "description": "Calculated distance cost using per-km rate",
        "result": "Distance cost: 375 USD (250 km × 1.5 USD/km)",
        "confidence": 0.95,
        "dataSource": "TMS:RouteInfo"
      },
      {
        "step": 3,
        "description": "Calculated fuel cost based on current price",
        "result": "Fuel cost: 157.50 USD (2.10 USD/l × 250 km × 0.3 l/km)",
        "confidence": 0.92,
        "dataSource": "MarketData:FuelPrice"
      },
      {
        "step": 4,
        "description": "Applied demand-supply adjustment",
        "result": "Price adjusted by +10% (demand: 0.2, supply: 0.1)",
        "confidence": 0.88,
        "dataSource": "MarketData:DemandSupply"
      },
      {
        "step": 5,
        "description": "Verified minimum margin requirement",
        "result": "Calculated margin: 12.3%, which is below minimum 15% requirement",
        "confidence": 0.96,
        "dataSource": "EconomicEngine:MarginCheck"
      }
    ],
    "keyFactors": [
      {
        "factor": "Fuel Price",
        "importance": 0.60,
        "contribution": "Negative impact on margin"
      },
      {
        "factor": "Distance",
        "importance": 0.25,
        "contribution": "Positive impact on cost"
      },
      {
        "factor": "Base Price",
        "importance": 0.10,
        "contribution": "Positive impact on cost"
      },
      {
        "factor": "Demand-Supply",
        "importance": 0.05,
        "contribution": "Positive impact on price"
      }
    ]
  },
  "decisionTree": {
    "root": {
      "name": "Price Calculation Decision",
      "type": "ROOT",
      "result": "REJECTED",
      "confidence": 0.92,
      "children": [
        {
          "name": "Input Validation",
          "type": "VALIDATION",
          "result": "PASS",
          "confidence": 0.99,
          "children": [
            {
              "name": "Base Price Validation",
              "type": "PRICE_CHECK",
              "result": "PASS",
              "confidence": 0.98,
              "value": "300 USD"
            },
            {
              "name": "Distance Validation",
              "type": "RANGE_CHECK",
              "result": "PASS",
              "confidence": 0.99,
              "value": "250 km"
            }
          ]
        },
        {
          "name": "Cost Calculation",
          "type": "CALCULATION",
          "result": "VALID",
          "confidence": 0.96,
          "children": [
            {
              "name": "Base Cost",
              "type": "LINEAR",
              "result": "300 USD",
              "confidence": 0.98
            },
            {
              "name": "Distance Cost",
              "type": "LINEAR",
              "result": "375 USD",
              "confidence": 0.95
            },
            {
              "name": "Fuel Cost",
              "type": "COMPLEX",
              "result": "157.50 USD",
              "confidence": 0.92
            }
          ]
        },
        {
          "name": "Margin Check",
          "type": "THRESHOLD",
          "result": "FAIL",
          "confidence": 0.96,
          "threshold": "15%",
          "actual": "12.3%"
        }
      ]
    }
  },
  "costDeltas": [
    {
      "component": "Base Cost",
      "calculated": 300.00,
      "base": 300.00,
      "delta": 0.00,
      "deltaPercent": 0.00,
      "reason": "Standard rate for GENERAL material"
    },
    {
      "component": "Distance Cost",
      "calculated": 375.00,
      "base": 375.00,
      "delta": 0.00,
      "deltaPercent": 0.00,
      "reason": "Per-km rate applied correctly"
    },
    {
      "component": "Fuel Cost",
      "calculated": 157.50,
      "base": 112.50,
      "delta": 45.00,
      "deltaPercent": 40.00,
      "reason": "Fuel price higher than expected (2.10 vs 1.50 USD/l)"
    },
    {
      "component": "Dynamic Price Adjustment",
      "calculated": 109.25,
      "base": 0.00,
      "delta": 109.25,
      "deltaPercent": 10.00,
      "reason": "Demand-supply adjustment (+10%)"
    }
  ],
  "riskAssessment": {
    "overallRiskLevel": "HIGH",
    "risks": [
      {
        "risk": "Fuel Price Volatility",
        "probability": 0.65,
        "impact": 0.85,
        "severity": "HIGH",
        "mitigation": "Fuel price hedging strategy"
      },
      {
        "risk": "Margin Violation",
        "probability": 1.00,
        "impact": 0.75,
        "severity": "CRITICAL",
        "mitigation": "Price adjustment or cost reduction"
      }
    ]
  },
  "auditTrail": {
    "userId": "admin@nodeorb.com",
    "calculationId": "CALC-2024-002",
    "executionEnvironment": {
      "javaVersion": "17",
      "os": "Linux",
      "cpuCount": 8,
      "memory": "16GB"
    },
    "version": "1.0",
    "hash": "sha256:def456..."
  },
  "blockchainMetadata": {
    "transactionId": "tx_0987654321fedcba",
    "blockNumber": 1054322,
    "timestamp": "2024-02-06T22:45:00Z",
    "hash": "0x0f9e8d7c6b5a4938271605f4e3d2c1b0a0b1c2d3e4f567890abcdef123456",
    "validator": "node-02",
    "signature": "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890"
  }
}
```

## Decision Tree to Cost Delta Mapping

### Decision Tree Structure
```
Root Decision
├── Input Validation (PASS)
│   ├── Base Price Validation (PASS: 300 USD)
│   └── Distance Validation (PASS: 250 km)
├── Cost Calculation (VALID)
│   ├── Base Cost (300 USD)
│   │   └── Cost Delta: 0.00 USD (0%)
│   ├── Distance Cost (375 USD)
│   │   └── Cost Delta: 0.00 USD (0%)
│   └── Fuel Cost (110.63 USD)
│       └── Cost Delta: -1.87 USD (-1.66%)
└── Margin Check (PASS: 24.5% ≥ 15%)
    └── Dynamic Price Adjustment (110.00 USD)
        └── Cost Delta: +110.00 USD (+10.00%)
```

### Mapping Rules
1. **Validation Nodes** → No cost impact, only verification status
2. **Calculation Nodes** → Direct cost delta from formula application
3. **Threshold Nodes** → Cost delta from adjustment rules
4. **Complex Nodes** → Aggregated cost delta from child nodes

## Implementation Architecture

### XAI Components

#### Explanation Generator
```kotlin
class ExplanationGenerator(private val economicEngine: EconomicEngine) {
    fun generateExplanation(decision: EconomicDecision): XAIExplanation {
        return XAIExplanation(
            explanationId = "XAI-${decision.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))}",
            decisionId = decision.decisionId,
            timestamp = decision.timestamp,
            decisionType = decision.decisionType,
            status = decision.status,
            calculationMethod = decision.calculationMethod,
            traceability = generateTraceability(decision),
            reasoning = generateReasoning(decision),
            decisionTree = generateDecisionTree(decision),
            costDeltas = generateCostDeltas(decision),
            riskAssessment = generateRiskAssessment(decision),
            auditTrail = generateAuditTrail(decision),
            blockchainMetadata = generateBlockchainMetadata(decision)
        )
    }
    
    private fun generateTraceability(decision: EconomicDecision): Traceability {
        return Traceability(
            inputVersion = decision.inputVersion,
            parameters = decision.calculationParameters,
            dataSources = decision.dataSources
        )
    }
    
    private fun generateReasoning(decision: EconomicDecision): Reasoning {
        val steps = mutableListOf<ReasoningStep>()
        
        decision.calculationSteps.forEachIndexed { index, step ->
            steps.add(
                ReasoningStep(
                    step = index + 1,
                    description = step.description,
                    result = step.result,
                    confidence = step.confidence,
                    dataSource = step.dataSource
                )
            )
        }
        
        return Reasoning(
            summary = decision.decisionSummary,
            detailedSteps = steps,
            keyFactors = decision.keyFactors.map {
                KeyFactor(
                    factor = it.name,
                    importance = it.importance,
                    contribution = it.contribution
                )
            }
        )
    }
    
    private fun generateDecisionTree(decision: EconomicDecision): DecisionTree {
        return DecisionTree(
            root = generateDecisionNode(decision.decisionRoot)
        )
    }
    
    private fun generateDecisionNode(node: DecisionNode): DecisionTreeNode {
        return DecisionTreeNode(
            name = node.name,
            type = node.type,
            result = node.result,
            confidence = node.confidence,
            threshold = node.threshold,
            actual = node.actual,
            value = node.value,
            children = node.children.map { generateDecisionNode(it) }
        )
    }
    
    private fun generateCostDeltas(decision: EconomicDecision): List<CostDelta> {
        return decision.costComponents.map {
            CostDelta(
                component = it.component,
                calculated = it.calculated,
                base = it.base,
                delta = it.calculated - it.base,
                deltaPercent = ((it.calculated - it.base) / it.base) * 100,
                reason = it.reason
            )
        }
    }
    
    private fun generateRiskAssessment(decision: EconomicDecision): RiskAssessment {
        return RiskAssessment(
            overallRiskLevel = decision.overallRiskLevel,
            risks = decision.risks.map {
                Risk(
                    risk = it.name,
                    probability = it.probability,
                    impact = it.impact,
                    severity = it.severity,
                    mitigation = it.mitigation
                )
            }
        )
    }
    
    private fun generateAuditTrail(decision: EconomicDecision): AuditTrail {
        return AuditTrail(
            userId = decision.userId,
            calculationId = decision.calculationId,
            executionEnvironment = decision.executionEnvironment,
            version = decision.version,
            hash = decision.hash
        )
    }
    
    private fun generateBlockchainMetadata(decision: EconomicDecision): BlockchainMetadata {
        return BlockchainMetadata(
            transactionId = decision.blockchainTransactionId,
            blockNumber = decision.blockchainBlockNumber,
            timestamp = decision.timestamp,
            hash = decision.blockchainHash,
            validator = decision.blockchainValidator,
            signature = decision.blockchainSignature
        )
    }
}
```

#### Decision Tree Visualizer
```kotlin
class DecisionTreeVisualizer {
    fun toGraphviz(node: DecisionTreeNode, indent: Int = 0): String {
        val sb = StringBuilder()
        
        if (indent == 0) {
            sb.append("digraph DecisionTree {\n")
            sb.append("  rankdir=TB;\n")
            sb.append("  node [shape=box, style=filled, color=lightblue];\n")
        }
        
        val nodeId = "node_${node.hashCode()}"
        val label = escapeLabel(node.displayText)
        
        sb.append("  $nodeId [label=\"$label\"];\n")
        
        node.children.forEach { child ->
            val childId = "node_${child.hashCode()}"
            val childLabel = escapeLabel(child.displayText)
            
            sb.append("  $childId [label=\"$childLabel\"];\n")
            sb.append("  $nodeId -> $childId;\n")
            
            sb.append(toGraphviz(child, indent + 1))
        }
        
        if (indent == 0) {
            sb.append("}\n")
        }
        
        return sb.toString()
    }
    
    private fun escapeLabel(text: String): String {
        return text.replace("\"", "\\\"").replace("\n", "\\n")
    }
    
    fun toMermaid(node: DecisionTreeNode): String {
        val sb = StringBuilder()
        
        if (node.isRoot) {
            sb.append("graph TD\n")
        }
        
        val nodeId = "node_${node.hashCode()}"
        sb.append("  $nodeId[${node.displayText}]\n")
        
        node.children.forEach { child ->
            val childId = "node_${child.hashCode()}"
            sb.append("  $nodeId --> $childId[${child.displayText}]\n")
            sb.append(toMermaid(child))
        }
        
        return sb.toString()
    }
    
    fun toText(node: DecisionTreeNode, indent: Int = 0): String {
        val sb = StringBuilder()
        val prefix = "  ".repeat(indent)
        
        sb.append("${prefix}${node.displayText}\n")
        
        node.children.forEach { child ->
            sb.append(toText(child, indent + 1))
        }
        
        return sb.toString()
    }
}
```

#### Blockchain Integration
```kotlin
class BlockchainLogger(private val blockchainClient: BlockchainClient) {
    suspend fun logExplanation(explanation: XAIExplanation): BlockchainMetadata {
        val logEntry = BlockchainLogEntry(
            explanationId = explanation.explanationId,
            decisionId = explanation.decisionId,
            timestamp = explanation.timestamp,
            decisionType = explanation.decisionType,
            status = explanation.status,
            calculationMethod = explanation.calculationMethod,
            traceability = explanation.traceability,
            reasoning = explanation.reasoning,
            decisionTree = explanation.decisionTree,
            costDeltas = explanation.costDeltas,
            riskAssessment = explanation.riskAssessment,
            auditTrail = explanation.auditTrail,
            hash = calculateExplanationHash(explanation)
        )
        
        return blockchainClient.addLogEntry(logEntry)
    }
    
    private fun calculateExplanationHash(explanation: XAIExplanation): String {
        val serialized = ObjectMapper().writeValueAsString(explanation)
        return DigestUtils.sha256Hex(serialized)
    }
    
    suspend fun verifyExplanation(explanationId: String): Boolean {
        val logEntry = blockchainClient.getLogEntry(explanationId)
        val currentExplanation = getCurrentExplanation(explanationId)
        
        val logHash = logEntry.hash
        val currentHash = calculateExplanationHash(currentExplanation)
        
        return logHash == currentHash
    }
    
    private fun getCurrentExplanation(explanationId: String): XAIExplanation {
        // Fetch from database
        return database.explanations.findByExplanationId(explanationId)
    }
}
```

## API Contract

### XAI Service API
```proto
// Explainable AI gRPC API
syntax = "proto3";

package com.autonomous.xai;

import "google/protobuf/timestamp.proto";

// Explainable AI service definition
service ExplainableAIService {
    // Get explanation for decision
    rpc GetExplanation(GetExplanationRequest) returns (GetExplanationResponse);

    // Get decision tree visualization
    rpc GetDecisionTree(GetDecisionTreeRequest) returns (GetDecisionTreeResponse);

    // Get cost delta mapping
    rpc GetCostDeltas(GetCostDeltasRequest) returns (GetCostDeltasResponse);

    // Verify explanation
    rpc VerifyExplanation(VerifyExplanationRequest) returns (VerifyExplanationResponse);

    // Get explanation history
    rpc GetExplanationHistory(GetExplanationHistoryRequest) returns (GetExplanationHistoryResponse);
}

// Get Explanation Request
message GetExplanationRequest {
    string explanation_id = 1;
    string decision_id = 2;
    bool include_details = 3;
}

// Get Explanation Response
message GetExplanationResponse {
    string explanation_id = 1;
    string decision_id = 2;
    google.protobuf.Timestamp timestamp = 3;
    string decision_type = 4;
    string status = 5;
    string calculation_method = 6;
    Traceability traceability = 7;
    Reasoning reasoning = 8;
    DecisionTree decision_tree = 9;
    repeated CostDelta cost_deltas = 10;
    RiskAssessment risk_assessment = 11;
    AuditTrail audit_trail = 12;
    BlockchainMetadata blockchain_metadata = 13;
}

// Traceability
message Traceability {
    string input_version = 1;
    map<string, string> parameters = 2;
    repeated string data_sources = 3;
}

// Reasoning
message Reasoning {
    string summary = 1;
    repeated ReasoningStep detailed_steps = 2;
    repeated KeyFactor key_factors = 3;
}

// Reasoning Step
message ReasoningStep {
    int32 step = 1;
    string description = 2;
    string result = 3;
    double confidence = 4;
    string data_source = 5;
}

// Key Factor
message KeyFactor {
    string factor = 1;
    double importance = 2;
    string contribution = 3;
}

// Decision Tree
message DecisionTree {
    DecisionTreeNode root = 1;
}

// Decision Tree Node
message DecisionTreeNode {
    string name = 1;
    string type = 2;
    string result = 3;
    double confidence = 4;
    string threshold = 5;
    string actual = 6;
    string value = 7;
    repeated DecisionTreeNode children = 8;
}

// Cost Delta
message CostDelta {
    string component = 1;
    double calculated = 2;
    double base = 3;
    double delta = 4;
    double delta_percent = 5;
    string reason = 6;
}

// Risk Assessment
message RiskAssessment {
    string overall_risk_level = 1;
    repeated Risk risks = 2;
}

// Risk
message Risk {
    string risk = 1;
    double probability = 2;
    double impact = 3;
    string severity = 4;
    string mitigation = 5;
}

// Audit Trail
message AuditTrail {
    string user_id = 1;
    string calculation_id = 2;
    ExecutionEnvironment execution_environment = 3;
    string version = 4;
    string hash = 5;
}

// Execution Environment
message ExecutionEnvironment {
    string java_version = 1;
    string os = 2;
    int32 cpu_count = 3;
    string memory = 4;
}

// Blockchain Metadata
message BlockchainMetadata {
    string transaction_id = 1;
    int32 block_number = 2;
    google.protobuf.Timestamp timestamp = 3;
    string hash = 4;
    string validator = 5;
    string signature = 6;
}

// Get Decision Tree Request
message GetDecisionTreeRequest {
    string explanation_id = 1;
    string format = 2;  // "graphviz" or "mermaid" or "text"
}

// Get Decision Tree Response
message GetDecisionTreeResponse {
    string explanation_id = 1;
    string format = 2;
    string content = 3;
}

// Get Cost Deltas Request
message GetCostDeltasRequest {
    string explanation_id = 1;
    string decision_id = 2;
}

// Get Cost Deltas Response
message GetCostDeltasResponse {
    string explanation_id = 1;
    string decision_id = 2;
    repeated CostDelta cost_deltas = 3;
}

// Verify Explanation Request
message VerifyExplanationRequest {
    string explanation_id = 1;
}

// Verify Explanation Response
message VerifyExplanationResponse {
    bool valid = 1;
    string message = 2;
    string blockchain_hash = 3;
    string calculated_hash = 4;
    bool match = 5;
}

// Get Explanation History Request
message GetExplanationHistoryRequest {
    google.protobuf.Timestamp start_time = 1;
    google.protobuf.Timestamp end_time = 2;
    repeated string decision_types = 3;
    repeated string statuses = 4;
    int32 limit = 5;
    int32 offset = 6;
}

// Get Explanation History Response
message GetExplanationHistoryResponse {
    int32 total_count = 1;
    repeated ExplanationSummary explanations = 2;
}

// Explanation Summary
message ExplanationSummary {
    string explanation_id = 1;
    string decision_id = 2;
    google.protobuf.Timestamp timestamp = 3;
    string decision_type = 4;
    string status = 5;
    string calculation_method = 6;
    string summary = 7;
    string overall_risk_level = 8;
}
```

## REST API Endpoints

### Get Explanation
```
GET /api/v1/xai/explanation
Query Parameters:
- explanation_id: string (required if decision_id not provided)
- decision_id: string (required if explanation_id not provided)
- include_details: boolean (default: true)
Response: GetExplanationResponse
```

### Get Decision Tree
```
GET /api/v1/xai/decision-tree
Query Parameters:
- explanation_id: string (required)
- format: string (optional, default: "mermaid")
Response: GetDecisionTreeResponse
```

### Get Cost Deltas
```
GET /api/v1/xai/cost-deltas
Query Parameters:
- explanation_id: string (required if decision_id not provided)
- decision_id: string (required if explanation_id not provided)
Response: GetCostDeltasResponse
```

### Verify Explanation
```
GET /api/v1/xai/verify
Query Parameters:
- explanation_id: string (required)
Response: VerifyExplanationResponse
```

### Get Explanation History
```
GET /api/v1/xai/history
Query Parameters:
- start_time: ISO8601 date (optional)
- end_time: ISO8601 date (optional)
- decision_types: comma-separated string (optional)
- statuses: comma-separated string (optional)
- limit: integer (default: 100)
- offset: integer (default: 0)
Response: GetExplanationHistoryResponse
```

## Configuration

### XAI Configuration
```yaml
logi:
  autonomous:
    xai:
      enabled: true
      # Decision tree visualization
      visualization:
        default_format: "mermaid"
        cache_ttl: 3600
      # Blockchain integration
      blockchain:
        enabled: true
        network: "mainnet"
        node_url: "http://localhost:8545"
        contract_address: "0x1234567890abcdef1234567890abcdef12345678"
      # Audit settings
      audit:
        include_execution_environment: true
        include_version_info: true
        require_signature: true
      # Storage
      storage:
        explanations_retention_days: 365
        history_retention_days: 1825
      # Performance
      cache:
        enabled: true
        ttl: 86400
        max_size: 1000
      # Monitoring
      metrics:
        enabled: true
        update_interval: 30000
```

## Metrics

### Prometheus Metrics
```
# XAI Metrics
autonomous_ops_xai_explanations_total
autonomous_ops_xai_explanations_generation_time_seconds
autonomous_ops_xai_explanations_cache_hits
autonomous_ops_xai_explanations_cache_misses
autonomous_ops_xai_blockchain_logs_total
autonomous_ops_xai_blockchain_errors_total
autonomous_ops_xai_verifications_total
autonomous_ops_xai_verifications_failed_total
```

## Logging

### Log Levels
```
DEBUG: Detailed generation and verification steps
INFO: Explanation generation and verification success
WARN: Blockchain log failures, cache misses
ERROR: Generation failures, verification errors
```

### Log Fields
```json
{
  "timestamp": "2024-02-06T22:45:00Z",
  "level": "INFO",
  "explanationId": "XAI-2024-001",
  "decisionId": "OP-2024-001",
  "status": "APPROVED",
  "generationTime": 0.125,
  "cacheHit": true,
  "blockchainLogged": true
}
```

## Usage Examples

### Get Explanation for Decision
```bash
curl -X GET "http://localhost:8087/api/v1/xai/explanation?decision_id=OP-2024-001&include_details=true" \
  -H "Authorization: Bearer <token>"
```

### Verify Explanation
```bash
curl -X GET "http://localhost:8087/api/v1/xai/verify?explanation_id=XAI-2024-001" \
  -H "Authorization: Bearer <token>"
```

### Get Decision Tree in Mermaid Format
```bash
curl -X GET "http://localhost:8087/api/v1/xai/decision-tree?explanation_id=XAI-2024-001&format=mermaid" \
  -H "Authorization: Bearer <token>"
```

## Conclusion
Explainable AI layer provides comprehensive, human-readable explanations for economic decisions with machine-verifiable structure, complete traceability, and blockchain compatibility. The structured JSON format supports both human understanding and automated audit processes, while the decision tree visualization simplifies complex logic comprehension.