package com.autonomous.services

import com.autonomous.data.entities.*
import com.autonomous.data.repositories.NodeProfileRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class NodeProfileService(
    private val nodeProfileRepository: NodeProfileRepository
) {

    fun createNodeProfile(nodeProfile: NodeProfile): NodeProfile {
        return nodeProfileRepository.save(nodeProfile)
    }

    fun getNodeProfile(nodeId: String): NodeProfile? {
        return nodeProfileRepository.findByNodeId(nodeId)
    }

    fun updateNodeProfile(nodeProfile: NodeProfile): NodeProfile {
        return nodeProfileRepository.save(nodeProfile)
    }

    fun deleteNodeProfile(nodeId: String) {
        nodeProfileRepository.findByNodeId(nodeId)?.let {
            nodeProfileRepository.delete(it)
        }
    }

    fun listNodeProfiles(status: String? = null, type: String? = null): List<NodeProfile> {
        return when {
            status != null && type != null -> nodeProfileRepository.findByStatus(NodeStatus.valueOf(status))
                .filter { it.type.toString() == type }
            status != null -> nodeProfileRepository.findByStatus(NodeStatus.valueOf(status))
            type != null -> nodeProfileRepository.findByType(NodeType.valueOf(type))
            else -> nodeProfileRepository.findByIsActive(true)
        }
    }

    fun getAvailableNodes(): List<NodeProfile> {
        return nodeProfileRepository.findByStatus(NodeStatus.IDLE)
    }

    fun getActiveNodes(): List<NodeProfile> {
        return nodeProfileRepository.findByStatus(NodeStatus.BUSY)
    }

    fun assignNodeToMission(nodeId: String, missionId: String): Boolean {
        val node = nodeProfileRepository.findByNodeId(nodeId)
        if (node != null && node.status == NodeStatus.IDLE) {
            nodeProfileRepository.save(node.copy(
                status = NodeStatus.BUSY,
                currentMissionId = missionId,
                updatedAt = Instant.now()
            ))
            return true
        }
        return false
    }

    fun releaseNodeFromMission(nodeId: String): Boolean {
        val node = nodeProfileRepository.findByNodeId(nodeId)
        if (node != null && node.status == NodeStatus.BUSY) {
            nodeProfileRepository.save(node.copy(
                status = NodeStatus.IDLE,
                currentMissionId = null,
                updatedAt = Instant.now()
            ))
            return true
        }
        return false
    }

    fun updateNodeLocation(nodeId: String, location: GeoPoint): Boolean {
        val node = nodeProfileRepository.findByNodeId(nodeId)
        if (node != null) {
            nodeProfileRepository.save(node.copy(
                currentLocation = location,
                updatedAt = Instant.now()
            ))
            return true
        }
        return false
    }

    fun updateBatteryLevel(nodeId: String, batteryLevel: Double): Boolean {
        val node = nodeProfileRepository.findByNodeId(nodeId)
        if (node != null) {
            nodeProfileRepository.save(node.copy(
                batteryLevel = batteryLevel,
                updatedAt = Instant.now()
            ))
            return true
        }
        return false
    }
}