package com.autonomous.data.repositories

import com.autonomous.data.entities.NodeProfile
import com.autonomous.data.entities.NodeStatus
import com.autonomous.data.entities.NodeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NodeProfileRepository : JpaRepository<NodeProfile, Long> {
    fun findByNodeId(nodeId: String): NodeProfile?
    fun findByStatus(status: NodeStatus): List<NodeProfile>
    fun findByType(type: NodeType): List<NodeProfile>
    fun findByIsActive(isActive: Boolean): List<NodeProfile>
}