package com.fms.repository

import com.fms.model.VehicleDocument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface VehicleDocumentRepository : JpaRepository<VehicleDocument, UUID> {
    fun findByVehicleId(vehicleId: UUID): List<VehicleDocument>
    fun findByVehicleIdAndDocumentType(vehicleId: UUID, documentType: String): List<VehicleDocument>
}