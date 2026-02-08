package com.logistics.customs.entities

import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime
import java.util.UUID

object GoodsClassifications : Table("goods_classifications") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val declarationId = uuid("declaration_id")
    val productCode = varchar("product_code", 100)
    val productName = varchar("product_name", 200)
    val quantity = integer("quantity")
    val unit = varchar("unit", 50)
    val value = decimal("value", 15, 2)
    val tnvedCode = varchar("tnved_code", 100)
    val countryOfOrigin = varchar("country_of_origin", 100)
    val countryOfDestination = varchar("country_of_destination", 100)
    val customsTariff = decimal("customs_tariff", 10, 4)
    val vatRate = decimal("vat_rate", 10, 4)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
    
    override val primaryKey = PrimaryKey(id)
    index(declarationId)
}

data class GoodsClassification(
    val id: UUID,
    val declarationId: UUID,
    val productCode: String,
    val productName: String,
    val quantity: Int,
    val unit: String,
    val value: java.math.BigDecimal,
    val tnvedCode: String,
    val countryOfOrigin: String,
    val countryOfDestination: String,
    val customsTariff: java.math.BigDecimal,
    val vatRate: java.math.BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)