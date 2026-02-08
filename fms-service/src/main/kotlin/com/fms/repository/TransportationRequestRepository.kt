package com.fms.repository

import com.fms.model.TransportationRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TransportationRequestRepository : JpaRepository<TransportationRequest, UUID> {
    fun findByCustomerId(customerId: UUID): List<TransportationRequest>
    fun findByStatus(status: String): List<TransportationRequest>
    fun findByPriority(priority: String): List<TransportationRequest>
    fun findByRequestType(requestType: String): List<TransportationRequest>
}