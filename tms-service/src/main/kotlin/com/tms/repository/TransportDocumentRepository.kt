package com.tms.repository

import com.tms.model.TransportDocument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransportDocumentRepository : JpaRepository<TransportDocument, Long> {

    fun findByShipmentIdAndDocumentType(shipmentId: Long, documentType: String): List<TransportDocument>

    fun findByDocumentNumber(documentNumber: String): TransportDocument?
}