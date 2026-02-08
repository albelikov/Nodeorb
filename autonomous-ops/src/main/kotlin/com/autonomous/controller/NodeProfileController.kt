package com.autonomous.controller

import com.autonomous.data.entities.*
import com.autonomous.services.NodeProfileService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/nodes")
class NodeProfileController(
    private val nodeProfileService: NodeProfileService
) {

    @PostMapping
    fun createNodeProfile(@RequestBody nodeProfile: NodeProfile): ResponseEntity<NodeProfile> {
        val createdNode = nodeProfileService.createNodeProfile(nodeProfile.copy(
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            status = NodeStatus.IDLE,
            batteryLevel = 1.0
        ))
        return ResponseEntity.ok(createdNode)
    }

    @GetMapping("/{nodeId}")
    fun getNodeProfile(@PathVariable nodeId: String): ResponseEntity<NodeProfile?> {
        val node = nodeProfileService.getNodeProfile(nodeId)
        return if (node != null) {
            ResponseEntity.ok(node)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{nodeId}")
    fun updateNodeProfile(@PathVariable nodeId: String, @RequestBody nodeProfile: NodeProfile): ResponseEntity<NodeProfile?> {
        val existingNode = nodeProfileService.getNodeProfile(nodeId)
        return if (existingNode != null) {
            val updatedNode = nodeProfileService.updateNodeProfile(nodeProfile.copy(
                id = existingNode.id,
                nodeId = existingNode.nodeId,
                updatedAt = Instant.now()
            ))
            ResponseEntity.ok(updatedNode)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{nodeId}")
    fun deleteNodeProfile(@PathVariable nodeId: String): ResponseEntity<Void> {
        nodeProfileService.deleteNodeProfile(nodeId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun listNodeProfiles(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) type: String?
    ): ResponseEntity<List<NodeProfile>> {
        val nodes = nodeProfileService.listNodeProfiles(status, type)
        return ResponseEntity.ok(nodes)
    }

    @GetMapping("/available")
    fun getAvailableNodes(): ResponseEntity<List<NodeProfile>> {
        val nodes = nodeProfileService.getAvailableNodes()
        return ResponseEntity.ok(nodes)
    }

    @GetMapping("/active")
    fun getActiveNodes(): ResponseEntity<List<NodeProfile>> {
        val nodes = nodeProfileService.getActiveNodes()
        return ResponseEntity.ok(nodes)
    }

    @PostMapping("/{nodeId}/assign/{missionId}")
    fun assignNodeToMission(
        @PathVariable nodeId: String,
        @PathVariable missionId: String
    ): ResponseEntity<Map<String, String>> {
        return if (nodeProfileService.assignNodeToMission(nodeId, missionId)) {
            ResponseEntity.ok(mapOf("message" to "Node assigned to mission"))
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Node cannot be assigned"))
        }
    }

    @PostMapping("/{nodeId}/release")
    fun releaseNodeFromMission(@PathVariable nodeId: String): ResponseEntity<Map<String, String>> {
        return if (nodeProfileService.releaseNodeFromMission(nodeId)) {
            ResponseEntity.ok(mapOf("message" to "Node released from mission"))
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Node cannot be released"))
        }
    }

    @PutMapping("/{nodeId}/location")
    fun updateNodeLocation(
        @PathVariable nodeId: String,
        @RequestBody location: GeoPoint
    ): ResponseEntity<Map<String, String>> {
        return if (nodeProfileService.updateNodeLocation(nodeId, location)) {
            ResponseEntity.ok(mapOf("message" to "Node location updated"))
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Node location cannot be updated"))
        }
    }

    @PutMapping("/{nodeId}/battery")
    fun updateBatteryLevel(
        @PathVariable nodeId: String,
        @RequestBody batteryLevel: Double
    ): ResponseEntity<Map<String, String>> {
        return if (nodeProfileService.updateBatteryLevel(nodeId, batteryLevel)) {
            ResponseEntity.ok(mapOf("message" to "Battery level updated"))
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Battery level cannot be updated"))
        }
    }
}