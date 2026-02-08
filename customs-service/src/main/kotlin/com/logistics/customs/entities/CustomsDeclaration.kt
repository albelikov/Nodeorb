package com.logistics.customs.entities

import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime
import java.util.UUID

object CustomsDeclarations : Table("customs_declarations") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val declarationNumber = varchar("declaration_number", 100)
    val shipperId = uuid("shipper_id")
    val consigneeId = uuid("consignee_id")
    val orderId = uuid("order_id")
    val goods = text("goods")
    val totalValue = decimal("total_value", 15, 2)
    val customsProcedure = varchar("customs_procedure", 100)
    val status = varchar("status", 50)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
    
    override val primaryKey = PrimaryKey(id)
    index(isUnique = true, declarationNumber)
}

data class CustomsDeclaration(
    val id: UUID,
    val declarationNumber: String,
    val shipperId: UUID,
    val consigneeId: UUID,
    val orderId: UUID,
    val goods: String,
    val totalValue: java.math.BigDecimal,
    val customsProcedure: String,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)