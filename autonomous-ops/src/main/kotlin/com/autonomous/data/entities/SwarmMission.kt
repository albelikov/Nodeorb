package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "swarm_missions")
data class SwarmMission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val swarmMissionId: String,

    @Column(columnDefinition = "jsonb")
    val participatingNodes: List<String>,

    val coordinationType: CoordinationType,
    val leaderNodeId: String?,

    val sharedWorldModel: String,

    val status: SwarmMissionStatus,

    val createdAt: Instant,
    val completedAt: Instant?
)

enum class CoordinationType {
    LEADER_FOLLOWER,
    DECENTRALIZED,
    HIERARCHICAL
}

enum class SwarmMissionStatus {
    FORMING,
    COORDINATING,
    EXECUTING,
    COMPLETED,
    FAILED
}