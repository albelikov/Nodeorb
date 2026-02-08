package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "a2a_negotiations")
data class A2ANegotiation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val negotiationId: String,
    val resourceType: ResourceType,
    val resourceId: String,

    val initiatorNodeId: String,

    @Column(columnDefinition = "jsonb")
    val participantNodeIds: List<String>,

    @Column(columnDefinition = "jsonb")
    val bids: List<Bid>,

    val winner: String?,

    val startedAt: Instant,
    val completedAt: Instant?,
    val status: NegotiationStatus
)

enum class ResourceType {
    CHARGING_STATION,
    LANDING_PAD,
    NARROW_PASSAGE,
    WAREHOUSE_SLOT,
    AIRSPACE_SLOT
}

data class Bid(
    val nodeId: String,
    val bidAmount: Double,
    val priority: Int,
    val timestamp: Instant
)

enum class NegotiationStatus {
    OPEN,
    CLOSED,
    ACCEPTED,
    REJECTED,
    TIMEOUT
}