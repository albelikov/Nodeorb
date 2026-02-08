package com.autonomous.api.rest

import com.autonomous.maintenance.PredictiveMaintenance
import com.autonomous.maintenance.SensorReading
import com.autonomous.maintenance.MaintenancePrediction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/predictive/maintenance")
class PredictiveMaintenanceController {

    @Autowired
    private lateinit var predictiveMaintenance: PredictiveMaintenance

    @PostMapping
    fun getPredictiveMaintenance(@RequestBody request: PredictiveMaintenanceRequest): MaintenancePrediction {
        val sensorData = request.sensorData.map {
            SensorReading(it.timestamp, it.temperature, it.vibration, it.pressure)
        }

        return predictiveMaintenance.predictFailure(request.vehicleId, sensorData)
    }
}

data class PredictiveMaintenanceRequest(
    val vehicleId: String,
    val sensorData: List<SensorReadingDTO>
)

data class SensorReadingDTO(
    val timestamp: Long,
    val temperature: Double,
    val vibration: Double,
    val pressure: Double
)