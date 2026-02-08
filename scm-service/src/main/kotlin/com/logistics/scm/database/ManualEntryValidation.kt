package com.logistics.scm.database

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal

object ManualEntryValidation : UUIDTable("manual_entry_validation") {
    val orderId = varchar("order_id", 255)
    val inputValue = decimal("input_value", 15, 2)
    val deviation = double("deviation")
    val verdict = varchar("verdict", 20) // GREEN, YELLOW, RED
    val previousHash = varchar("previous_hash", 64).nullable()
    val currentHash = varchar("current_hash", 64)
    val createdAt = datetime("created_at")
    
    init {
        index(true, orderId)
        index(true, verdict)
        index(true, createdAt)
    }
}