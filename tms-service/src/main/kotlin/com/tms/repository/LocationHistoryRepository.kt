package com.tms.repository

import com.tms.model.LocationHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LocationHistoryRepository : JpaRepository<LocationHistory, Long> {

    fun findByShipmentId(shipmentId: Long): List<LocationHistory>

    fun findByShipmentIdOrderByTimestampDesc(shipmentId: Long): List<LocationHistory>

    fun findByVehicleId(vehicleId: Long): List<LocationHistory>

    fun findByVehicleIdOrderByTimestampDesc(vehicleId: Long): List<LocationHistory>
}