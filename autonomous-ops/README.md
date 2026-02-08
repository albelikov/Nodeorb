# Autonomous Ops Service

## Overview
The Autonomous Ops service is responsible for managing and executing autonomous missions for the Nodeorb logistics platform. It provides core business logic for mission planning, execution, and monitoring.

## Structure

### Core Modules
- `com.autonomous.core` - Business logic and service classes
- `com.autonomous.data.entities` - JPA entities for data persistence
- `com.autonomous.services` - Service layer classes for business operations

### Key Classes

#### Core Business Logic
- `AutonomousEngine.kt` - Main engine for executing missions with safety and governance checks
- `DecisionPipeline.kt` - Decision-making pipeline for autonomous operations
- `SafetyController.kt` - Safety checks and validation
- `GovernanceController.kt` - Governance and compliance management
- `MLAdvisorClient.kt` - ML-based advice and recommendations

#### Data Entities
- `Mission.kt` - Mission entity with details about mission type, status, and execution parameters
- `Task.kt` - Task entity representing individual steps within a mission
- `MissionExecutionStatus.kt` - Real-time status of mission execution
- `MissionPlan.kt` - Detailed plan for mission execution
- `CostBreakdown.kt` - Cost breakdown for mission operations
- `RiskAssessment.kt` - Risk assessment for missions
- `Alert.kt` - Alert management for mission execution

#### Services
- `MissionService.kt` - Service for managing missions and their execution

## Functionality

### Mission Management
- Create, update, and delete missions
- Manage mission plans and execution status
- Track mission progress and performance

### Autonomous Execution
- Execute missions with safety checks
- Monitor mission execution in real-time
- Handle alerts and deviations from planned routes

### Decision Making
- Autonomous decision-making pipeline
- Safety and governance checks
- ML-based recommendations

## Technology Stack
- Spring Boot 3.2.4
- Kotlin 1.9.23
- PostgreSQL 18
- Spring Data JPA
- gRPC for communication
- Docker for containerization

## Configuration

### Application Properties
```properties
# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/autonomous_ops
spring.datasource.username=nodeorb
spring.datasource.password=nodeorb_dev_password

# gRPC configuration
spring.grpc.server.port=9090

# Kafka configuration
spring.kafka.bootstrap-servers=localhost:9092

# Logging configuration
logging.level.com.logi.autonomous=DEBUG
```

## Running the Service

### Local Development
```bash
cd c:\Project\Nodeorb
./gradlew :autonomous-ops:compileKotlin
./gradlew :autonomous-ops:test
./gradlew :autonomous-ops:bootRun
```

### Docker Deployment
```bash
cd c:\Project\Nodeorb
docker build -t autonomous-ops:latest -f autonomous-ops/Dockerfile .
docker run -d -p 9090:9090 --network nodeorb-network autonomous-ops:latest
```

## API

### gRPC Services
- `MissionService` - Manage missions
- `DecisionService` - Make autonomous decisions
- `SafetyService` - Check safety conditions
- `MonitoringService` - Monitor mission execution

### REST Endpoints
- `GET /api/missions` - List all missions
- `POST /api/missions` - Create a new mission
- `GET /api/missions/{id}` - Get mission details
- `PUT /api/missions/{id}` - Update mission
- `DELETE /api/missions/{id}` - Delete mission

## Contributing
Please follow the project guidelines for contributing. Create a feature branch, make your changes, and submit a pull request.

## License
MIT License - see LICENSE file for details.