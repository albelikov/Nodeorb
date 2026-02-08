package com.logistics.customs.entities

import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime
import java.util.UUID

object CustomsPayments : Table("customs_payments") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val declarationId = uuid("declaration_id")
    val paymentNumber = varchar("payment_number", 100)
    val amount = decimal("amount", 15, 2)
    val paymentDate = datetime("payment_date").default(LocalDateTime.now())
    val paymentMethod = varchar("payment_method", 100)
    val status = varchar("status", 50)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
    
    override val primaryKey = PrimaryKey(id)
    index(declarationId)
    index(isUnique = true, paymentNumber)
}

data class CustomsPayment(
    val id: UUID,
    val declarationId: UUID,
    val paymentNumber: String,
    val amount: java.math.BigDecimal,
    val paymentDate: LocalDateTime,
    val paymentMethod: String,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)