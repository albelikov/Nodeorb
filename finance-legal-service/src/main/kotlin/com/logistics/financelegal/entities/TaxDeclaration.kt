package com.logistics.financelegal.entities

import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

object TaxDeclarations : Table("tax_declarations") {
    val id = long("id").autoIncrement()
    val declarationNumber = varchar("declaration_number", 100)
    val taxType = varchar("tax_type", 100)
    val taxPeriod = varchar("tax_period", 50)
    val year = integer("year")
    val period = varchar("period", 20)
    val totalAmount = decimal("total_amount", 15, 2)
    val currency = varchar("currency", 3).default("USD")
    val status = varchar("status", 50)
    val filingDate = datetime("filing_date").nullable()
    val approvedDate = datetime("approved_date").nullable()
    val documentUrl = varchar("document_url", 500).nullable()
    val createdBy = varchar("created_by", 100)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
    
    override val primaryKey = PrimaryKey(id)
    index(isUnique = true, declarationNumber)
}

data class TaxDeclaration(
    val id: Long?,
    val declarationNumber: String,
    val taxType: String,
    val taxPeriod: String,
    val year: Int,
    val period: String,
    val totalAmount: java.math.BigDecimal,
    val currency: String,
    val status: String,
    val filingDate: LocalDateTime?,
    val approvedDate: LocalDateTime?,
    val documentUrl: String?,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)