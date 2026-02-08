package com.autonomous.maintenance

import org.springframework.stereotype.Service

@Service
class PredictiveMaintenance {

    fun predictFailure(vehicleId: String, sensorData: List<SensorReading>): MaintenancePrediction {
        // Simple prediction based on sensor data
        val averageTemperature = sensorData.map { it.temperature }.average()
        val averageVibration = sensorData.map { it.vibration }.average()
        val averagePressure = sensorData.map { it.pressure }.average()

        val failureProbability = calculateFailureProbability(averageTemperature, averageVibration, averagePressure)
        val nextMaintenance = calculateNextMaintenance(failureProbability)

        return MaintenancePrediction(
            vehicleId = vehicleId,
            failureProbability = failureProbability,
            nextMaintenance = nextMaintenance,
            recommendedAction = getRecommendedAction(failureProbability)
        )
    }

    private fun calculateFailureProbability(temperature: Double, vibration: Double, pressure: Double): Double {
        var probability = 0.0

        if (temperature > 100) probability += 0.4
        if (vibration > 5) probability += 0.3
        if (pressure > 10) probability += 0.3

        return Math.min(probability, 1.0)
    }

    private fun calculateNextMaintenance(failureProbability: Double): String {
        return when {
            failureProbability > 0.7 -> "Within 24 hours"
            failureProbability > 0.5 -> "Within 7 days"
            failureProbability > 0.3 -> "Within 30 days"
            else -> "Within 90 days"
        }
    }

    private fun getRecommendedAction(failureProbability: Double): String {
        return when {
            failureProbability > 0.7 -> "Immediate maintenance required"
            failureProbability > 0.5 -> "Schedule maintenance soon"
            failureProbability > 0.3 -> "Maintenance recommended"
            else -> "Maintenance not needed"
        }
    }
}

data class SensorReading(
    val timestamp: Long,
    val temperature: Double,
    val vibration: Double,
    val pressure: Double
)

data class MaintenancePrediction(
    val vehicleId: String,
    val failureProbability: Double,
    val nextMaintenance: String,
    val recommendedAction: String
)