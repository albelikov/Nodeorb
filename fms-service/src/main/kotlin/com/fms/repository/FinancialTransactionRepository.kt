package com.fms.repository

import com.fms.model.FinancialTransaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FinancialTransactionRepository : JpaRepository<FinancialTransaction, UUID> {
    fun findByTransactionType(transactionType: String): List<FinancialTransaction>
    fun findByReferenceIdAndReferenceType(referenceId: UUID, referenceType: String): List<FinancialTransaction>
}