package com.logistics.financelegal

import com.logistics.financelegal.entities.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class CustomsServiceImpl : CustomsServiceGrpcKt.CustomsServiceCoroutineImplBase() {
    override suspend fun createCustomsDocument(request: CreateCustomsDocumentRequest): CustomsDocumentResponse {
        return transaction {
            val id = CustomsDocuments.insertAndGetId {
                it[documentNumber] = request.documentNumber
                it[documentType] = request.documentType
                it[customsCode] = request.customsCode.takeIf { it.isNotEmpty() }
                it[goodsDescription] = request.goodsDescription.takeIf { it.isNotEmpty() }
                it[value] = request.value.takeIf { it.isNotEmpty() }?.let { java.math.BigDecimal(it) }
                it[currency] = request.currency.takeIf { it.isNotEmpty() } ?: "USD"
                it[customsDuty] = request.customsDuty.takeIf { it.isNotEmpty() }?.let { java.math.BigDecimal(it) }
                it[vatAmount] = request.vatAmount.takeIf { it.isNotEmpty() }?.let { java.math.BigDecimal(it) }
                it[totalTaxes] = request.totalTaxes.takeIf { it.isNotEmpty() }?.let { java.math.BigDecimal(it) }
                it[status] = request.status.takeIf { it.isNotEmpty() } ?: "PENDING"
                it[issueDate] = request.issueDate.takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) }
                it[expiryDate] = request.expiryDate.takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) }
                it[documentUrl] = request.documentUrl.takeIf { it.isNotEmpty() }
                it[createdBy] = request.createdBy
                it[updatedAt] = LocalDateTime.now()
            }.value
            
            CustomsDocumentResponse.newBuilder()
                .setId(id)
                .setDocumentNumber(request.documentNumber)
                .setDocumentType(request.documentType)
                .setCustomsCode(request.customsCode)
                .setGoodsDescription(request.goodsDescription)
                .setValue(request.value)
                .setCurrency(request.currency)
                .setCustomsDuty(request.customsDuty)
                .setVatAmount(request.vatAmount)
                .setTotalTaxes(request.totalTaxes)
                .setStatus(request.status.takeIf { it.isNotEmpty() } ?: "PENDING")
                .setIssueDate(request.issueDate)
                .setExpiryDate(request.expiryDate)
                .setDocumentUrl(request.documentUrl)
                .setCreatedBy(request.createdBy)
                .build()
        }
    }

    override suspend fun getCustomsDocument(request: GetCustomsDocumentRequest): CustomsDocumentResponse {
        return transaction {
            CustomsDocuments.select { CustomsDocuments.id eq request.id }
                .singleOrNull()
                ?.let {
                    CustomsDocumentResponse.newBuilder()
                        .setId(it[CustomsDocuments.id].value)
                        .setDocumentNumber(it[CustomsDocuments.documentNumber])
                        .setDocumentType(it[CustomsDocuments.documentType])
                        .setCustomsCode(it[CustomsDocuments.customsCode] ?: "")
                        .setGoodsDescription(it[CustomsDocuments.goodsDescription] ?: "")
                        .setValue(it[CustomsDocuments.value]?.toString() ?: "")
                        .setCurrency(it[CustomsDocuments.currency])
                        .setCustomsDuty(it[CustomsDocuments.customsDuty]?.toString() ?: "")
                        .setVatAmount(it[CustomsDocuments.vatAmount]?.toString() ?: "")
                        .setTotalTaxes(it[CustomsDocuments.totalTaxes]?.toString() ?: "")
                        .setStatus(it[CustomsDocuments.status])
                        .setIssueDate(it[CustomsDocuments.issueDate]?.toString() ?: "")
                        .setExpiryDate(it[CustomsDocuments.expiryDate]?.toString() ?: "")
                        .setDocumentUrl(it[CustomsDocuments.documentUrl] ?: "")
                        .setCreatedBy(it[CustomsDocuments.createdBy])
                        .build()
                } ?: CustomsDocumentResponse.getDefaultInstance()
        }
    }

    override suspend fun listCustomsDocuments(request: ListCustomsDocumentsRequest): ListCustomsDocumentsResponse {
        return transaction {
            val documents = CustomsDocuments.selectAll()
                .orderBy(CustomsDocuments.createdAt to SortOrder.DESC)
                .map {
                    CustomsDocumentResponse.newBuilder()
                        .setId(it[CustomsDocuments.id].value)
                        .setDocumentNumber(it[CustomsDocuments.documentNumber])
                        .setDocumentType(it[CustomsDocuments.documentType])
                        .setCustomsCode(it[CustomsDocuments.customsCode] ?: "")
                        .setGoodsDescription(it[CustomsDocuments.goodsDescription] ?: "")
                        .setValue(it[CustomsDocuments.value]?.toString() ?: "")
                        .setCurrency(it[CustomsDocuments.currency])
                        .setCustomsDuty(it[CustomsDocuments.customsDuty]?.toString() ?: "")
                        .setVatAmount(it[CustomsDocuments.vatAmount]?.toString() ?: "")
                        .setTotalTaxes(it[CustomsDocuments.totalTaxes]?.toString() ?: "")
                        .setStatus(it[CustomsDocuments.status])
                        .setIssueDate(it[CustomsDocuments.issueDate]?.toString() ?: "")
                        .setExpiryDate(it[CustomsDocuments.expiryDate]?.toString() ?: "")
                        .setDocumentUrl(it[CustomsDocuments.documentUrl] ?: "")
                        .setCreatedBy(it[CustomsDocuments.createdBy])
                        .build()
                }
            
            ListCustomsDocumentsResponse.newBuilder()
                .addAllDocuments(documents)
                .build()
        }
    }

    override suspend fun updateCustomsDocumentStatus(request: UpdateCustomsDocumentStatusRequest): CustomsDocumentResponse {
        return transaction {
            CustomsDocuments.update({ CustomsDocuments.id eq request.id }) {
                it[status] = request.status
                it[updatedAt] = LocalDateTime.now()
            }
            
            getCustomsDocument(GetCustomsDocumentRequest.newBuilder().setId(request.id).build())
        }
    }
}