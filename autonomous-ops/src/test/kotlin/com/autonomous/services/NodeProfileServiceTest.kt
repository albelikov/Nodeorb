package com.autonomous.services

import com.autonomous.data.entities.*
import com.autonomous.data.repositories.NodeProfileRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.Instant

class NodeProfileServiceTest {

    private val nodeProfileRepository = mockk<NodeProfileRepository>()
    private val nodeProfileService = NodeProfileService(nodeProfileRepository)

    @Test
    fun `should create node profile`() {
        // Given
        val nodeProfile = NodeProfile(
            id = 1L,
            nodeId = "NODE-001",
            name = "Delivery Drone",
            type = NodeType.UAV,
            model = "DJI Mavic 3",
            status = NodeStatus.IDLE,
            capabilities = NodeCapabilities(
                maxSpeed = 60.0,
                maxPayload = 2.0,
                maxRange = 15.0,
                maxAltitude = 120.0,
                sensors = listOf(SensorType.RGB_CAMERA, SensorType.GPS, SensorType.IMU),
                communicationProtocols = listOf("MQTT", "TCP/IP"),
                autonomyLevel = AutonomyLevel.LEVEL_3
            ),
            hardwareSpecs = HardwareSpecs(
                batteryCapacity = 3000.0,
                batteryType = "LiPo",
                chargingTime = 60.0,
                processorModel = "Snapdragon 865",
                ramSize = 8,
                storageSize = 64
            ),
            currentLocation = GeoPoint(51.5074, -0.1278),
            batteryLevel = 1.0,
            currentMissionId = null,
            operationalHours = 100,
            lastMaintenanceDate = Instant.now().minusSeconds(86400),
            nextMaintenanceDate = Instant.now().plusSeconds(86400 * 30),
            costParameters = NodeCostParameters(
                purchasePrice = 2500.0,
                depreciationRate = 0.1,
                energyCostPerKm = 0.5,
                maintenanceCostPerHour = 15.0,
                insuranceCostPerMonth = 20.0,
                operatorCostPerHour = 25.0
            ),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = true
        )

        every { nodeProfileRepository.save(any()) } returns nodeProfile

        // When
        val result = nodeProfileService.createNodeProfile(nodeProfile)

        // Then
        assertEquals(nodeProfile, result)
        verify(exactly = 1) { nodeProfileRepository.save(any()) }
    }

    @Test
    fun `should get node profile by id`() {
        // Given
        val nodeId = "NODE-001"
        val nodeProfile = NodeProfile(
            id = 1L,
            nodeId = nodeId,
            name = "Delivery Drone",
            type = NodeType.UAV,
            model = "DJI Mavic 3",
            status = NodeStatus.IDLE,
            capabilities = NodeCapabilities(
                maxSpeed = 60.0,
                maxPayload = 2.0,
                maxRange = 15.0,
                maxAltitude = 120.0,
                sensors = listOf(SensorType.RGB_CAMERA, SensorType.GPS, SensorType.IMU),
                communicationProtocols = listOf("MQTT", "TCP/IP"),
                autonomyLevel = AutonomyLevel.LEVEL_3
            ),
            hardwareSpecs = HardwareSpecs(
                batteryCapacity = 3000.0,
                batteryType = "LiPo",
                chargingTime = 60.0,
                processorModel = "Snapdragon 865",
                ramSize = 8,
                storageSize = 64
            ),
            currentLocation = GeoPoint(51.5074, -0.1278),
            batteryLevel = 1.0,
            currentMissionId = null,
            operationalHours = 100,
            lastMaintenanceDate = Instant.now().minusSeconds(86400),
            nextMaintenanceDate = Instant.now().plusSeconds(86400 * 30),
            costParameters = NodeCostParameters(
                purchasePrice = 2500.0,
                depreciationRate = 0.1,
                energyCostPerKm = 0.5,
                maintenanceCostPerHour = 15.0,
                insuranceCostPerMonth = 20.0,
                operatorCostPerHour = 25.0
            ),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = true
        )

        every { nodeProfileRepository.findByNodeId(nodeId) } returns nodeProfile

        // When
        val result = nodeProfileService.getNodeProfile(nodeId)

        // Then
        assertEquals(nodeProfile, result)
        verify(exactly = 1) { nodeProfileRepository.findByNodeId(nodeId) }
    }

    @Test
    fun `should return null when getting non-existent node`() {
        // Given
        val nodeId = "NODE-999"
        every { nodeProfileRepository.findByNodeId(nodeId) } returns null

        // When
        val result = nodeProfileService.getNodeProfile(nodeId)

        // Then
        assertNull(result)
    }

    @Test
    fun `should assign node to mission`() {
        // Given
        val nodeId = "NODE-001"
        val missionId = "MISSION-001"
        val nodeProfile = NodeProfile(
            id = 1L,
            nodeId = nodeId,
            name = "Delivery Drone",
            type = NodeType.UAV,
            model = "DJI Mavic 3",
            status = NodeStatus.IDLE,
            capabilities = NodeCapabilities(
                maxSpeed = 60.0,
                maxPayload = 2.0,
                maxRange = 15.0,
                maxAltitude = 120.0,
                sensors = listOf(SensorType.RGB_CAMERA, SensorType.GPS, SensorType.IMU),
                communicationProtocols = listOf("MQTT", "TCP/IP"),
                autonomyLevel = AutonomyLevel.LEVEL_3
            ),
            hardwareSpecs = HardwareSpecs(
                batteryCapacity = 3000.0,
                batteryType = "LiPo",
                chargingTime = 60.0,
                processorModel = "Snapdragon 865",
                ramSize = 8,
                storageSize = 64
            ),
            currentLocation = GeoPoint(51.5074, -0.1278),
            batteryLevel = 1.0,
            currentMissionId = null,
            operationalHours = 100,
            lastMaintenanceDate = Instant.now().minusSeconds(86400),
            nextMaintenanceDate = Instant.now().plusSeconds(86400 * 30),
            costParameters = NodeCostParameters(
                purchasePrice = 2500.0,
                depreciationRate = 0.1,
                energyCostPerKm = 0.5,
                maintenanceCostPerHour = 15.0,
                insuranceCostPerMonth = 20.0,
                operatorCostPerHour = 25.0
            ),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = true
        )

        every { nodeProfileRepository.findByNodeId(nodeId) } returns nodeProfile
        every { nodeProfileRepository.save(any()) } returns nodeProfile.copy(
            status = NodeStatus.BUSY,
            currentMissionId = missionId
        )

        // When
        val result = nodeProfileService.assignNodeToMission(nodeId, missionId)

        // Then
        assertTrue(result)
        verify(exactly = 1) { nodeProfileRepository.findByNodeId(nodeId) }
        verify(exactly = 1) { nodeProfileRepository.save(any()) }
    }

    @Test
    fun `should not assign already busy node`() {
        // Given
        val nodeId = "NODE-001"
        val missionId = "MISSION-001"
        val nodeProfile = NodeProfile(
            id = 1L,
            nodeId = nodeId,
            name = "Delivery Drone",
            type = NodeType.UAV,
            model = "DJI Mavic 3",
            status = NodeStatus.BUSY,
            capabilities = NodeCapabilities(
                maxSpeed = 60.0,
                maxPayload = 2.0,
                maxRange = 15.0,
                maxAltitude = 120.0,
                sensors = listOf(SensorType.RGB_CAMERA, SensorType.GPS, SensorType.IMU),
                communicationProtocols = listOf("MQTT", "TCP/IP"),
                autonomyLevel = AutonomyLevel.LEVEL_3
            ),
            hardwareSpecs = HardwareSpecs(
                batteryCapacity = 3000.0,
                batteryType = "LiPo",
                chargingTime = 60.0,
                processorModel = "Snapdragon 865",
                ramSize = 8,
                storageSize = 64
            ),
            currentLocation = GeoPoint(51.5074, -0.1278),
            batteryLevel = 1.0,
            currentMissionId = "MISSION-002",
            operationalHours = 100,
            lastMaintenanceDate = Instant.now().minusSeconds(86400),
            nextMaintenanceDate = Instant.now().plusSeconds(86400 * 30),
            costParameters = NodeCostParameters(
                purchasePrice = 2500.0,
                depreciationRate = 0.1,
                energyCostPerKm = 0.5,
                maintenanceCostPerHour = 15.0,
                insuranceCostPerMonth = 20.0,
                operatorCostPerHour = 25.0
            ),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = true
        )

        every { nodeProfileRepository.findByNodeId(nodeId) } returns nodeProfile

        // When
        val result = nodeProfileService.assignNodeToMission(nodeId, missionId)

        // Then
        assertFalse(result)
    }
}