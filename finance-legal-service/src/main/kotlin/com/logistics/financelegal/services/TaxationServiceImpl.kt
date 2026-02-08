package com.logistics.financelegal

import com.logistics.financelegal.entities.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class TaxationServiceImpl : TaxationServiceGrpcKt.TaxationServiceCoroutineImplBase() {
    override suspend fun createTaxDeclaration(request: CreateTaxDeclarationRequest): TaxDeclarationResponse {
        return transaction {
            val id = TaxDeclarations.insertAndGetId {
                it[declarationNumber] = request.declarationNumber
                it[taxType] = request.taxType
                it[taxPeriod] = request.taxPeriod
                it[year] = request.year
                it[period] = request.period
                it[totalAmount] = java.math.BigDecimal(request.totalAmount)
                it[currency] = request.currency.takeIf { it.isNotEmpty() } ?: "USD"
                it[status] = request.status.takeIf { it.isNotEmpty() } ?: "DRAFT"
                it[createdBy] = request.createdBy
                it[updatedAt] = LocalDateTime.now()
            }.value
            
            TaxDeclarationResponse.newBuilder()
                .setId(id)
                .setDeclarationNumber(request.declarationNumber)
                .setTaxType(request.taxType)
                .setTaxPeriod(request.taxPeriod)
                .setYear(request.year)
                .setPeriod(request.period)
                .setTotalAmount(request.totalAmount)
                .setCurrency(request.currency)
                .setStatus(request.status.takeIf { it.isNotEmpty() } ?: "DRAFT")
                .setCreatedBy(request.createdBy)
                .build()
        }
    }

    override suspend fun getTaxDeclaration(request: GetTaxDeclarationRequest): TaxDeclarationResponse {
        return transaction {
            TaxDeclarations.select { TaxDeclarations.id eq request.id }
                .singleOrNull()
                ?.let {
                    TaxDeclarationResponse.newBuilder()
                        .setId(it[TaxDeclarations.id].value)
                        .setDeclarationNumber(it[TaxDeclarations.declarationNumber])
                        .setTaxType(it[TaxDeclarations.taxType])
                        .setTaxPeriod(it[TaxDeclarations.taxPeriod])
                        .setYear(it[TaxDeclarations.year])
                        .setPeriod(it[TaxDeclarations.period])
                        .setTotalAmount(it[TaxDeclarations.totalAmount].toString())
                        .setCurrency(it[TaxDeclarations.currency])
                        .setStatus(it[TaxDeclarations.status])
                        .setFilingDate(it[TaxDeclarations.filingDate]?.toString() ?: "")
                        .setApprovedDate(it[TaxDeclarations.approvedDate]?.toString() ?: "")
                        .setDocumentUrl(it[TaxDeclarations.documentUrl] ?: "")
                        .setCreatedBy(it[TaxDeclarations.createdBy])
                        .build()
                } ?: TaxDeclarationResponse.getDefaultInstance()
        }
    }

    override suspend fun listTaxDeclarations(request: ListTaxDeclarationsRequest): ListTaxDeclarationsResponse {
        return transaction {
            val declarations = TaxDeclarations.selectAll()
                .orderBy(TaxDeclarations.createdAt to SortOrder.DESC)
                .map {
                    TaxDeclarationResponse.newBuilder()
                        .setId(it[TaxDeclarations.id].value)
                        .setDeclarationNumber(it[TaxDeclarations.declarationNumber])
                        .setTaxType(it[TaxDeclarations.taxType])
                        .setTaxPeriod(it[TaxDeclarations.taxPeriod])
                        .setYear(it[TaxDeclarations.year])
                        .setPeriod(it[TaxDeclarations.period])
                        .setTotalAmount(it[TaxDeclarations.totalAmount].toString())
                        .setCurrency(it[TaxDeclarations.currency])
                        .setStatus(it[TaxDeclarations.status])
                        .setFilingDate(it[TaxDeclarations.filingDate]?.toString() ?: "")
                        .setApprovedDate(it[TaxDeclarations.approvedDate]?.toString() ?: "")
                        .setDocumentUrl(it[TaxDeclarations.documentUrl] ?: "")
                        .setCreatedBy(it[TaxDeclarations.createdBy])
                        .build()
                }
            
            ListTaxDeclarationsResponse.newBuilder()
                .addAllDeclarations(declarations)
                .build()
        }
    }

    override suspend fun submitTaxDeclaration(request: SubmitTaxDeclarationRequest): TaxDeclarationResponse {
        return transaction {
            TaxDeclarations.update({ TaxDeclarations.id eq request.id }) {
                it[status] = "SUBMITTED"
                it[filingDate] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }
            
            getTaxDeclaration(GetTaxDeclarationRequest.newBuilder().setId(request.id).build())
        }
    }
}