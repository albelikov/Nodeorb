package com.autonomous.data.repositories

import com.autonomous.data.entities.A2ANegotiation
import com.autonomous.data.entities.NegotiationStatus
import com.autonomous.data.entities.ResourceType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface A2ANegotiationRepository : JpaRepository<A2ANegotiation, Long> {
    fun findByNegotiationId(negotiationId: String): A2ANegotiation?
    fun findByResourceType(resourceType: ResourceType): List<A2ANegotiation>
    fun findByStatus(status: NegotiationStatus): List<A2ANegotiation>
}