# Integration Contracts for Autonomous-OP

## Overview
This document defines integration contracts between Autonomous-OP and other subsystems within the Nodeorb logistics platform. The integration contracts include gRPC/REST service definitions, Kafka event flows, and security boundaries.

## Design Principles
1. **Contract-First Design** - Services interact through well-defined interfaces
2. **Asynchronous Communication** - Kafka for event-driven interactions
3. **Synchronous Communication** - gRPC for direct API calls
4. **Security Boundaries** - Clear security policies for each integration point
5. **Versioning** - Semantic versioning for backward compatibility
6. **Error Handling** - Standard error codes and recovery mechanisms

## FMS Integration (Digital Twins, Telemetry, Maintenance)

### gRPC Contract
```proto
// FMS Integration Service
syntax = "proto3";

package com.autonomous.fms;

import "google/protobuf/timestamp.proto";
import "google/protobuf/struct.proto";

service FMSIntegrationService {
    // Get vehicle digital twin
    rpc GetVehicleDigitalTwin(GetVehicleDigitalTwinRequest) returns (VehicleDigitalTwin);
    
    // Get vehicle telemetry
    rpc GetVehicleTelemetry(GetVehicleTelemetryRequest) returns (VehicleTelemetry);
    
    // Get vehicle maintenance schedule
    rpc GetVehicleMaintenanceSchedule(GetVehicleMaintenanceScheduleRequest) returns (VehicleMaintenanceSchedule);
    
    // Update vehicle telemetry
    rpc UpdateVehicleTelemetry(UpdateVehicleTelemetryRequest) returns (UpdateVehicleTelemetryResponse);
    
    // Create maintenance request
    rpc CreateMaintenanceRequest(CreateMaintenanceRequest) returns (CreateMaintenanceResponse);
    
    // Get maintenance history
    rpc GetMaintenanceHistory(GetMaintenanceHistoryRequest) returns (MaintenanceHistory);
}

// Request for digital twin
message GetVehicleDigitalTwinRequest {
    string vehicle_id = 1;
}

// Vehicle digital twin
message VehicleDigitalTwin {
    string vehicle_id = 1;
    string model = 2;
    string status = 3;
    double battery_level = 4;
    double fuel_level = 5;
    double current_speed = 6;
    Location location = 7;
    repeated Sensor sensors = 8;
    google.protobuf.timestamp last_updated = 9;
}

// Location
message Location {
    double latitude = 1;
    double longitude = 2;
    double altitude = 3;
    double accuracy = 4;
}

// Sensor
message Sensor {
    string sensor_id = 1;
    string type = 2;
    google.protobuf.Value value = 3;
    double confidence = 4;
    google.protobuf.timestamp last_updated = 5;
}

// Request for telemetry
message GetVehicleTelemetryRequest {
    string vehicle_id = 1;
    google.protobuf.timestamp start_time = 2;
    google.protobuf.timestamp end_time = 3;
}

// Vehicle telemetry
message VehicleTelemetry {
    string vehicle_id = 1;
    repeated TelemetryPoint points = 2;
}

// Telemetry point
message TelemetryPoint {
    google.protobuf.timestamp timestamp = 1;
    double speed = 2;
    double acceleration = 3;
    double battery_level = 4;
    double fuel_level = 5;
    Location location = 6;
    repeated SensorReading sensor_readings = 7;
}

// Sensor reading
message SensorReading {
    string sensor_id = 1;
    string type = 2;
    google.protobuf.Value value = 3;
    double confidence = 4;
}

// Request for maintenance schedule
message GetVehicleMaintenanceScheduleRequest {
    string vehicle_id = 1;
}

// Vehicle maintenance schedule
message VehicleMaintenanceSchedule {
    string vehicle_id = 1;
    repeated MaintenanceTask tasks = 2;
}

// Maintenance task
message MaintenanceTask {
    string task_id = 1;
    string type = 2;
    string status = 3;
    google.protobuf.timestamp scheduled_time = 4;
    google.protobuf.timestamp completed_time = 5;
    string description = 6;
    double estimated_duration = 7;
}

// Request to update telemetry
message UpdateVehicleTelemetryRequest {
    string vehicle_id = 1;
    TelemetryPoint telemetry = 2;
}

// Response to update telemetry
message UpdateVehicleTelemetryResponse {
    bool success = 1;
    string message = 2;
}

// Request to create maintenance request
message CreateMaintenanceRequest {
    string vehicle_id = 1;
    string type = 2;
    string description = 3;
    google.protobuf.timestamp requested_time = 4;
}

// Response to create maintenance request
message CreateMaintenanceResponse {
    bool success = 1;
    string message = 2;
    string task_id = 3;
}

// Request to get maintenance history
message GetMaintenanceHistoryRequest {
    string vehicle_id = 1;
    google.protobuf.timestamp start_time = 2;
    google.protobuf.timestamp end_time = 3;
}

// Maintenance history
message MaintenanceHistory {
    string vehicle_id = 1;
    repeated MaintenanceTask tasks = 2;
}
```

### REST Contract
```yaml
openapi: 3.0.0
info:
  title: FMS Integration API
  description: API for integration between Autonomous-OP and Fleet Management System
  version: 1.0.0
paths:
  /fms/vehicles/{vehicleId}/digital-twin:
    get:
      summary: Get vehicle digital twin
      parameters:
        - name: vehicleId
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VehicleDigitalTwin'
  /fms/vehicles/{vehicleId}/telemetry:
    get:
      summary: Get vehicle telemetry
      parameters:
        - name: vehicleId
          in: path
          required: true
          schema:
            type: string
        - name: startTime
          in: query
          required: false
          schema:
            type: string
            format: date-time
        - name: endTime
          in: query
          required: false
          schema:
            type: string
            format: date-time
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VehicleTelemetry'
  /fms/vehicles/{vehicleId}/maintenance-schedule:
    get:
      summary: Get vehicle maintenance schedule
      parameters:
        - name: vehicleId
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VehicleMaintenanceSchedule'
  /fms/vehicles/{vehicleId}/telemetry:
    post:
      summary: Update vehicle telemetry
      parameters:
        - name: vehicleId
          in: path
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/TelemetryPoint'
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UpdateVehicleTelemetryResponse'
  /fms/vehicles/{vehicleId}/maintenance-requests:
    post:
      summary: Create maintenance request
      parameters:
        - name: vehicleId
          in: path
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateMaintenanceRequest'
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CreateMaintenanceResponse'
  /fms/vehicles/{vehicleId}/maintenance-history:
    get:
      summary: Get maintenance history
      parameters:
        - name: vehicleId
          in: path
          required: true
          schema:
            type: string
        - name: startTime
          in: query
          required: false
          schema:
            type: string
            format: date-time
        - name: endTime
          in: query
          required: false
          schema:
            type: string
            format: date-time
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MaintenanceHistory'
components:
  schemas:
    VehicleDigitalTwin:
      type: object
      properties:
        vehicle_id:
          type: string
        model:
          type: string
        status:
          type: string
        battery_level:
          type: number
        fuel_level:
          type: number
        current_speed:
          type: number
        location:
          $ref: '#/components/schemas/Location'
        sensors:
          type: array
          items:
            $ref: '#/components/schemas/Sensor'
        last_updated:
          type: string
          format: date-time
    Location:
      type: object
      properties:
        latitude:
          type: number
        longitude:
          type: number
        altitude:
          type: number
        accuracy:
          type: number
    Sensor:
      type: object
      properties:
        sensor_id:
          type: string
        type:
          type: string
        value:
          type: object
        confidence:
          type: number
        last_updated:
          type: string
          format: date-time
    VehicleTelemetry:
      type: object
      properties:
        vehicle_id:
          type: string
        points:
          type: array
          items:
            $ref: '#/components/schemas/TelemetryPoint'
    TelemetryPoint:
      type: object
      properties:
        timestamp:
          type: string
          format: date-time
        speed:
          type: number
        acceleration:
          type: number
        battery_level:
          type: number
        fuel_level:
          type: number
        location:
          $ref: '#/components/schemas/Location'
        sensor_readings:
          type: array
          items:
            $ref: '#/components/schemas/SensorReading'
    SensorReading:
      type: object
      properties:
        sensor_id:
          type: string
        type:
          type: string
        value:
          type: object
        confidence:
          type: number
    VehicleMaintenanceSchedule:
      type: object
      properties:
        vehicle_id:
          type: string
        tasks:
          type: array
          items:
            $ref: '#/components/schemas/MaintenanceTask'
    MaintenanceTask:
      type: object
      properties:
        task_id:
          type: string
        type:
          type: string
        status:
          type: string
        scheduled_time:
          type: string
          format: date-time
        completed_time:
          type: string
          format: date-time
        description:
          type: string
        estimated_duration:
          type: number
    UpdateVehicleTelemetryResponse:
      type: object
      properties:
        success:
          type: boolean
        message:
          type: string
    CreateMaintenanceRequest:
      type: object
      properties:
        type:
          type: string
        description:
          type: string
        requested_time:
          type: string
          format: date-time
    CreateMaintenanceResponse:
      type: object
      properties:
        success:
          type: boolean
        message:
          type: string
        task_id:
          type: string
    MaintenanceHistory:
      type: object
      properties:
        vehicle_id:
          type: string
        tasks:
          type: array
          items:
            $ref: '#/components/schemas/MaintenanceTask'
```

### Kafka Event Flows
```java
// FMS Event Types
public enum FMSEventType {
    VEHICLE_TELEMETRY_UPDATED,
    VEHICLE_STATUS_CHANGED,
    MAINTENANCE_TASK_CREATED,
    MAINTENANCE_TASK_COMPLETED,
    SENSOR_READING_UPDATED,
    BATTERY_LEVEL_LOW,
    FUEL_LEVEL_LOW
}

// Vehicle Telemetry Updated Event
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleTelemetryUpdatedEvent {
    private String eventId;
    private String vehicleId;
    private String eventType = FMSEventType.VEHICLE_TELEMETRY_UPDATED.name();
    private TelemetryPoint telemetryPoint;
    private long timestamp;
}

// Kafka Event Listener
@Service
public class FMSKafkaEventListener {
    private static final Logger logger = LoggerFactory.getLogger(FMSKafkaEventListener.class);
    
    @KafkaListener(topics = "fms-vehicle-telemetry-updated", groupId = "autonomous-op")
    public void handleVehicleTelemetryUpdatedEvent(VehicleTelemetryUpdatedEvent event) {
        logger.info("Received vehicle telemetry update for vehicle: {}", event.getVehicleId());
        // Handle telemetry update
    }
}
```

### Security Boundaries
```yaml
security:
  # FMS integration security policy
  fms-integration:
    enabled: true
    authentication:
      type: "JWT"
      token-validation:
        enabled: true
        issuer: "https://auth.nodeorb.com"
        audience: "fms"
        jwk-set-uri: "https://auth.nodeorb.com/.well-known/jwks.json"
    authorization:
      enabled: true
      roles-required: ["OPERATOR", "MAINTENANCE", "ADMIN"]
      scopes-required: ["fms.read", "fms.write"]
    transport:
      type: "TLS"
      protocol: "TLSv1.3"
      certificate-validation: "STRICT"
    rate-limiting:
      enabled: true
      requests-per-second: 100
      requests-per-minute: 5000
```

## TMS Integration (Routing, ETA, Replanning)

### gRPC Contract
```proto
// TMS Integration Service
syntax = "proto3";

package com.autonomous.tms;

import "google/protobuf/timestamp.proto";
import "google/protobuf/struct.proto";

service TMSIntegrationService {
    // Get route
    rpc GetRoute(GetRouteRequest) returns (Route);
    
    // Get ETA
    rpc GetETA(GetETARequest) returns (ETA);
    
    // Request replanning
    rpc RequestReplanning(RequestReplanningRequest) returns (RequestReplanningResponse);
    
    // Update route status
    rpc UpdateRouteStatus(UpdateRouteStatusRequest) returns (UpdateRouteStatusResponse);
    
    // Get route history
    rpc GetRouteHistory(GetRouteHistoryRequest) returns (RouteHistory);
}

// Request for route
message GetRouteRequest {
    string route_id = 1;
}

// Route
message Route {
    string route_id = 1;
    string vehicle_id = 2;
    string status = 3;
    Location origin = 4;
    Location destination = 5;
    repeated Waypoint waypoints = 6;
    double distance = 7;
    double duration = 8;
    google.protobuf.timestamp departure_time = 9;
    google.protobuf.timestamp arrival_time = 10;
}

// Location
message Location {
    double latitude = 1;
    double longitude = 2;
    double altitude = 3;
    double accuracy = 4;
}

// Waypoint
message Waypoint {
    string waypoint_id = 1;
    Location location = 2;
    string type = 3;
    google.protobuf.timestamp arrival_time = 4;
}

// Request for ETA
message GetETARequest {
    string route_id = 1;
    Location current_location = 2;
}

// ETA
message ETA {
    string route_id = 1;
    google.protobuf.timestamp estimated_arrival_time = 2;
    double remaining_distance = 3;
    double remaining_duration = 4;
}

// Request for replanning
message RequestReplanningRequest {
    string route_id = 1;
    Location current_location = 2;
    repeated Location avoided_locations = 3;
}

// Response to replanning request
message RequestReplanningResponse {
    bool success = 1;
    string message = 2;
    Route new_route = 3;
}
```

### REST Contract
```yaml
openapi: 3.0.0
info:
  title: TMS Integration API
  description: API for integration between Autonomous-OP and Transportation Management System
  version: 1.0.0
paths:
  /tms/routes/{routeId}:
    get:
      summary: Get route
      parameters:
        - name: routeId
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Route'
  /tms/routes/{routeId}/eta:
    get:
      summary: Get ETA
      parameters:
        - name: routeId
          in: path
          required: true
          schema:
            type: string
        - name: currentLat
          in: query
          required: true
          schema:
            type: number
        - name: currentLon
          in: query
          required: true
          schema:
            type: number
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ETA'
```

### Kafka Event Flows
```java
// TMS Event Types
public enum TMSEventType {
    ROUTE_UPDATED,
    ETA_UPDATED,
    ROUTE_REPLANNED,
    ROUTE_STATUS_CHANGED
}

// Route Updated Event
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RouteUpdatedEvent {
    private String eventId;
    private String routeId;
    private String eventType = TMSEventType.ROUTE_UPDATED.name();
    private Route route;
    private long timestamp;
}

// Kafka Event Listener
@Service
public class TMSKafkaEventListener {
    private static final Logger logger = LoggerFactory.getLogger(TMSKafkaEventListener.class);
    
    @KafkaListener(topics = "tms-route-updated", groupId = "autonomous-op")
    public void handleRouteUpdatedEvent(RouteUpdatedEvent event) {
        logger.info("Received route update for route: {}", event.getRouteId());
        // Handle route update
    }
}
```

### Security Boundaries
```yaml
security:
  # TMS integration security policy
  tms-integration:
    enabled: true
    authentication:
      type: "JWT"
      token-validation:
        enabled: true
        issuer: "https://auth.nodeorb.com"
        audience: "tms"
        jwk-set-uri: "https://auth.nodeorb.com/.well-known/jwks.json"
    authorization:
      enabled: true
      roles-required: ["OPERATOR", "DISPATCHER", "ADMIN"]
      scopes-required: ["tms.read", "tms.write"]
    transport:
      type: "TLS"
      protocol: "TLSv1.3"
      certificate-validation: "STRICT"
    rate-limiting:
      enabled: true
      requests-per-second: 50
      requests-per-minute: 2500
```

## SCM Integration (Compliance, Geofencing, Audit)

### gRPC Contract
```proto
// SCM Integration Service
syntax = "proto3";

package com.autonomous.scm;

import "google/protobuf/timestamp.proto";
import "google/protobuf/struct.proto";

service SCMIntegrationService {
    // Check compliance
    rpc CheckCompliance(CheckComplianceRequest) returns (CheckComplianceResponse);
    
    // Check geofence violation
    rpc CheckGeofenceViolation(CheckGeofenceViolationRequest) returns (CheckGeofenceViolationResponse);
    
    // Get compliance history
    rpc GetComplianceHistory(GetComplianceHistoryRequest) returns (ComplianceHistory);
    
    // Create audit record
    rpc CreateAuditRecord(CreateAuditRecordRequest) returns (CreateAuditRecordResponse);
    
    // Get audit trail
    rpc GetAuditTrail(GetAuditTrailRequest) returns (AuditTrail);
}

// Request to check compliance
message CheckComplianceRequest {
    string vehicle_id = 1;
    string operation_type = 2;
    Location current_location = 3;
    google.protobuf.timestamp timestamp = 4;
    google.protobuf.Struct additional_context = 5;
}

// Response to compliance check
message CheckComplianceResponse {
    bool compliant = 1;
    string message = 2;
    repeated ComplianceIssue issues = 3;
}

// Compliance issue
message ComplianceIssue {
    string issue_id = 1;
    string type = 2;
    string severity = 3;
    string message = 4;
}

// Request to check geofence violation
message CheckGeofenceViolationRequest {
    string vehicle_id = 1;
    Location current_location = 2;
    google.protobuf.timestamp timestamp = 3;
}

// Response to geofence violation check
message CheckGeofenceViolationResponse {
    bool violation = 1;
    string message = 2;
    repeated GeofenceEntry entries = 3;
}

// Geofence entry
message GeofenceEntry {
    string geofence_id = 1;
    string name = 2;
    string type = 3;
    string severity = 4;
}
```

### REST Contract
```yaml
openapi: 3.0.0
info:
  title: SCM Integration API
  description: API for integration between Autonomous-OP and Supply Chain Management System
  version: 1.0.0
paths:
  /scm/compliance/check:
    post:
      summary: Check compliance
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CheckComplianceRequest'
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CheckComplianceResponse'
  /scm/geofence/check:
    post:
      summary: Check geofence violation
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CheckGeofenceViolationRequest'
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CheckGeofenceViolationResponse'
```

### Kafka Event Flows
```java
// SCM Event Types
public enum SCMEventType {
    COMPLIANCE_CHECK_PASSED,
    COMPLIANCE_CHECK_FAILED,
    GEOFENCE_VIOLATION_DETECTED,
    GEOFENCE_VIOLATION_CLEARED,
    AUDIT_RECORD_CREATED
}

// Compliance Check Failed Event
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComplianceCheckFailedEvent {
    private String eventId;
    private String vehicleId;
    private String eventType = SCMEventType.COMPLIANCE_CHECK_FAILED.name();
    private String operationType;
    private Location location;
    private List<ComplianceIssue> issues;
    private long timestamp;
}

// Kafka Event Listener
@Service
public class SCMSKafkaEventListener {
    private static final Logger logger = LoggerFactory.getLogger(SCMSKafkaEventListener.class);
    
    @KafkaListener(topics = "scm-compliance-check-failed", groupId = "autonomous-op")
    public void handleComplianceCheckFailedEvent(ComplianceCheckFailedEvent event) {
        logger.warn("Compliance check failed for vehicle: {}", event.getVehicleId());
        // Handle compliance check failed
    }
}
```

### Security Boundaries
```yaml
security:
  # SCM integration security policy
  scm-integration:
    enabled: true
    authentication:
      type: "JWT"
      token-validation:
        enabled: true
        issuer: "https://auth.nodeorb.com"
        audience: "scm"
        jwk-set-uri: "https://auth.nodeorb.com/.well-known/jwks.json"
    authorization:
      enabled: true
      roles-required: ["OPERATOR", "COMPLIANCE", "ADMIN"]
      scopes-required: ["scm.read", "scm.write"]
    transport:
      type: "TLS"
      protocol: "TLSv1.3"
      certificate-validation: "STRICT"
    rate-limiting:
      enabled: true
      requests-per-second: 20
      requests-per-minute: 1000
    data-encryption:
      enabled: true
      encryption-algorithm: "AES-256-GCM"
    digital-signatures:
      enabled: true
      signature-algorithm: "RSASSA-PSS"
```

## Error Handling and Recovery

### Error Codes
```java
public enum IntegrationErrorCode {
    // General errors
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error"),
    BAD_REQUEST("BAD_REQUEST", "Invalid request"),
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized"),
    FORBIDDEN("FORBIDDEN", "Forbidden"),
    NOT_FOUND("NOT_FOUND", "Resource not found"),
    CONFLICT("CONFLICT", "Resource conflict"),
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "Too many requests"),
    // FMS specific errors
    VEHICLE_NOT_FOUND("VEHICLE_NOT_FOUND", "Vehicle not found"),
    TELEMETRY_NOT_AVAILABLE("TELEMETRY_NOT_AVAILABLE", "Telemetry data not available"),
    MAINTENANCE_NOT_SCHEDULED("MAINTENANCE_NOT_SCHEDULED", "Maintenance not scheduled"),
    // TMS specific errors
    ROUTE_NOT_FOUND("ROUTE_NOT_FOUND", "Route not found"),
    ROUTE_INVALID("ROUTE_INVALID", "Invalid route"),
    REPLANNING_FAILED("REPLANNING_FAILED", "Route replanning failed"),
    // SCM specific errors
    COMPLIANCE_CHECK_FAILED("COMPLIANCE_CHECK_FAILED", "Compliance check failed"),
    GEOFENCE_NOT_CONFIGURED("GEOFENCE_NOT_CONFIGURED", "Geofence not configured"),
    AUDIT_RECORD_FAILED("AUDIT_RECORD_FAILED", "Failed to create audit record");
    
    private final String code;
    private final String message;
    
    IntegrationErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}

// Error response structure
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private String details;
    private long timestamp;
}
```

## Circuit Breaker Configuration
```java
@Configuration
public class CircuitBreakerConfig {
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .slidingWindowType(SlidingWindowType.TIME_BASED)
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .build();
        
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(5))
            .build();
        
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .timeLimiterConfig(timeLimiterConfig)
            .circuitBreakerConfig(circuitBreakerConfig)
            .build());
    }
}
```

## Conclusion
This document provides comprehensive integration contracts between Autonomous-OP and FMS, TMS, and SCM subsystems. The contracts include gRPC and REST API definitions, Kafka event flows, and security boundaries to ensure secure and reliable communication.