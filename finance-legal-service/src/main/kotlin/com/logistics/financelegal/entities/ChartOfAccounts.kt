package com.logistics.financelegal.entities

import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

object ChartOfAccounts : Table("chart_of_accounts") {
    val id = long("id").autoIncrement()
    val accountNumber = varchar("account_number", 50)
    val accountName = varchar("account_name", 200)
    val accountType = varchar("account_type", 50)
    val description = varchar("description", 1000).nullable()
    val parentAccount = varchar("parent_account", 50).nullable()
    val currency = varchar("currency", 3).default("USD")
    val isActive = bool("is_active").default(true)
    val createdBy = varchar("created_by", 100)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
    
    override val primaryKey = PrimaryKey(id)
    index(isUnique = true, accountNumber)
}

data class Account(
    val id: Long?,
    val accountNumber: String,
    val accountName: String,
    val accountType: String,
    val description: String?,
    val parentAccount: String?,
    val currency: String,
    val isActive: Boolean,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)