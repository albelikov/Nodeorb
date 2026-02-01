# GitHub Copilot Instructions - Logistics Ecosystem

## Project Context

This is a **dual-purpose logistics ecosystem** supporting commercial operations and strategic resilience (government/defense). The system manages autonomous vehicles, drones, warehouses, and requires strict security compliance.

**Key Technology**: Kotlin Multiplatform, Kubernetes, Kafka, React, PostgreSQL, PostGIS

**Compliance**: FedRAMP, CMMC Level 3, NIST 800-171, GDPR, CSRD, ITAR/EAR

---

## Code Generation Guidelines

### Architecture
- Generate microservices-based code (independently deployable)
- Use event-driven architecture with Kafka
- Implement REST/gRPC APIs
- Follow hexagonal/clean architecture patterns
- Always include dependency injection

### Security First
- **NEVER hardcode credentials** - use environment variables or secret management
- **Always validate input** - implement input sanitization
- **Encrypt sensitive data** - use FIPS 140-2 compliant algorithms
- **Implement RBAC** - every endpoint needs authorization checks
- **Log security events** - all auth attempts, access, and modifications
- **Use mTLS** - for service-to-service communication

### Technology Preferences

#### Backend (Kotlin/Java)
```kotlin
// Prefer Spring Boot with Kotlin
@RestController
@RequestMapping("/api/v1")
class OrderController(
    private val orderService: OrderService
) {
    @PostMapping("/orders")
    @PreAuthorize("hasRole('DISPATCHER')")
    suspend fun createOrder(@Valid @RequestBody request: CreateOrderRequest): OrderResponse
}
```

#### Frontend (React)
```typescript
// Use TypeScript, functional components, hooks
import { useState, useEffect } from 'react';

const Dashboard: React.FC = () => {
    const [mode, setMode] = useState<'civilian' | 'strategic'>('civilian');
    // ...
}
```

#### Mobile (Kotlin Multiplatform)
```kotlin
// Shared business logic
expect class PlatformService {
    fun scanBarcode(): String
}

// Common module
class WarehouseViewModel(
    private val repository: WarehouseRepository
) : ViewModel() {
    // Shared logic
}
```

### Naming Conventions
- **Services**: `OrderManagementSystem`, `FleetManagementSystem`
- **Endpoints**: `/api/v1/order-management`, `/api/v1/fleet-tracking`
- **Variables**: `camelCase` (e.g., `orderId`, `vehicleStatus`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_RETRIES`, `DEFAULT_TIMEOUT`)
- **Database**: `snake_case` (e.g., `order_items`, `vehicle_telemetry`)

### Error Handling Pattern
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val code: String, val message: String, val correlationId: String) : Result<Nothing>()
}

// Usage
fun createOrder(request: CreateOrderRequest): Result<Order> {
    return try {
        val order = orderRepository.save(request.toDomain())
        auditLog.log("ORDER_CREATED", order.id)
        Result.Success(order)
    } catch (e: Exception) {
        logger.error("Failed to create order", e)
        Result.Error("ORDER_001", "Order creation failed", generateCorrelationId())
    }
}
```

### Testing Requirements
- Generate unit tests with >80% coverage
- Use meaningful test names: `should_createOrder_when_validRequest()`
- Mock external dependencies
- Include edge cases and error scenarios

```kotlin
@Test
fun `should reject order when in restricted geofence`() {
    // given
    val order = createTestOrder(location = restrictedLocation)
    
    // when
    val result = orderService.createOrder(order)
    
    // then
    assertTrue(result is Result.Error)
    assertEquals("GEOFENCE_VIOLATION", (result as Result.Error).code)
}
```

---

## Microservices & Components

### Core Services (always consider these contexts)

1. **OMS** - Order Management: lifecycle, status, validation
2. **WMS** - Warehouse: inventory, RFID, AGV integration
3. **TMS** - Transportation: routing, optimization, geofences
4. **FMS** - Fleet: vehicle tracking, IoT telemetry, driver app
5. **SCM** - Security & Compliance: RBAC, audit, GDPR
6. **DroneFleetController** - Autonomous drones: missions, safety
7. **ESG Compliance** - Carbon footprint, sustainability metrics
8. **Integration Gateway** - External system connections

### When generating code for these services:
- Include audit logging
- Implement mode switching (civilian/strategic)
- Validate geofence restrictions
- Calculate ESG metrics where applicable
- Support role-based access

---

## Dual-Purpose Design

### Every component must support two operational modes:

#### Civilian Mode (default)
```kotlin
enum class OperationalMode {
    CIVILIAN,
    STRATEGIC_RESILIENCE
}

@Service
class OrderService(
    private val modeProvider: OperationalModeProvider
) {
    fun processOrder(order: Order): Result<Order> {
        return when (modeProvider.currentMode()) {
            CIVILIAN -> processCivilianOrder(order)
            STRATEGIC_RESILIENCE -> processStrategicOrder(order) // Higher priority
        }
    }
}
```

#### Strategic Mode (government/emergency)
- Override civilian priorities
- Require elevated authorization
- Log mode activation with justification
- Enable resource reallocation

---

## Security & Compliance Patterns

### Authentication & Authorization
```kotlin
// Always use RBAC with context
@PreAuthorize("hasRole('DISPATCHER') and @securityService.checkGeofenceAccess(#location)")
fun planRoute(@RequestBody request: RouteRequest, @PathVariable location: String): RouteResponse

// Implement ABAC for complex scenarios
fun canAccessResource(user: User, resource: Resource, action: Action): Boolean {
    return policyEngine.evaluate(
        subject = user,
        resource = resource,
        action = action,
        environment = getCurrentContext()
    )
}
```

### Audit Logging (immutable)
```kotlin
@Aspect
class AuditAspect {
    @Around("@annotation(Auditable)")
    fun logAuditEvent(joinPoint: ProceedingJoinPoint): Any? {
        val event = AuditEvent(
            timestamp = Instant.now(),
            user = SecurityContextHolder.getContext().authentication.name,
            action = joinPoint.signature.name,
            resource = extractResource(joinPoint),
            result = "PENDING"
        )
        
        return try {
            val result = joinPoint.proceed()
            event.copy(result = "SUCCESS").also { auditRepository.append(it) }
            result
        } catch (e: Exception) {
            event.copy(result = "FAILURE", error = e.message).also { auditRepository.append(it) }
            throw e
        }
    }
}
```

### Data Encryption
```kotlin
// Use FIPS 140-2 compliant encryption
class FIPSEncryptionService {
    fun encrypt(data: ByteArray, key: SecretKey): EncryptedData {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding", "BCFIPS")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(data)
        return EncryptedData(ciphertext, iv)
    }
}
```

---

## Geographic Restrictions

### Always validate locations against geofences:

```kotlin
interface GeofenceValidator {
    fun isAllowed(location: Coordinate, operation: Operation): ValidationResult
    fun getBlockedCountries(): List<CountryCode>
}

@Service
class RouteService(
    private val geofenceValidator: GeofenceValidator
) {
    fun planRoute(from: Location, to: Location): Result<Route> {
        // Validate origin
        if (!geofenceValidator.isAllowed(from.coordinate, Operation.ROUTE)) {
            return Result.Error("GEO_001", "Origin is in restricted zone")
        }
        
        // Validate destination
        if (!geofenceValidator.isAllowed(to.coordinate, Operation.ROUTE)) {
            return Result.Error("GEO_002", "Destination is in restricted zone")
        }
        
        // Plan route avoiding restricted areas
        val route = routePlanner.calculate(from, to, 
            constraints = geofenceValidator.getBlockedCountries()
        )
        
        return Result.Success(route)
    }
}
```

---

## UI Components

### Dashboard Pattern
```typescript
// React dashboard component
interface DashboardProps {
    mode: 'civilian' | 'strategic';
    onModeChange: (mode: 'civilian' | 'strategic') => void;
}

const OperationalDashboard: React.FC<DashboardProps> = ({ mode, onModeChange }) => {
    const [vehicles, setVehicles] = useState<Vehicle[]>([]);
    const [alerts, setAlerts] = useState<Alert[]>([]);
    
    useEffect(() => {
        // WebSocket for real-time updates
        const ws = new WebSocket(`wss://api/v1/real-time`);
        ws.onmessage = (event) => {
            const update = JSON.parse(event.data);
            // Handle update
        };
        return () => ws.close();
    }, []);
    
    return (
        <div className={`dashboard ${mode}`}>
            <ModeIndicator current={mode} onChange={onModeChange} />
            <MapView vehicles={vehicles} />
            <AlertPanel alerts={alerts} />
        </div>
    );
};
```

### Mobile App Pattern (Kotlin Multiplatform)
```kotlin
// Shared ViewModel
class WarehouseViewModel(
    private val repository: WarehouseRepository
) : ViewModel() {
    
    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()
    
    suspend fun scanBarcode(code: String) {
        _scanResult.value = ScanResult.Loading
        try {
            val item = repository.getItemByBarcode(code)
            _scanResult.value = ScanResult.Success(item)
            auditLog.log("ITEM_SCANNED", item.id)
        } catch (e: Exception) {
            _scanResult.value = ScanResult.Error(e.message ?: "Scan failed")
        }
    }
}
```

---

## Integration Patterns

### External System Integration
```kotlin
@Service
class IntegrationGateway(
    private val sapClient: SAPClient,
    private val customsClient: CustomsClient,
    private val circuitBreakerFactory: CircuitBreakerFactory
) {
    
    @CircuitBreaker(name = "sap", fallbackMethod = "sapFallback")
    @RateLimiter(name = "sap")
    @Retry(name = "sap")
    suspend fun syncOrderToSAP(order: Order): Result<SAPOrderResponse> {
        return try {
            val response = sapClient.createOrder(order.toSAPFormat())
            Result.Success(response)
        } catch (e: Exception) {
            logger.error("SAP integration failed", e)
            Result.Error("INT_001", "SAP sync failed")
        }
    }
    
    private fun sapFallback(order: Order, ex: Exception): Result<SAPOrderResponse> {
        // Queue for retry
        retryQueue.enqueue(order)
        return Result.Error("INT_002", "SAP temporarily unavailable, queued for retry")
    }
}
```

### Event-Driven Communication (Kafka)
```kotlin
@Service
class OrderEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, OrderEvent>
) {
    
    fun publishOrderCreated(order: Order) {
        val event = OrderEvent(
            eventId = UUID.randomUUID(),
            eventType = "ORDER_CREATED",
            timestamp = Instant.now(),
            orderId = order.id,
            payload = order
        )
        
        kafkaTemplate.send("orders.events", order.id, event)
            .addCallback(
                { logger.info("Event published: ${event.eventId}") },
                { ex -> logger.error("Event publish failed", ex) }
            )
    }
}

@KafkaListener(topics = ["orders.events"], groupId = "warehouse-service")
fun handleOrderEvent(event: OrderEvent) {
    when (event.eventType) {
        "ORDER_CREATED" -> prepareInventory(event.payload)
        "ORDER_CANCELLED" -> releaseInventory(event.payload)
    }
}
```

---

## ESG & Sustainability

### Carbon Footprint Calculation
```kotlin
@Service
class CarbonFootprintService {
    
    fun calculateEmissions(route: Route, vehicle: Vehicle): CarbonEmissions {
        val distance = route.totalDistance
        val emissionFactor = vehicle.emissionFactorPerKm
        
        val co2 = distance * emissionFactor
        
        return CarbonEmissions(
            co2KG = co2,
            route = route.id,
            vehicle = vehicle.id,
            timestamp = Instant.now()
        ).also {
            metricsRepository.save(it)
            esgReportingService.update(it)
        }
    }
}
```

### Reverse Logistics Flow
```kotlin
// Complete return cycle
@Service
class ReverseLogisticsService(
    private val rms: ReturnsManagementSystem,
    private val rws: ReverseWarehouseSystem,
    private val rlp: ReverseLogisticsPlanning,
    private val droneController: DroneFleetController
) {
    
    suspend fun processReturn(returnRequest: ReturnRequest): Result<Return> {
        // 1. Accept return
        val returnOrder = rms.createReturn(returnRequest)
        
        // 2. Warehouse certification
        val certResult = rws.certifyItem(returnOrder.itemId)
        
        // 3. Plan reverse logistics
        val route = rlp.planReturnRoute(returnOrder)
        
        // 4. Schedule drone pickup if applicable
        if (certResult.reusable) {
            droneController.scheduleMission(
                type = MissionType.PICKUP,
                location = returnOrder.location,
                priority = returnOrder.priority
            )
        }
        
        // 5. Update ESG metrics
        esgService.recordRecycling(certResult)
        
        return Result.Success(returnOrder)
    }
}
```

---

## Performance & Scalability

### Caching Strategy
```kotlin
@Cacheable(value = ["vehicles"], key = "#vehicleId")
fun getVehicle(vehicleId: String): Vehicle {
    return vehicleRepository.findById(vehicleId)
}

@CacheEvict(value = ["vehicles"], key = "#vehicle.id")
fun updateVehicle(vehicle: Vehicle): Vehicle {
    return vehicleRepository.save(vehicle)
}
```

### Async Processing
```kotlin
@Service
class OrderProcessingService(
    @Qualifier("taskExecutor") private val executor: AsyncTaskExecutor
) {
    
    @Async
    fun processLargeOrder(order: Order): CompletableFuture<OrderResult> {
        return CompletableFuture.supplyAsync({
            // Long-running process
            val result = heavyComputation(order)
            notificationService.notify(order.customerId, "Order processed")
            result
        }, executor)
    }
}
```

---

## Prohibited Patterns

### ❌ Never Generate:
```kotlin
// DON'T: Hardcoded credentials
val apiKey = "sk-1234567890abcdef"

// DON'T: SQL injection vulnerability
val query = "SELECT * FROM orders WHERE id = '$orderId'"

// DON'T: Exposing sensitive data
logger.info("User password: ${user.password}")

// DON'T: Ignoring exceptions
try { riskyOperation() } catch (e: Exception) { }

// DON'T: Blocking main thread
runBlocking { longRunningTask() }
```

### ✅ Always Generate:
```kotlin
// DO: Environment variables
val apiKey = System.getenv("API_KEY") ?: throw ConfigurationException("API_KEY not set")

// DO: Parameterized queries
val query = entityManager.createQuery("SELECT o FROM Order o WHERE o.id = :id")
    .setParameter("id", orderId)

// DO: Redacted logging
logger.info("User authenticated: ${user.username}")

// DO: Proper error handling
try { 
    riskyOperation() 
} catch (e: Exception) { 
    logger.error("Operation failed", e)
    throw BusinessException("Operation failed", e)
}

// DO: Async operations
launch(Dispatchers.IO) { longRunningTask() }
```

---

## Documentation Standards

### API Documentation
```kotlin
/**
 * Creates a new order in the system.
 * 
 * Validates geofence restrictions, checks inventory availability,
 * and publishes ORDER_CREATED event to Kafka.
 * 
 * @param request Order creation request containing items and delivery location
 * @return Created order with assigned ID and status
 * @throws GeofenceViolationException if delivery location is restricted
 * @throws InsufficientInventoryException if items are not available
 * 
 * @see OrderManagementSystem
 * @see GeofenceValidator
 */
@PostMapping("/orders")
@PreAuthorize("hasRole('DISPATCHER')")
fun createOrder(@Valid @RequestBody request: CreateOrderRequest): OrderResponse
```

### Architecture Decisions
Document significant decisions in ADR (Architecture Decision Record) format:
```markdown
# ADR-001: Use Kotlin Multiplatform for Mobile Apps

## Status
Accepted

## Context
Need to maintain iOS and Android apps with shared business logic.

## Decision
Use Kotlin Multiplatform to share viewmodels and domain logic.

## Consequences
- Positive: Single codebase for business logic
- Positive: Type-safe sharing between platforms
- Negative: Team needs to learn KMP
```

---

## Key User Flows to Support

### 1. Security Threat Response
```kotlin
// SCM Alert → Dispatcher → Strategic Mode → Drone Reassignment
@Service
class SecurityResponseService(
    private val scm: SecurityComplianceModule,
    private val modeController: OperationalModeController,
    private val droneController: DroneFleetController
) {
    suspend fun handleSecurityThreat(alert: SecurityAlert) {
        // Log threat
        scm.logSecurityEvent(alert)
        
        // Activate strategic mode
        modeController.activateStrategicMode(
            reason = "Security threat: ${alert.type}",
            approvedBy = alert.responderId
        )
        
        // Reassign drones
        droneController.reassignForStrategicMission(
            threatLocation = alert.location,
            priority = Priority.CRITICAL
        )
        
        // Notify relevant parties
        notificationService.sendSecurityAlert(alert)
    }
}
```

### 2. Reverse Logistics & Recycling
```kotlin
// Mobile App → Certification → Drone Delivery → LSP Update
// (See complete flow in Reverse Logistics section above)
```

### 3. Policy Administration
```kotlin
// Admin Panel → Geofence Config → Route Blocking → SCM Logging
@Service
class PolicyAdministrationService(
    private val geofenceService: GeofenceService,
    private val routeService: RouteService,
    private val scm: SecurityComplianceModule
) {
    @Transactional
    fun updateGeofencePolicy(policy: GeofencePolicy): Result<GeofencePolicy> {
        // Update geofence configuration
        geofenceService.updatePolicy(policy)
        
        // Block affected routes
        val affectedRoutes = routeService.findRoutesInGeofence(policy.area)
        affectedRoutes.forEach { route ->
            routeService.blockRoute(route.id, reason = "Geofence policy update")
        }
        
        // Log in SCM
        scm.logPolicyChange(
            policyType = "GEOFENCE",
            changedBy = getCurrentUser(),
            details = policy
        )
        
        return Result.Success(policy)
    }
}
```

---

## Real-Time Features

### WebSocket Pattern
```kotlin
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic", "/queue")
        registry.setApplicationDestinationPrefixes("/app")
    }
    
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins("*")
            .withSockJS()
    }
}

@Controller
class VehicleTrackingController(
    private val vehicleService: VehicleService
) {
    
    @MessageMapping("/vehicle/location")
    @SendTo("/topic/vehicles")
    fun updateVehicleLocation(location: VehicleLocation): VehicleLocation {
        vehicleService.updateLocation(location)
        return location
    }
}
```

---

## Quick Reference

### Common Imports
```kotlin
// Spring Boot
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.security.access.prepost.PreAuthorize

// Kotlin Coroutines
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// Kafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate

// Database
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*
```

### Environment Variables to Expect
```
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=logistics
DB_USER=app_user
DB_PASSWORD=<from-secrets>

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Security
JWT_SECRET=<from-secrets>
ENCRYPTION_KEY=<from-secrets>

# External APIs
SAP_API_URL=https://api.sap.com
SAP_API_KEY=<from-secrets>

# Operational
OPERATIONAL_MODE=CIVILIAN
ALLOWED_COUNTRIES=US,CA,MX
BLOCKED_COUNTRIES=<from-config>
```

---

## Remember

- **Security is not optional** - every feature must be secure by design
- **Dual-purpose always** - support both civilian and strategic modes
- **Compliance first** - FedRAMP, CMMC, GDPR are mandatory
- **Audit everything** - immutable logs for all operations
- **Performance matters** - system must handle high load
- **Test thoroughly** - >80% coverage, integration tests, e2e tests
- **Document clearly** - ADRs, API docs, architecture diagrams

This system handles critical infrastructure. Code quality and security cannot be compromised.

---

**For questions or clarification on these guidelines, consult the architecture team.**