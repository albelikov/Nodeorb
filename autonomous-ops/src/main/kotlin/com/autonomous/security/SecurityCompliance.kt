package com.autonomous.security

import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SecurityCompliance {

    private val auditLogs = mutableListOf<AuditEntry>()

    fun logAccess(userId: String, resource: String, action: String) {
        auditLogs.add(AuditEntry(
            timestamp = Instant.now(),
            userId = userId,
            resource = resource,
            action = action,
            success = true
        ))
    }

    fun logFailure(userId: String, resource: String, action: String, error: String) {
        auditLogs.add(AuditEntry(
            timestamp = Instant.now(),
            userId = userId,
            resource = resource,
            action = action,
            success = false,
            error = error
        ))
    }

    fun isCompliant(action: String): Boolean {
        // Simple compliance check
        return when (action) {
            "delete" -> false
            "update" -> true
            "read" -> true
            "create" -> true
            else -> true
        }
    }

    fun getAuditLogs(): List<AuditEntry> {
        return auditLogs.toList()
    }

    fun getLogsByUser(userId: String): List<AuditEntry> {
        return auditLogs.filter { it.userId == userId }
    }

    fun getLogsByResource(resource: String): List<AuditEntry> {
        return auditLogs.filter { it.resource == resource }
    }
}

data class AuditEntry(
    val timestamp: Instant,
    val userId: String,
    val resource: String,
    val action: String,
    val success: Boolean,
    val error: String = ""
)