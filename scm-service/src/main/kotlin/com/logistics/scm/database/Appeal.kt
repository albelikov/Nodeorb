package com.logistics.scm.database

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal

object Appeal : UUIDTable("appeals") {
    val recordHash = varchar("record_hash", 64).uniqueIndex()
    val justification = text("justification")
    val evidenceUrl = varchar("evidence_url", 500)
    val status = varchar("status", 20).default("PENDING") // PENDING, APPROVED, REJECTED
    val createdAt = datetime("created_at")
    val reviewedAt = datetime("reviewed_at").nullable()
    val reviewNotes = text("review_notes").nullable()
    
    init {
        index(true, recordHash)
        index(true, status)
        index(true, createdAt)
    }
}