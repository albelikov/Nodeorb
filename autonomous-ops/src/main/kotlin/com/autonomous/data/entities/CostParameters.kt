package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "cost_parameters")
data class CostParameters(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val parameterId: String,
    val region: String,
    val validFrom: Instant,
    val validUntil: Instant?,

    val electricityPricePerKwh: Double,
    val fuelPricePerLiter: Double?,
    val batterySwapCost: Double?,

    val operatorHourlyRate: Double,
    val technicianHourlyRate: Double,
    val supervisionCostPerMission: Double,

    val penaltyPerMinuteDelay: Double,

    @Column(columnDefinition = "jsonb")
    val priorityMultiplier: Map<String, Double>,

    val repairCostEstimate: Double,
    val insurancePremium: Double,
    val liabilityCostPerIncident: Double,

    val createdBy: String,
    val approvedBy: String?
)