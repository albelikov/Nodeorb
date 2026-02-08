package com.autonomous.vehicles

import org.springframework.stereotype.Service

@Service
class AutonomousVehicles {

    private val connectedVehicles = mutableMapOf<String, VehicleStatus>()

    fun connectVehicle(vehicleId: String, status: VehicleStatus) {
        connectedVehicles[vehicleId] = status
    }

    fun disconnectVehicle(vehicleId: String) {
        connectedVehicles.remove(vehicleId)
    }

    fun sendCommand(vehicleId: String, command: VehicleCommand): Boolean {
        return connectedVehicles.containsKey(vehicleId)
    }

    fun getVehicleStatus(vehicleId: String): VehicleStatus? {
        return connectedVehicles[vehicleId]
    }

    fun listConnectedVehicles(): List<String> {
        return connectedVehicles.keys.toList()
    }

    fun getActiveVehicles(): List<String> {
        return connectedVehicles.filter { (_, status) -> status == VehicleStatus.ACTIVE }.keys.toList()
    }
}

data class VehicleCommand(
    val command: String,
    val parameters: Map<String, Any>
)

enum class VehicleStatus {
    ACTIVE, INACTIVE, ERROR, MAINTENANCE
}