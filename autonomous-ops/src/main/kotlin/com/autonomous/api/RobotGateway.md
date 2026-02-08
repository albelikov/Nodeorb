# Robot Gateway (Layer 1)

## Overview
Robot Gateway предоставляет универсальный интерфейс для подключения любых роботов к системе Nodeorb. Он поддерживает множество протоколов коммуникации, включая MAVLink, ROS2, MQTT и WebSockets, и обеспечивает прозрачную интеграцию с верхними слоями системы.

## Design Principles
1. **Protocol Agnosticism** - Поддержка множества протоколов без изменения кода
2. **Node Profiles** - Структурированное описание характеристик робота
3. **Real-time Communication** - Локальная и глобальная синхронизация с низкой задержкой
4. **Fault Tolerance** - Автоматическое переключение между протоколами при сбоях
5. **Security** - Шифрование данных и аутентификация роботов
6. **Scalability** - Поддержка тысячи одновременных подключений

## Architecture

### Robot Gateway Pipeline
```
┌─────────────────────────────────────────────────────────────────┐
│                    Robot Gateway Pipeline                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Protocol   │  │   Node       │  │   Telemetry  │          │
│  │  Decoder     │  │  Profile     │  │  Processing  │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
│         │                 │                 │                   │
│         └────────────┬────┴─────────────────┘                   │
│                      ▼                                           │
│              ┌──────────────┐                                    │
│              │   Message    │                                    │
│              │  Router      │                                    │
│              └──────┬───────┘                                    │
│                     │                                            │
│         ┌───────────┴───────────┐                               │
│         ▼                       ▼                               │
│  ┌──────────────┐      ┌──────────────┐                          │
│  │   Local      │      │   Global     │                          │
│  │  Navigation  │      │  Synchronization│                        │
│  └──────┬───────┘      └──────┬───────┘                          │
│         │                     │                                  │
│         └──────────┬──────────┘                                  │
│                    ▼                                             │
│            ┌──────────────┐                                       │
│            │   Safety     │                                       │
│            │  Validation  │                                       │
│            └──────┬───────┘                                       │
│                   │                                               │
│         ┌──────────┴──────────┐                                  │
│         ▼                      ▼                                 │
│  ┌──────────────┐       ┌──────────────┐                        │
│  │   Command    │       │   Status     │                        │
│  │  Execution   │       │  Monitoring  │                        │
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

## Supported Protocols

### Protocol Support Table
| Protocol | Type | Use Case | Latency | Throughput | Security |
|----------|------|----------|---------|------------|----------|
| MAVLink | UAV Control | Дроны и беспилотные аппараты | < 50ms | 1000 msg/sec | TLS 1.3 |
| ROS2 | Robot Control | Industrial robots (AGV, AMR) | < 100ms | 500 msg/sec | DDS-Security |
| MQTT | IoT | Sensors and telemetry | < 200ms | 10000 msg/sec | TLS 1.3 + mTLS |
| WebSockets | Web | Browser-based control | < 500ms | 1000 msg/sec | TLS 1.3 |
| gRPC | Inter-service | Communication with other modules | < 100ms | 500 msg/sec | TLS 1.3 |

### Protocol Decoder Architecture
```kotlin
interface ProtocolDecoder {
    fun decode(data: ByteArray): RobotMessage
    fun encode(message: RobotMessage): ByteArray
    fun getProtocol(): ProtocolType
    fun validateConnection(): Boolean
    fun getSupportedMessages(): List<MessageType>
}

class MavlinkDecoder : ProtocolDecoder {
    override fun decode(data: ByteArray): RobotMessage {
        // Decode MAVLink message
        val msg = MAVLinkMessage.parseFrom(data)
        return RobotMessage(
            protocol = ProtocolType.MAVLINK,
            type = msg.msgId.toMessageType(),
            payload = msg.payload,
            timestamp = System.currentTimeMillis()
        )
    }
    
    override fun encode(message: RobotMessage): ByteArray {
        // Encode to MAVLink
        val mavlinkMsg = MAVLinkMessage.newBuilder()
            .setMsgId(message.type.toMavlinkId())
            .setPayload(message.payload)
            .build()
        return mavlinkMsg.toByteArray()
    }
}

class ROS2Decoder : ProtocolDecoder {
    override fun decode(data: ByteArray): RobotMessage {
        // Decode ROS2 message
        val msg = ROS2Message.parseFrom(data)
        return RobotMessage(
            protocol = ProtocolType.ROS2,
            type = msg.msgType.toMessageType(),
            payload = msg.payload,
            timestamp = System.currentTimeMillis()
        )
    }
}
```

## Node Profile Schema

### Node Profile Definition
```json
{
  "node_id": "drone_001",
  "type": "UAV",
  "model": "DJI Mavic 3 Enterprise",
  "manufacturer": "DJI",
  "capabilities": {
    "max_speed_kmh": 80,
    "payload_kg": 5,
    "battery_kwh": 0.5,
    "sensors": [
      {
        "id": "lidar_001",
        "type": "lidar",
        "range": 100,
        "accuracy": 0.1
      },
      {
        "id": "camera_001",
        "type": "rgb_camera",
        "resolution": "4K",
        "frame_rate": 30
      },
      {
        "id": "ms_camera_001",
        "type": "multispectral",
        "bands": ["red", "green", "blue", "nir"]
      }
    ],
    "communication": {
      "protocols": ["MAVLink", "MQTT"],
      "max_bandwidth_mbps": 10,
      "range_km": 10
    }
  },
  "costs": {
    "energy_per_km": 0.02,  // kWh/km
    "maintenance_per_hour": 5.0,  // USD/hour
    "depreciation_per_hour": 10.0,  // USD/hour
    "insurance_per_mission": 2.0  // USD/mission
  },
  "safety": {
    "max_flight_altitude_m": 120,
    "min_flight_altitude_m": 5,
    "emergency_stop_distance_m": 10,
    "safe_landing_zones": ["zone_001", "zone_002"]
  },
  "telemetry": {
    "update_frequency_hz": 10,
    "required_fields": ["position", "velocity", "battery_level", "payload_status"],
    "optional_fields": ["sensor_readings", "communication_status"]
  }
}
```

### Node Profile Validation
```kotlin
class NodeProfileValidator {
    fun validate(profile: NodeProfile): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (profile.nodeId.isBlank()) {
            errors.add("Node ID cannot be blank")
        }
        
        if (!NodeType.values().map { it.name }.contains(profile.type)) {
            errors.add("Invalid node type: ${profile.type}")
        }
        
        if (profile.capabilities.maxSpeedKmh < 0) {
            errors.add("Max speed cannot be negative")
        }
        
        if (profile.capabilities.payloadKg < 0) {
            errors.add("Payload cannot be negative")
        }
        
        if (profile.capabilities.sensors.isEmpty()) {
            errors.add("Node must have at least one sensor")
        }
        
        if (profile.costs.energyPerKm < 0) {
            errors.add("Energy cost per km cannot be negative")
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
}

data class ValidationResult(
    val valid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)
```

## Real-time Communication

### Local Navigation Mode
```kotlin
class LocalNavigationService(private val robotGateway: RobotGateway) {
    suspend fun navigateLocal(nodeId: String, target: Location): NavigationResult {
        val robot = robotGateway.getRobot(nodeId)
        if (!robot.isConnected) {
            return NavigationResult.Failure("Robot not connected")
        }
        
        // Use local sensors (SLAM, LIDAR) for navigation
        val navigationPath = calculateLocalPath(robot.position, target, robot.sensors)
        
        // Send command to robot
        val command = NavigationCommand(
            type = CommandType.LOCAL_NAVIGATION,
            path = navigationPath,
            speed = robot.capabilities.maxSpeedKmh * 0.5  // 50% of max speed for safety
        )
        
        return robotGateway.sendCommand(nodeId, command)
    }
    
    private suspend fun calculateLocalPath(from: Location, to: Location, sensors: List<Sensor>): List<Waypoint> {
        // Use SLAM to build local map and find path
        val slamMap = buildSLAMMap(sensors)
        return findPathUsingAStar(slamMap, from, to)
    }
}
```

### Global Synchronization
```kotlin
class GlobalSynchronizationService(private val robotGateway: RobotGateway) {
    suspend fun syncAllNodes(): SyncResult {
        val allNodes = robotGateway.getConnectedNodes()
        val syncTasks = allNodes.map { node ->
            async {
                syncNode(node.id)
            }
        }
        
        val results = syncTasks.awaitAll()
        val failedNodes = results.filterIsInstance<SyncResult.Failure>()
        
        if (failedNodes.isNotEmpty()) {
            return SyncResult.Failure("Failed to sync ${failedNodes.size} nodes")
        }
        
        return SyncResult.Success("All ${allNodes.size} nodes synchronized")
    }
    
    private suspend fun syncNode(nodeId: String): SyncResult {
        val node = robotGateway.getRobot(nodeId)
        if (!node.isConnected) {
            return SyncResult.Failure("Node $nodeId not connected")
        }
        
        // Sync global map and mission plan
        val globalMap = getGlobalMap()
        val missionPlan = getMissionPlan(nodeId)
        
        val syncMessage = SyncMessage(
            type = SyncType.GLOBAL_MAP,
            map = globalMap,
            missionPlan = missionPlan
        )
        
        return robotGateway.sendMessage(nodeId, syncMessage)
    }
}
```

## Fault Tolerance

### Protocol Failover
```kotlin
class ProtocolFailoverManager(private val robotGateway: RobotGateway) {
    suspend fun monitorConnections() {
        while (isActive) {
            val connectedNodes = robotGateway.getConnectedNodes()
            
            connectedNodes.forEach { node ->
                if (!node.isCommunicating()) {
                    attemptFailover(node.id)
                }
            }
            
            delay(FailoverConfig.MONITORING_INTERVAL)
        }
    }
    
    private suspend fun attemptFailover(nodeId: String): FailoverResult {
        val node = robotGateway.getRobot(nodeId)
        val availableProtocols = node.capabilities.communication.protocols
            .filter { it != node.currentProtocol }
            .sortedBy { getProtocolPriority(it) }
        
        availableProtocols.forEach { protocol ->
            val result = robotGateway.switchProtocol(nodeId, protocol)
            if (result.success) {
                return FailoverResult.Success("Switched to $protocol")
            }
        }
        
        return FailoverResult.Failure("No available protocols for failover")
    }
    
    private fun getProtocolPriority(protocol: ProtocolType): Int {
        return when (protocol) {
            ProtocolType.MAVLINK -> 1
            ProtocolType.ROS2 -> 2
            ProtocolType.MQTT -> 3
            ProtocolType.WEBSOCKETS -> 4
            ProtocolType.GRPC -> 5
        }
    }
}

object FailoverConfig {
    const val MONITORING_INTERVAL = 5000L  // 5 seconds
    const val FAILOVER_ATTEMPTS = 3
    const val FAILOVER_DELAY = 1000L  // 1 second between attempts
}
```

## Security

### Robot Authentication
```kotlin
class RobotAuthenticator(private val keyStore: KeyStore) {
    suspend fun authenticate(nodeId: String, credentials: String): AuthenticationResult {
        val storedCredentials = keyStore.getRobotCredentials(nodeId)
        
        if (storedCredentials == null) {
            return AuthenticationResult.Failure("Unknown robot: $nodeId")
        }
        
        if (!validateCredentials(credentials, storedCredentials)) {
            return AuthenticationResult.Failure("Invalid credentials for $nodeId")
        }
        
        return AuthenticationResult.Success(nodeId)
    }
    
    private suspend fun validateCredentials(provided: String, stored: String): Boolean {
        // Verify digital signature or hash
        return verifySignature(provided, stored)
    }
}

data class AuthenticationResult(
    val success: Boolean,
    val nodeId: String? = null,
    val error: String? = null
)
```

### Data Encryption
```kotlin
class DataEncryptor(private val encryptionService: EncryptionService) {
    fun encrypt(data: ByteArray, nodeId: String): ByteArray {
        val publicKey = getPublicKey(nodeId)
        return encryptionService.encrypt(data, publicKey)
    }
    
    fun decrypt(data: ByteArray, nodeId: String): ByteArray {
        val privateKey = getPrivateKey(nodeId)
        return encryptionService.decrypt(data, privateKey)
    }
    
    private fun getPublicKey(nodeId: String): PublicKey {
        // Load public key from keystore
        return keystore.getPublicKey(nodeId)
    }
    
    private fun getPrivateKey(nodeId: String): PrivateKey {
        // Load private key from keystore
        return keystore.getPrivateKey(nodeId)
    }
}
```

## API Contract

### Robot Gateway API
```proto
// Robot Gateway gRPC API
syntax = "proto3";

package com.autonomous.robot;

import "google/protobuf/timestamp.proto";

// Robot Gateway service definition
service RobotGatewayService {
    // Register new robot
    rpc RegisterRobot(RegisterRobotRequest) returns (RegisterRobotResponse);

    // Get robot information
    rpc GetRobotInfo(GetRobotInfoRequest) returns (GetRobotInfoResponse);

    // Send command to robot
    rpc SendCommand(SendCommandRequest) returns (SendCommandResponse);

    // Send message to robot
    rpc SendMessage(SendMessageRequest) returns (SendMessageResponse);

    // Get robot status
    rpc GetRobotStatus(GetRobotStatusRequest) returns (GetRobotStatusResponse);

    // Get all connected robots
    rpc GetConnectedRobots(GetConnectedRobotsRequest) returns (GetConnectedRobotsResponse);

    // Switch communication protocol
    rpc SwitchProtocol(SwitchProtocolRequest) returns (SwitchProtocolResponse);

    // Disconnect robot
    rpc DisconnectRobot(DisconnectRobotRequest) returns (DisconnectRobotResponse);
}

// Register Robot Request
message RegisterRobotRequest {
    string node_id = 1;
    NodeProfile profile = 2;
    string credentials = 3;
}

// Register Robot Response
message RegisterRobotResponse {
    bool success = 1;
    string message = 2;
    string node_id = 3;
}

// Get Robot Info Request
message GetRobotInfoRequest {
    string node_id = 1;
}

// Get Robot Info Response
message GetRobotInfoResponse {
    string node_id = 1;
    NodeProfile profile = 2;
    RobotStatus status = 3;
}

// Send Command Request
message SendCommandRequest {
    string node_id = 1;
    Command command = 2;
}

// Send Command Response
message SendCommandResponse {
    bool success = 1;
    string message = 2;
    CommandResult result = 3;
}

// Send Message Request
message SendMessageRequest {
    string node_id = 1;
    RobotMessage message = 2;
}

// Send Message Response
message SendMessageResponse {
    bool success = 1;
    string message = 2;
    google.protobuf.Timestamp sent_at = 3;
}

// Get Robot Status Request
message GetRobotStatusRequest {
    string node_id = 1;
}

// Get Robot Status Response
message GetRobotStatusResponse {
    string node_id = 1;
    RobotStatus status = 2;
}

// Get Connected Robots Request
message GetConnectedRobotsRequest {
    int32 limit = 1;
    int32 offset = 2;
}

// Get Connected Robots Response
message GetConnectedRobotsResponse {
    int32 total_count = 1;
    repeated RobotSummary robots = 2;
}

// Switch Protocol Request
message SwitchProtocolRequest {
    string node_id = 1;
    ProtocolType protocol = 2;
}

// Switch Protocol Response
message SwitchProtocolResponse {
    bool success = 1;
    string message = 2;
    ProtocolType protocol = 3;
}

// Disconnect Robot Request
message DisconnectRobotRequest {
    string node_id = 1;
}

// Disconnect Robot Response
message DisconnectRobotResponse {
    bool success = 1;
    string message = 2;
}

// Node Profile
message NodeProfile {
    string node_id = 1;
    string type = 2;
    string model = 3;
    string manufacturer = 4;
    Capabilities capabilities = 5;
    Costs costs = 6;
    Safety safety = 7;
    TelemetryConfig telemetry = 8;
}

// Capabilities
message Capabilities {
    double max_speed_kmh = 1;
    double payload_kg = 2;
    double battery_kwh = 3;
    repeated Sensor sensors = 4;
    Communication communication = 5;
}

// Sensor
message Sensor {
    string id = 1;
    string type = 2;
    double range = 3;
    double accuracy = 4;
    map<string, string> properties = 5;
}

// Communication
message Communication {
    repeated string protocols = 1;
    double max_bandwidth_mbps = 2;
    double range_km = 3;
}

// Costs
message Costs {
    double energy_per_km = 1;
    double maintenance_per_hour = 2;
    double depreciation_per_hour = 3;
    double insurance_per_mission = 4;
}

// Safety
message Safety {
    double max_flight_altitude_m = 1;
    double min_flight_altitude_m = 2;
    double emergency_stop_distance_m = 3;
    repeated string safe_landing_zones = 4;
}

// Telemetry Configuration
message TelemetryConfig {
    int32 update_frequency_hz = 1;
    repeated string required_fields = 2;
    repeated string optional_fields = 3;
}

// Robot Status
message RobotStatus {
    string node_id = 1;
    string status = 2;
    Location location = 3;
    double velocity_kmh = 4;
    double battery_level = 5;
    double payload_status = 6;
    repeated SensorReading sensor_readings = 7;
    string current_protocol = 8;
    bool is_connected = 9;
}

// Location
message Location {
    double latitude = 1;
    double longitude = 2;
    double altitude = 3;
    double accuracy = 4;
}

// Sensor Reading
message SensorReading {
    string sensor_id = 1;
    string type = 2;
    double value = 3;
    double confidence = 4;
    google.protobuf.Timestamp timestamp = 5;
}

// Command
message Command {
    string id = 1;
    CommandType type = 2;
    map<string, string> parameters = 3;
    google.protobuf.Timestamp timestamp = 4;
}

// Command Type
enum CommandType {
    UNKNOWN = 0;
    TAKE_OFF = 1;
    LAND = 2;
    NAVIGATE_GLOBAL = 3;
    NAVIGATE_LOCAL = 4;
    EMERGENCY_STOP = 5;
    RETURN_TO_LAUNCH = 6;
    PAUSE = 7;
    RESUME = 8;
    SET_SPEED = 9;
    SET_ALTITUDE = 10;
}

// Command Result
message CommandResult {
    string command_id = 1;
    string status = 2;
    string message = 3;
    google.protobuf.Timestamp executed_at = 4;
}

// Robot Message
message RobotMessage {
    string id = 1;
    ProtocolType protocol = 2;
    MessageType type = 3;
    bytes payload = 4;
    google.protobuf.Timestamp timestamp = 5;
}

// Protocol Type
enum ProtocolType {
    UNKNOWN = 0;
    MAVLINK = 1;
    ROS2 = 2;
    MQTT = 3;
    WEBSOCKETS = 4;
    GRPC = 5;
}

// Message Type
enum MessageType {
    UNKNOWN = 0;
    TELEMETRY = 1;
    STATUS = 2;
    COMMAND = 3;
    RESPONSE = 4;
    ERROR = 5;
    WARNING = 6;
}

// Robot Summary
message RobotSummary {
    string node_id = 1;
    string type = 2;
    string model = 3;
    string status = 4;
    double battery_level = 5;
    bool is_connected = 6;
}
```

## REST API Endpoints

### Register Robot
```
POST /api/v1/robot/register
Request: RegisterRobotRequest
Response: RegisterRobotResponse
```

### Get Robot Info
```
GET /api/v1/robot/{nodeId}/info
Response: GetRobotInfoResponse
```

### Send Command
```
POST /api/v1/robot/{nodeId}/command
Request: SendCommandRequest
Response: SendCommandResponse
```

### Send Message
```
POST /api/v1/robot/{nodeId}/message
Request: SendMessageRequest
Response: SendMessageResponse
```

### Get Robot Status
```
GET /api/v1/robot/{nodeId}/status
Response: GetRobotStatusResponse
```

### Get Connected Robots
```
GET /api/v1/robot/connected
Query Parameters:
- limit: integer (default: 100)
- offset: integer (default: 0)
Response: GetConnectedRobotsResponse
```

### Switch Protocol
```
POST /api/v1/robot/{nodeId}/protocol
Request: SwitchProtocolRequest
Response: SwitchProtocolResponse
```

### Disconnect Robot
```
POST /api/v1/robot/{nodeId}/disconnect
Response: DisconnectRobotResponse
```

## Configuration

### Robot Gateway Configuration
```yaml
logi:
  autonomous:
    robot:
      enabled: true
      # Protocol support
      protocols:
        mavlink:
          enabled: true
          port: 5760
          baud_rate: 115200
          heartbeat_interval: 1000
        ros2:
          enabled: true
          port: 9090
          dds_domain: 0
          quality_of_service: "RELIABLE"
        mqtt:
          enabled: true
          broker_url: "tcp://mqtt:1883"
          client_id: "robot-gateway"
          username: "robot"
          password: "password"
        websockets:
          enabled: true
          port: 8088
          path: "/ws"
          idle_timeout: 300
      # Node profiles
      profiles:
        auto_load: true
        directory: "classpath:profiles"
        validation:
          enabled: true
          strict_mode: false
      # Security
      security:
        authentication:
          enabled: true
          method: "JWT"
          token_validation:
            issuer: "https://auth.nodeorb.com"
            audience: "robot"
            jwk_set_uri: "https://auth.nodeorb.com/.well-known/jwks.json"
        encryption:
          enabled: true
          algorithm: "AES-256-GCM"
          key_size: 256
        rate_limiting:
          enabled: true
          requests_per_second: 100
          requests_per_minute: 5000
      # Fault tolerance
      failover:
        enabled: true
        monitoring_interval: 5000
        failover_attempts: 3
        failover_delay: 1000
      # Telemetry
      telemetry:
        enabled: true
        update_interval: 1000
        batch_size: 10
        storage:
          type: "timescaledb"
          retention_days: 90
      # Performance
      thread_pool:
        core_pool_size: 10
        max_pool_size: 100
        queue_capacity: 1000
        keep_alive_seconds: 60
```

## Metrics

### Prometheus Metrics
```
# Robot Gateway Metrics
autonomous_ops_robot_connected_nodes_total
autonomous_ops_robot_registered_nodes_total
autonomous_ops_robot_disconnected_nodes_total
autonomous_ops_robot_protocol_failures_total
autonomous_ops_robot_command_execution_time_seconds
autonomous_ops_robot_message_processing_time_seconds
autonomous_ops_robot_telemetry_updates_total
autonomous_ops_robot_sensor_readings_total
autonomous_ops_robot_battery_level_avg
autonomous_ops_robot_payload_status_avg
```

## Logging

### Log Levels
```
DEBUG: Detailed protocol communication, node registration
INFO: Robot connection status, command execution
WARN: Protocol failures, low battery, sensor errors
ERROR: Robot disconnections, communication failures
```

### Log Fields
```json
{
  "timestamp": "2024-02-06T22:45:00Z",
  "level": "INFO",
  "nodeId": "drone_001",
  "eventType": "COMMAND_EXECUTED",
  "commandType": "TAKE_OFF",
  "status": "SUCCESS",
  "executionTime": 0.125,
  "protocol": "MAVLINK",
  "batteryLevel": 0.85,
  "location": {"lat": 51.5074, "lon": -0.1278}
}
```

## Usage Example

### Register and Control Robot
```kotlin
fun main() {
    runBlocking {
        val robotGateway = RobotGateway()
        
        // Register robot
        val registerRequest = RegisterRobotRequest(
            nodeId = "drone_001",
            profile = NodeProfile(
                nodeId = "drone_001",
                type = "UAV",
                model = "DJI Mavic 3 Enterprise",
                capabilities = Capabilities(
                    maxSpeedKmh = 80,
                    payloadKg = 5,
                    batteryKwh = 0.5,
                    sensors = listOf(
                        Sensor("lidar_001", "lidar", 100.0, 0.1),
                        Sensor("camera_001", "rgb_camera", 0.0, 0.0)
                    ),
                    communication = Communication(
                        protocols = listOf("MAVLINK", "MQTT"),
                        maxBandwidthMbps = 10.0,
                        rangeKm = 10.0
                    )
                )
            ),
            credentials = "jwt_token"
        )
        
        val registerResponse = robotGateway.registerRobot(registerRequest)
        if (registerResponse.success) {
            println("Robot registered successfully: ${registerResponse.nodeId}")
        } else {
            println("Failed to register robot: ${registerResponse.message}")
            return@runBlocking
        }
        
        // Send command to take off
        val takeOffCommand = Command(
            id = "CMD-001",
            type = CommandType.TAKE_OFF,
            parameters = mapOf("altitude" to "10"),
            timestamp = System.currentTimeMillis()
        )
        
        val commandResponse = robotGateway.sendCommand("drone_001", takeOffCommand)
        if (commandResponse.success) {
            println("Command executed successfully")
        } else {
            println("Failed to execute command: ${commandResponse.message}")
        }
        
        // Get robot status
        val statusResponse = robotGateway.getRobotStatus("drone_001")
        println("Robot status: ${statusResponse.status}")
        println("Battery level: ${statusResponse.status.batteryLevel}")
        println("Location: ${statusResponse.status.location}")
    }
}
```

## Conclusion
Robot Gateway предоставляет универсальный интерфейс для подключения любых роботов к системе Nodeorb. Он поддерживает множество протоколов коммуникации, обеспечивает прозрачную интеграцию с верхними слоями и обеспечивает высокую доступность через автоматическое переключение протоколов при сбоях.