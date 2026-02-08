package com.logistics.financelegal.entities

import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

object InsurancePolicies : Table("insurance_policies") {
    val id = long("id").autoIncrement()
    val policyNumber = varchar("policy_number", 100)
    val policyType = varchar("policy_type", 100)
    val insurer = varchar("insurer", 200)
    val insured = varchar("insured", 200)
    val startDate = datetime("start_date")
    val endDate = datetime("end_date")
    val coverageAmount = decimal("coverage_amount", 15, 2)
    val premiumAmount = decimal("premium_amount", 15, 2)
    val currency = varchar("currency", 3).default("USD")
    val coverageDetails = text("coverage_details").nullable()
    val status = varchar("status", 50)
    val documentUrl = varchar("document_url", 500).nullable()
    val createdBy = varchar("created_by", 100)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
    
    override val primaryKey = PrimaryKey(id)
    index(isUnique = true, policyNumber)
}

data class InsurancePolicy(
    val id: Long?,
    val policyNumber: String,
    val policyType: String,
    val insurer: String,
    val insured: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val coverageAmount: java.math.BigDecimal,
    val premiumAmount: java.math.BigDecimal,
    val currency: String,
    val coverageDetails: String?,
    val status: String,
    val documentUrl: String?,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)