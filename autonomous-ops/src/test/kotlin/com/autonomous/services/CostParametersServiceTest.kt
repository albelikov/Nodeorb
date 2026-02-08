package com.autonomous.services

import com.autonomous.data.entities.CostParameters
import com.autonomous.data.repositories.CostParametersRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.Instant

class CostParametersServiceTest {

    private val costParametersRepository = mockk<CostParametersRepository>()
    private val costParametersService = CostParametersService(costParametersRepository)

    @Test
    fun `should create cost parameters`() {
        // Given
        val costParameters = CostParameters(
            id = 1L,
            parameterId = "default",
            region = "global",
            validFrom = Instant.now(),
            validUntil = null,
            electricityPricePerKwh = 0.15,
            fuelPricePerLiter = 1.50,
            batterySwapCost = 25.00,
            operatorHourlyRate = 25.00,
            technicianHourlyRate = 40.00,
            supervisionCostPerMission = 15.00,
            penaltyPerMinuteDelay = 0.25,
            priorityMultiplier = mapOf("CRITICAL" to 2.0, "HIGH" to 1.5, "MEDIUM" to 1.0, "LOW" to 0.8),
            repairCostEstimate = 500.00,
            insurancePremium = 150.00,
            liabilityCostPerIncident = 10000.00,
            createdBy = "system",
            approvedBy = "system"
        )

        every { costParametersRepository.save(any()) } returns costParameters

        // When
        val result = costParametersService.createCostParameters(costParameters)

        // Then
        assertEquals(costParameters, result)
        verify(exactly = 1) { costParametersRepository.save(any()) }
    }

    @Test
    fun `should get cost parameters by id`() {
        // Given
        val parameterId = "default"
        val costParameters = CostParameters(
            id = 1L,
            parameterId = parameterId,
            region = "global",
            validFrom = Instant.now(),
            validUntil = null,
            electricityPricePerKwh = 0.15,
            fuelPricePerLiter = 1.50,
            batterySwapCost = 25.00,
            operatorHourlyRate = 25.00,
            technicianHourlyRate = 40.00,
            supervisionCostPerMission = 15.00,
            penaltyPerMinuteDelay = 0.25,
            priorityMultiplier = mapOf("CRITICAL" to 2.0, "HIGH" to 1.5, "MEDIUM" to 1.0, "LOW" to 0.8),
            repairCostEstimate = 500.00,
            insurancePremium = 150.00,
            liabilityCostPerIncident = 10000.00,
            createdBy = "system",
            approvedBy = "system"
        )

        every { costParametersRepository.findByParameterId(parameterId) } returns costParameters

        // When
        val result = costParametersService.getCostParameters(parameterId)

        // Then
        assertEquals(costParameters, result)
        verify(exactly = 1) { costParametersRepository.findByParameterId(parameterId) }
    }

    @Test
    fun `should return null when getting non-existent cost parameters`() {
        // Given
        val parameterId = "nonexistent"
        every { costParametersRepository.findByParameterId(parameterId) } returns null

        // When
        val result = costParametersService.getCostParameters(parameterId)

        // Then
        assertNull(result)
    }

    @Test
    fun `should get active cost parameters`() {
        // Given
        val now = Instant.now()
        val activeCostParams = CostParameters(
            id = 1L,
            parameterId = "active",
            region = "global",
            validFrom = now.minusSeconds(3600),
            validUntil = now.plusSeconds(3600),
            electricityPricePerKwh = 0.15,
            fuelPricePerLiter = 1.50,
            batterySwapCost = 25.00,
            operatorHourlyRate = 25.00,
            technicianHourlyRate = 40.00,
            supervisionCostPerMission = 15.00,
            penaltyPerMinuteDelay = 0.25,
            priorityMultiplier = mapOf("CRITICAL" to 2.0, "HIGH" to 1.5, "MEDIUM" to 1.0, "LOW" to 0.8),
            repairCostEstimate = 500.00,
            insurancePremium = 150.00,
            liabilityCostPerIncident = 10000.00,
            createdBy = "system",
            approvedBy = "system"
        )

        val inactiveCostParams = CostParameters(
            id = 2L,
            parameterId = "inactive",
            region = "global",
            validFrom = now.minusSeconds(7200),
            validUntil = now.minusSeconds(3600),
            electricityPricePerKwh = 0.15,
            fuelPricePerLiter = 1.50,
            batterySwapCost = 25.00,
            operatorHourlyRate = 25.00,
            technicianHourlyRate = 40.00,
            supervisionCostPerMission = 15.00,
            penaltyPerMinuteDelay = 0.25,
            priorityMultiplier = mapOf("CRITICAL" to 2.0, "HIGH" to 1.5, "MEDIUM" to 1.0, "LOW" to 0.8),
            repairCostEstimate = 500.00,
            insurancePremium = 150.00,
            liabilityCostPerIncident = 10000.00,
            createdBy = "system",
            approvedBy = "system"
        )

        every { costParametersRepository.findAll() } returns listOf(activeCostParams, inactiveCostParams)

        // When
        val result = costParametersService.getActiveCostParameters()

        // Then
        assertEquals(1, result.size)
        assertEquals(activeCostParams, result[0])
    }

    @Test
    fun `should get default cost parameters`() {
        // Given
        val defaultCostParams = CostParameters(
            id = 1L,
            parameterId = "default",
            region = "global",
            validFrom = Instant.now(),
            validUntil = null,
            electricityPricePerKwh = 0.15,
            fuelPricePerLiter = 1.50,
            batterySwapCost = 25.00,
            operatorHourlyRate = 25.00,
            technicianHourlyRate = 40.00,
            supervisionCostPerMission = 15.00,
            penaltyPerMinuteDelay = 0.25,
            priorityMultiplier = mapOf("CRITICAL" to 2.0, "HIGH" to 1.5, "MEDIUM" to 1.0, "LOW" to 0.8),
            repairCostEstimate = 500.00,
            insurancePremium = 150.00,
            liabilityCostPerIncident = 10000.00,
            createdBy = "system",
            approvedBy = "system"
        )

        every { costParametersRepository.findByParameterId("default") } returns defaultCostParams

        // When
        val result = costParametersService.getDefaultCostParameters()

        // Then
        assertEquals(defaultCostParams, result)
    }
}