package com.logistics.customs.services

import com.logistics.customs.*
import com.logistics.customs.entities.*
import io.grpc.stub.StreamObserver
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

class CustomsServiceImpl : CustomsServiceGrpcKt.CustomsServiceCoroutineImplBase() {
    // Declaration operations
    override suspend fun createDeclaration(request: CreateDeclarationRequest): DeclarationResponse {
        return transaction {
            val id = CustomsDeclarations.insertAndGetId {
                it[declarationNumber] = request.declarationNumber
                it[shipperId] = UUID.fromString(request.shipperId)
                it[consigneeId] = UUID.fromString(request.consigneeId)
                it[orderId] = UUID.fromString(request.orderId)
                it[goods] = request.goods
                it[totalValue] = java.math.BigDecimal(request.totalValue)
                it[customsProcedure] = request.customsProcedure
                it[status] = request.status.takeIf { it.isNotEmpty() } ?: "PENDING"
            }
            
            DeclarationResponse.newBuilder()
                .setId(id.toString())
                .setDeclarationNumber(request.declarationNumber)
                .setShipperId(request.shipperId)
                .setConsigneeId(request.consigneeId)
                .setOrderId(request.orderId)
                .setGoods(request.goods)
                .setTotalValue(request.totalValue)
                .setCustomsProcedure(request.customsProcedure)
                .setStatus(request.status.takeIf { it.isNotEmpty() } ?: "PENDING")
                .setCreatedAt(LocalDateTime.now().toString())
                .setUpdatedAt(LocalDateTime.now().toString())
                .build()
        }
    }

    override suspend fun getDeclaration(request: GetDeclarationRequest): DeclarationResponse {
        return transaction {
            CustomsDeclarations.select { CustomsDeclarations.id eq UUID.fromString(request.id) }
                .singleOrNull()
                ?.let {
                    DeclarationResponse.newBuilder()
                        .setId(it[CustomsDeclarations.id].toString())
                        .setDeclarationNumber(it[CustomsDeclarations.declarationNumber])
                        .setShipperId(it[CustomsDeclarations.shipperId].toString())
                        .setConsigneeId(it[CustomsDeclarations.consigneeId].toString())
                        .setOrderId(it[CustomsDeclarations.orderId].toString())
                        .setGoods(it[CustomsDeclarations.goods])
                        .setTotalValue(it[CustomsDeclarations.totalValue].toString())
                        .setCustomsProcedure(it[CustomsDeclarations.customsProcedure])
                        .setStatus(it[CustomsDeclarations.status])
                        .setCreatedAt(it[CustomsDeclarations.createdAt].toString())
                        .setUpdatedAt(it[CustomsDeclarations.updatedAt].toString())
                        .build()
                } ?: DeclarationResponse.getDefaultInstance()
        }
    }

    override suspend fun updateDeclaration(request: UpdateDeclarationRequest): DeclarationResponse {
        return transaction {
            CustomsDeclarations.update({ CustomsDeclarations.id eq UUID.fromString(request.id) }) {
                it[declarationNumber] = request.declarationNumber.takeIf { it.isNotEmpty() } ?: ""
                it[shipperId] = UUID.fromString(request.shipperId.takeIf { it.isNotEmpty() } ?: "00000000-0000-0000-0000-000000000000")
                it[consigneeId] = UUID.fromString(request.consigneeId.takeIf { it.isNotEmpty() } ?: "00000000-0000-0000-0000-000000000000")
                it[orderId] = UUID.fromString(request.orderId.takeIf { it.isNotEmpty() } ?: "00000000-0000-0000-0000-000000000000")
                it[goods] = request.goods.takeIf { it.isNotEmpty() } ?: ""
                it[totalValue] = request.totalValue.takeIf { it.isNotEmpty() }?.let { java.math.BigDecimal(it) } ?: java.math.BigDecimal.ZERO
                it[customsProcedure] = request.customsProcedure.takeIf { it.isNotEmpty() } ?: ""
                it[status] = request.status.takeIf { it.isNotEmpty() } ?: "PENDING"
                it[updatedAt] = LocalDateTime.now()
            }
            
            getDeclaration(GetDeclarationRequest.newBuilder().setId(request.id).build())
        }
    }

    override suspend fun listDeclarations(request: ListDeclarationsRequest): ListDeclarationsResponse {
        return transaction {
            val page = request.pageNumber.takeIf { it > 0 } ?: 1
            val pageSize = request.pageSize.takeIf { it > 0 } ?: 10
            val offset = (page - 1) * pageSize
            
            val declarations = CustomsDeclarations.selectAll()
                .limit(pageSize, offset.toLong())
                .map {
                    DeclarationResponse.newBuilder()
                        .setId(it[CustomsDeclarations.id].toString())
                        .setDeclarationNumber(it[CustomsDeclarations.declarationNumber])
                        .setShipperId(it[CustomsDeclarations.shipperId].toString())
                        .setConsigneeId(it[CustomsDeclarations.consigneeId].toString())
                        .setOrderId(it[CustomsDeclarations.orderId].toString())
                        .setGoods(it[CustomsDeclarations.goods])
                        .setTotalValue(it[CustomsDeclarations.totalValue].toString())
                        .setCustomsProcedure(it[CustomsDeclarations.customsProcedure])
                        .setStatus(it[CustomsDeclarations.status])
                        .setCreatedAt(it[CustomsDeclarations.createdAt].toString())
                        .setUpdatedAt(it[CustomsDeclarations.updatedAt].toString())
                        .build()
                }
            
            val totalItems = CustomsDeclarations.selectAll().count()
            val totalPages = (totalItems + pageSize - 1) / pageSize
            
            ListDeclarationsResponse.newBuilder()
                .addAllDeclarations(declarations)
                .setTotalPages(totalPages.toInt())
                .setTotalItems(totalItems.toInt())
                .build()
        }
    }

    // Document operations
    override suspend fun createDocument(request: CreateDocumentRequest): DocumentResponse {
        return transaction {
            val id = CustomsDocuments.insertAndGetId {
                it[declarationId] = UUID.fromString(request.declarationId)
                it[documentType] = request.documentType
                it[documentNumber] = request.documentNumber
                it[documentDate] = LocalDateTime.parse(request.documentDate)
                it[fileUrl] = request.fileUrl.takeIf { it.isNotEmpty() }
                it[status] = request.status.takeIf { it.isNotEmpty() } ?: "PENDING"
            }
            
            DocumentResponse.newBuilder()
                .setId(id.toString())
                .setDeclarationId(request.declarationId)
                .setDocumentType(request.documentType)
                .setDocumentNumber(request.documentNumber)
                .setDocumentDate(request.documentDate)
                .setFileUrl(request.fileUrl)
                .setStatus(request.status.takeIf { it.isNotEmpty() } ?: "PENDING")
                .setCreatedAt(LocalDateTime.now().toString())
                .setUpdatedAt(LocalDateTime.now().toString())
                .build()
        }
    }

    override suspend fun getDocument(request: GetDocumentRequest): DocumentResponse {
        return transaction {
            CustomsDocuments.select { CustomsDocuments.id eq UUID.fromString(request.id) }
                .singleOrNull()
                ?.let {
                    DocumentResponse.newBuilder()
                        .setId(it[CustomsDocuments.id].toString())
                        .setDeclarationId(it[CustomsDocuments.declarationId].toString())
                        .setDocumentType(it[CustomsDocuments.documentType])
                        .setDocumentNumber(it[CustomsDocuments.documentNumber])
                        .setDocumentDate(it[CustomsDocuments.documentDate].toString())
                        .setFileUrl(it[CustomsDocuments.fileUrl] ?: "")
                        .setStatus(it[CustomsDocuments.status])
                        .setCreatedAt(it[CustomsDocuments.createdAt].toString())
                        .setUpdatedAt(it[CustomsDocuments.updatedAt].toString())
                        .build()
                } ?: DocumentResponse.getDefaultInstance()
        }
    }

    override suspend fun listDocuments(request: ListDocumentsRequest): ListDocumentsResponse {
        return transaction {
            val page = request.pageNumber.takeIf { it > 0 } ?: 1
            val pageSize = request.pageSize.takeIf { it > 0 } ?: 10
            val offset = (page - 1) * pageSize
            
            var query = CustomsDocuments.selectAll()
            if (request.declarationId.isNotEmpty()) {
                query = query.andWhere { CustomsDocuments.declarationId eq UUID.fromString(request.declarationId) }
            }
            
            val documents = query
                .limit(pageSize, offset.toLong())
                .map {
                    DocumentResponse.newBuilder()
                        .setId(it[CustomsDocuments.id].toString())
                        .setDeclarationId(it[CustomsDocuments.declarationId].toString())
                        .setDocumentType(it[CustomsDocuments.documentType])
                        .setDocumentNumber(it[CustomsDocuments.documentNumber])
                        .setDocumentDate(it[CustomsDocuments.documentDate].toString())
                        .setFileUrl(it[CustomsDocuments.fileUrl] ?: "")
                        .setStatus(it[CustomsDocuments.status])
                        .setCreatedAt(it[CustomsDocuments.createdAt].toString())
                        .setUpdatedAt(it[CustomsDocuments.updatedAt].toString())
                        .build()
                }
            
            val totalItems = if (request.declarationId.isNotEmpty()) {
                CustomsDocuments.select { CustomsDocuments.declarationId eq UUID.fromString(request.declarationId) }.count()
            } else {
                CustomsDocuments.selectAll().count()
            }
            val totalPages = (totalItems + pageSize - 1) / pageSize
            
            ListDocumentsResponse.newBuilder()
                .addAllDocuments(documents)
                .setTotalPages(totalPages.toInt())
                .setTotalItems(totalItems.toInt())
                .build()
        }
    }

    // Payment operations
    override suspend fun createPayment(request: CreatePaymentRequest): PaymentResponse {
        return transaction {
            val id = CustomsPayments.insertAndGetId {
                it[declarationId] = UUID.fromString(request.declarationId)
                it[paymentNumber] = request.paymentNumber
                it[amount] = java.math.BigDecimal(request.amount)
                it[paymentDate] = LocalDateTime.parse(request.paymentDate)
                it[paymentMethod] = request.paymentMethod
                it[status] = request.status.takeIf { it.isNotEmpty() } ?: "PENDING"
            }
            
            PaymentResponse.newBuilder()
                .setId(id.toString())
                .setDeclarationId(request.declarationId)
                .setPaymentNumber(request.paymentNumber)
                .setAmount(request.amount)
                .setPaymentDate(request.paymentDate)
                .setPaymentMethod(request.paymentMethod)
                .setStatus(request.status.takeIf { it.isNotEmpty() } ?: "PENDING")
                .setCreatedAt(LocalDateTime.now().toString())
                .setUpdatedAt(LocalDateTime.now().toString())
                .build()
        }
    }

    override suspend fun getPayment(request: GetPaymentRequest): PaymentResponse {
        return transaction {
            CustomsPayments.select { CustomsPayments.id eq UUID.fromString(request.id) }
                .singleOrNull()
                ?.let {
                    PaymentResponse.newBuilder()
                        .setId(it[CustomsPayments.id].toString())
                        .setDeclarationId(it[CustomsPayments.declarationId].toString())
                        .setPaymentNumber(it[CustomsPayments.paymentNumber])
                        .setAmount(it[CustomsPayments.amount].toString())
                        .setPaymentDate(it[CustomsPayments.paymentDate].toString())
                        .setPaymentMethod(it[CustomsPayments.paymentMethod])
                        .setStatus(it[CustomsPayments.status])
                        .setCreatedAt(it[CustomsPayments.createdAt].toString())
                        .setUpdatedAt(it[CustomsPayments.updatedAt].toString())
                        .build()
                } ?: PaymentResponse.getDefaultInstance()
        }
    }

    override suspend fun listPayments(request: ListPaymentsRequest): ListPaymentsResponse {
        return transaction {
            val page = request.pageNumber.takeIf { it > 0 } ?: 1
            val pageSize = request.pageSize.takeIf { it > 0 } ?: 10
            val offset = (page - 1) * pageSize
            
            var query = CustomsPayments.selectAll()
            if (request.declarationId.isNotEmpty()) {
                query = query.andWhere { CustomsPayments.declarationId eq UUID.fromString(request.declarationId) }
            }
            
            val payments = query
                .limit(pageSize, offset.toLong())
                .map {
                    PaymentResponse.newBuilder()
                        .setId(it[CustomsPayments.id].toString())
                        .setDeclarationId(it[CustomsPayments.declarationId].toString())
                        .setPaymentNumber(it[CustomsPayments.paymentNumber])
                        .setAmount(it[CustomsPayments.amount].toString())
                        .setPaymentDate(it[CustomsPayments.paymentDate].toString())
                        .setPaymentMethod(it[CustomsPayments.paymentMethod])
                        .setStatus(it[CustomsPayments.status])
                        .setCreatedAt(it[CustomsPayments.createdAt].toString())
                        .setUpdatedAt(it[CustomsPayments.updatedAt].toString())
                        .build()
                }
            
            val totalItems = if (request.declarationId.isNotEmpty()) {
                CustomsPayments.select { CustomsPayments.declarationId eq UUID.fromString(request.declarationId) }.count()
            } else {
                CustomsPayments.selectAll().count()
            }
            val totalPages = (totalItems + pageSize - 1) / pageSize
            
            ListPaymentsResponse.newBuilder()
                .addAllPayments(payments)
                .setTotalPages(totalPages.toInt())
                .setTotalItems(totalItems.toInt())
                .build()
        }
    }

    // Goods classification operations
    override suspend fun createGoodsClassification(request: CreateGoodsClassificationRequest): GoodsClassificationResponse {
        return transaction {
            val id = GoodsClassifications.insertAndGetId {
                it[declarationId] = UUID.fromString(request.declarationId)
                it[productCode] = request.productCode
                it[productName] = request.productName
                it[quantity] = request.quantity
                it[unit] = request.unit
                it[value] = java.math.BigDecimal(request.value)
                it[tnvedCode] = request.tnvedCode
                it[countryOfOrigin] = request.countryOfOrigin
                it[countryOfDestination] = request.countryOfDestination
                it[customsTariff] = java.math.BigDecimal(request.customsTariff)
                it[vatRate] = java.math.BigDecimal(request.vatRate)
            }
            
            GoodsClassificationResponse.newBuilder()
                .setId(id.toString())
                .setDeclarationId(request.declarationId)
                .setProductCode(request.productCode)
                .setProductName(request.productName)
                .setQuantity(request.quantity)
                .setUnit(request.unit)
                .setValue(request.value)
                .setTnvedCode(request.tnvedCode)
                .setCountryOfOrigin(request.countryOfOrigin)
                .setCountryOfDestination(request.countryOfDestination)
                .setCustomsTariff(request.customsTariff)
                .setVatRate(request.vatRate)
                .setCreatedAt(LocalDateTime.now().toString())
                .setUpdatedAt(LocalDateTime.now().toString())
                .build()
        }
    }

    override suspend fun getGoodsClassification(request: GetGoodsClassificationRequest): GoodsClassificationResponse {
        return transaction {
            GoodsClassifications.select { GoodsClassifications.id eq UUID.fromString(request.id) }
                .singleOrNull()
                ?.let {
                    GoodsClassificationResponse.newBuilder()
                        .setId(it[GoodsClassifications.id].toString())
                        .setDeclarationId(it[GoodsClassifications.declarationId].toString())
                        .setProductCode(it[GoodsClassifications.productCode])
                        .setProductName(it[GoodsClassifications.productName])
                        .setQuantity(it[GoodsClassifications.quantity])
                        .setUnit(it[GoodsClassifications.unit])
                        .setValue(it[GoodsClassifications.value].toString())
                        .setTnvedCode(it[GoodsClassifications.tnvedCode])
                        .setCountryOfOrigin(it[GoodsClassifications.countryOfOrigin])
                        .setCountryOfDestination(it[GoodsClassifications.countryOfDestination])
                        .setCustomsTariff(it[GoodsClassifications.customsTariff].toString())
                        .setVatRate(it[GoodsClassifications.vatRate].toString())
                        .setCreatedAt(it[GoodsClassifications.createdAt].toString())
                        .setUpdatedAt(it[GoodsClassifications.updatedAt].toString())
                        .build()
                } ?: GoodsClassificationResponse.getDefaultInstance()
        }
    }

    override suspend fun listGoodsClassifications(request: ListGoodsClassificationsRequest): ListGoodsClassificationsResponse {
        return transaction {
            val page = request.pageNumber.takeIf { it > 0 } ?: 1
            val pageSize = request.pageSize.takeIf { it > 0 } ?: 10
            val offset = (page - 1) * pageSize
            
            var query = GoodsClassifications.selectAll()
            if (request.declarationId.isNotEmpty()) {
                query = query.andWhere { GoodsClassifications.declarationId eq UUID.fromString(request.declarationId) }
            }
            
            val classifications = query
                .limit(pageSize, offset.toLong())
                .map {
                    GoodsClassificationResponse.newBuilder()
                        .setId(it[GoodsClassifications.id].toString())
                        .setDeclarationId(it[GoodsClassifications.declarationId].toString())
                        .setProductCode(it[GoodsClassifications.productCode])
                        .setProductName(it[GoodsClassifications.productName])
                        .setQuantity(it[GoodsClassifications.quantity])
                        .setUnit(it[GoodsClassifications.unit])
                        .setValue(it[GoodsClassifications.value].toString())
                        .setTnvedCode(it[GoodsClassifications.tnvedCode])
                        .setCountryOfOrigin(it[GoodsClassifications.countryOfOrigin])
                        .setCountryOfDestination(it[GoodsClassifications.countryOfDestination])
                        .setCustomsTariff(it[GoodsClassifications.customsTariff].toString())
                        .setVatRate(it[GoodsClassifications.vatRate].toString())
                        .setCreatedAt(it[GoodsClassifications.createdAt].toString())
                        .setUpdatedAt(it[GoodsClassifications.updatedAt].toString())
                        .build()
                }
            
            val totalItems = if (request.declarationId.isNotEmpty()) {
                GoodsClassifications.select { GoodsClassifications.declarationId eq UUID.fromString(request.declarationId) }.count()
            } else {
                GoodsClassifications.selectAll().count()
            }
            val totalPages = (totalItems + pageSize - 1) / pageSize
            
            ListGoodsClassificationsResponse.newBuilder()
                .addAllClassifications(classifications)
                .setTotalPages(totalPages.toInt())
                .setTotalItems(totalItems.toInt())
                .build()
        }
    }
}