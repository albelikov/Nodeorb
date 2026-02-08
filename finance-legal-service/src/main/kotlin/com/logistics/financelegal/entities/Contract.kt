package com.logistics.financelegal.entities

import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

object Contracts : Table("contracts") {
    val id = long("id").autoIncrement()
    val contractNumber = varchar("contract_number", 100)
    val contractName = varchar("contract_name", 200)
    val counterparty = varchar("counterparty", 200)
    val counterpartyId = long("counterparty_id").nullable()
    val startDate = datetime("start_date")
    val endDate = datetime("end_date").nullable()
    val contractType = varchar("contract_type", 100)
    val status = varchar("status", 50)
    val totalAmount = decimal("total_amount", 15, 2).nullable()
    val currency = varchar("currency", 3).default("USD")
    val terms = text("terms").nullable()
    val signedDate = datetime("signed_date").nullable()
    val signedBy = varchar("signed_by", 100).nullable()
    val documentUrl = varchar("document_url", 500).nullable()
    val createdBy = varchar("created_by", 100)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
    
    override val primaryKey = PrimaryKey(id)
    index(isUnique = true, contractNumber)
}

data class Contract(
    val id: Long?,
    val contractNumber: String,
    val contractName: String,
    val counterparty: String,
    val counterpartyId: Long?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime?,
    val contractType: String,
    val status: String,
    val totalAmount: java.math.BigDecimal?,
    val currency: String,
    val terms: String?,
    val signedDate: LocalDateTime?,
    val signedBy: String?,
    val documentUrl: String?,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)