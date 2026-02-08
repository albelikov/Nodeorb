package com.autonomous.services

import com.autonomous.data.entities.*
import com.autonomous.data.repositories.MissionRepository
import com.autonomous.data.repositories.MissionPlanRepository
import com.autonomous.data.repositories.MissionExecutionStatusRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.Instant

class MissionServiceTest {

    private val missionRepository = mockk<MissionRepository>()
    private val missionPlanRepository = mockk<MissionPlanRepository>()
    private val missionExecutionStatusRepository = mockk<MissionExecutionStatusRepository>()

    private val missionService = MissionService(
        missionRepository,
        missionPlanRepository,
        missionExecutionStatusRepository
    )

    @Test
    fun `should create mission`() {
        // Given
        val mission = Mission(
            id = 1L,
            missionId = "MISSION-001",
            type = MissionType.DELIVERY,
            priority = Priority.MEDIUM,
            status = MissionStatus.PENDING,
            origin = GeoPoint(51.5074, -0.1278),
            destination = GeoPoint(48.8566, 2.3522),
            waypoints = emptyList(),
            payload = PayloadSpec(
                weightKg = 5.0,
                dimensions = Dimensions(1.0, 0.5, 0.3),
                type = "package",
                handlingInstructions = "Fragile"
            ),
            constraints = MissionConstraints(
                maxDuration = 3600000,
                maxCost = 100.0,
                requiredSensors = emptyList(),
                forbiddenAreas = emptyList()
            ),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            scheduledStartTime = null,
            actualStartTime = null,
            completedAt = null,
            assignedNodeId = "NODE-001",
            createdBy = "user1",
            approvedBy = null,
            tasks = emptyList()
        )

        every { missionRepository.save(any()) } returns mission

        // When
        val result = missionService.createMission(mission)

        // Then
        assertEquals(mission, result)
        verify(exactly = 1) { missionRepository.save(any()) }
    }

    @Test
    fun `should get mission by id`() {
        // Given
        val missionId = "MISSION-001"
        val mission = Mission(
            id = 1L,
            missionId = missionId,
            type = MissionType.DELIVERY,
            priority = Priority.MEDIUM,
            status = MissionStatus.PENDING,
            origin = GeoPoint(51.5074, -0.1278),
            destination = GeoPoint(48.8566, 2.3522),
            waypoints = emptyList(),
            payload = PayloadSpec(
                weightKg = 5.0,
                dimensions = Dimensions(1.0, 0.5, 0.3),
                type = "package",
                handlingInstructions = "Fragile"
            ),
            constraints = MissionConstraints(
                maxDuration = 3600000,
                maxCost = 100.0,
                requiredSensors = emptyList(),
                forbiddenAreas = emptyList()
            ),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            scheduledStartTime = null,
            actualStartTime = null,
            completedAt = null,
            assignedNodeId = "NODE-001",
            createdBy = "user1",
            approvedBy = null,
            tasks = emptyList()
        )

        every { missionRepository.findByMissionId(missionId) } returns mission

        // When
        val result = missionService.getMission(missionId)

        // Then
        assertEquals(mission, result)
        verify(exactly = 1) { missionRepository.findByMissionId(missionId) }
    }

    @Test
    fun `should return null when getting non-existent mission`() {
        // Given
        val missionId = "MISSION-999"
        every { missionRepository.findByMissionId(missionId) } returns null

        // When
        val result = missionService.getMission(missionId)

        // Then
        assertNull(result)
    }

    @Test
    fun `should update mission`() {
        // Given
        val mission = Mission(
            id = 1L,
            missionId = "MISSION-001",
            type = MissionType.DELIVERY,
            priority = Priority.MEDIUM,
            status = MissionStatus.PENDING,
            origin = GeoPoint(51.5074, -0.1278),
            destination = GeoPoint(48.8566, 2.3522),
            waypoints = emptyList(),
            payload = PayloadSpec(
                weightKg = 5.0,
                dimensions = Dimensions(1.0, 0.5, 0.3),
                type = "package",
                handlingInstructions = "Fragile"
            ),
            constraints = MissionConstraints(
                maxDuration = 3600000,
                maxCost = 100.0,
                requiredSensors = emptyList(),
                forbiddenAreas = emptyList()
            ),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            scheduledStartTime = null,
            actualStartTime = null,
            completedAt = null,
            assignedNodeId = "NODE-001",
            createdBy = "user1",
            approvedBy = null,
            tasks = emptyList()
        )

        every { missionRepository.save(any()) } returns mission

        // When
        val result = missionService.updateMission(mission)

        // Then
        assertEquals(mission, result)
        verify(exactly = 1) { missionRepository.save(any()) }
    }
}