package com.logistics.financelegal.entities

import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

object CustomsDocuments : Table("customs_documents") {
    val id = long("id").autoIncrement()
    val documentNumber = varchar("document_number", 100)
    val documentType = varchar("document_type", 100)
    val customsCode = varchar("customs_code", 50).nullable()
    val goodsDescription = text("goods_description").nullable()
    val value = decimal("value", 15, 2).nullable()
    val currency = varchar("currency", 3).default("USD")
    val customsDuty = decimal("customs_duty", 15, 2).nullable()
    val vatAmount = decimal("vat_amount", 15, 2).nullable()
    val totalTaxes = decimal("total_taxes", 15, 2).nullable()
    val status = varchar("status", 50)
    val issueDate = datetime("issue_date").nullable()
    val expiryDate = datetime("expiry_date").nullable()
    val documentUrl = varchar("document_url", 500).nullable()
    val createdBy = varchar("created_by", 100)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
    
    override val primaryKey = PrimaryKey(id)
    index(isUnique = true, documentNumber)
}

data class CustomsDocument(
    val id: Long?,
    val documentNumber: String,
    val documentType: String,
    val customsCode: String?,
    val goodsDescription: String?,
    val value: java.math.BigDecimal?,
    val currency: String,
    val customsDuty: java.math.BigDecimal?,
    val vatAmount: java.math.BigDecimal?,
    val totalTaxes: java.math.BigDecimal?,
    val status: String,
    val issueDate: LocalDateTime?,
    val expiryDate: LocalDateTime?,
    val documentUrl: String?,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)