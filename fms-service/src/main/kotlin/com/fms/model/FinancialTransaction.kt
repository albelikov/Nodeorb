package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "financial_transactions")
data class FinancialTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var transactionType: String = "",

    @Column(nullable = false)
    var amount: Double = 0.0,

    var currency: String = "USD",

    var description: String = "",

    var referenceId: UUID? = null,

    var referenceType: String? = null,

    @Column(nullable = false)
    var transactionDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)