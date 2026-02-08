package com.logistics.financelegal.entities

import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

object AccountingEntries : Table("accounting_entries") {
    val id = long("id").autoIncrement()
    val entryDate = datetime("entry_date")
    val description = varchar("description", 500)
    val debitAccount = varchar("debit_account", 50)
    val creditAccount = varchar("credit_account", 50)
    val amount = decimal("amount", 15, 2)
    val currency = varchar("currency", 3)
    val exchangeRate = decimal("exchange_rate", 10, 4).nullable()
    val referenceNumber = varchar("reference_number", 100).nullable()
    val transactionType = varchar("transaction_type", 50)
    val createdBy = varchar("created_by", 100)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    
    override val primaryKey = PrimaryKey(id)
}

data class AccountingEntry(
    val id: Long?,
    val entryDate: LocalDateTime,
    val description: String,
    val debitAccount: String,
    val creditAccount: String,
    val amount: java.math.BigDecimal,
    val currency: String,
    val exchangeRate: java.math.BigDecimal?,
    val referenceNumber: String?,
    val transactionType: String,
    val createdBy: String,
    val createdAt: LocalDateTime
)