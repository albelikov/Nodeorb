package com.tms.repository

import com.tms.model.Shipment
import com.tms.model.ShipmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShipmentRepository : JpaRepository<Shipment, Long> {

    fun findByShipmentNumber(shipmentNumber: String): Shipment?

    fun findByOrderId(orderId: Long): List<Shipment>

    fun findByStatus(status: ShipmentStatus): List<Shipment>
}
