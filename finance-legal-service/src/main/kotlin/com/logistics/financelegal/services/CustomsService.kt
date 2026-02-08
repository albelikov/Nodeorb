package com.logistics.financelegal.services

import com.logistics.customs.*
import com.logistics.financelegal.clients.CustomsClient
import com.logistics.financelegal.clients.CustomsServiceClient
import kotlinx.coroutines.runBlocking
import java.util.UUID

class CustomsService {
    private val client: CustomsServiceClient = CustomsClient.getInstance()
    
    // Declaration operations
    suspend fun createDeclaration(
        declarationNumber: String,
        shipperId: UUID,
        consigneeId: UUID,
        orderId: UUID,
        goods: String,
        totalValue: String,
        customsProcedure: String,
        status: String = "PENDING"
    ): DeclarationResponse {
        val request = CreateDeclarationRequest.newBuilder()
            .setDeclarationNumber(declarationNumber)
            .setShipperId(shipperId.toString())
            .setConsigneeId(consigneeId.toString())
            .setOrderId(orderId.toString())
            .setGoods(goods)
            .setTotalValue(totalValue)
            .setCustomsProcedure(customsProcedure)
            .setStatus(status)
            .build()
        
        return client.createDeclaration(request)
    }
    
    suspend fun getDeclaration(id: UUID): DeclarationResponse {
        val request = GetDeclarationRequest.newBuilder()
            .setId(id.toString())
            .build()
        
        return client.getDeclaration(request)
    }
    
    suspend fun updateDeclaration(
        id: UUID,
        declarationNumber: String? = null,
        shipperId: UUID? = null,
        consigneeId: UUID? = null,
        orderId: UUID? = null,
        goods: String? = null,
        totalValue: String? = null,
        customsProcedure: String? = null,
        status: String? = null
    ): DeclarationResponse {
        val request = UpdateDeclarationRequest.newBuilder()
            .setId(id.toString())
            .apply {
                declarationNumber?.let { setDeclarationNumber(it) }
                shipperId?.let { setShipperId(it.toString()) }
                consigneeId?.let { setConsigneeId(it.toString()) }
                orderId?.let { setOrderId(it.toString()) }
                goods?.let { setGoods(it) }
                totalValue?.let { setTotalValue(it) }
                customsProcedure?.let { setCustomsProcedure(it) }
                status?.let { setStatus(it) }
            }
            .build()
        
        return client.updateDeclaration(request)
    }
    
    suspend fun listDeclarations(pageNumber: Int = 1, pageSize: Int = 10): ListDeclarationsResponse {
        val request = ListDeclarationsRequest.newBuilder()
            .setPageNumber(pageNumber)
            .setPageSize(pageSize)
            .build()
        
        return client.listDeclarations(request)
    }
    
    // Document operations
    suspend fun createDocument(
        declarationId: UUID,
        documentType: String,
        documentNumber: String,
        documentDate: String,
        fileUrl: String? = null,
        status: String = "PENDING"
    ): DocumentResponse {
        val request = CreateDocumentRequest.newBuilder()
            .setDeclarationId(declarationId.toString())
            .setDocumentType(documentType)
            .setDocumentNumber(documentNumber)
            .setDocumentDate(documentDate)
            .apply { fileUrl?.let { setFileUrl(it) } }
            .setStatus(status)
            .build()
        
        return client.createDocument(request)
    }
    
    suspend fun getDocument(id: UUID): DocumentResponse {
        val request = GetDocumentRequest.newBuilder()
            .setId(id.toString())
            .build()
        
        return client.getDocument(request)
    }
    
    suspend fun listDocuments(pageNumber: Int = 1, pageSize: Int = 10, declarationId: UUID? = null): ListDocumentsResponse {
        val request = ListDocumentsRequest.newBuilder()
            .setPageNumber(pageNumber)
            .setPageSize(pageSize)
            .apply { declarationId?.let { setDeclarationId(it.toString()) } }
            .build()
        
        return client.listDocuments(request)
    }
    
    // Payment operations
    suspend fun createPayment(
        declarationId: UUID,
        paymentNumber: String,
        amount: String,
        paymentDate: String,
        paymentMethod: String,
        status: String = "PENDING"
    ): PaymentResponse {
        val request = CreatePaymentRequest.newBuilder()
            .setDeclarationId(declarationId.toString())
            .setPaymentNumber(paymentNumber)
            .setAmount(amount)
            .setPaymentDate(paymentDate)
            .setPaymentMethod(paymentMethod)
            .setStatus(status)
            .build()
        
        return client.createPayment(request)
    }
    
    suspend fun getPayment(id: UUID): PaymentResponse {
        val request = GetPaymentRequest.newBuilder()
            .setId(id.toString())
            .build()
        
        return client.getPayment(request)
    }
    
    suspend fun listPayments(pageNumber: Int = 1, pageSize: Int = 10, declarationId: UUID? = null): ListPaymentsResponse {
        val request = ListPaymentsRequest.newBuilder()
            .setPageNumber(pageNumber)
            .setPageSize(pageSize)
            .apply { declarationId?.let { setDeclarationId(it.toString()) } }
            .build()
        
        return client.listPayments(request)
    }
    
    // Goods classification operations
    suspend fun createGoodsClassification(
        declarationId: UUID,
        productCode: String,
        productName: String,
        quantity: Int,
        unit: String,
        value: String,
        tnvedCode: String,
        countryOfOrigin: String,
        countryOfDestination: String,
        customsTariff: String,
        vatRate: String
    ): GoodsClassificationResponse {
        val request = CreateGoodsClassificationRequest.newBuilder()
            .setDeclarationId(declarationId.toString())
            .setProductCode(productCode)
            .setProductName(productName)
            .setQuantity(quantity)
            .setUnit(unit)
            .setValue(value)
            .setTnvedCode(tnvedCode)
            .setCountryOfOrigin(countryOfOrigin)
            .setCountryOfDestination(countryOfDestination)
            .setCustomsTariff(customsTariff)
            .setVatRate(vatRate)
            .build()
        
        return client.createGoodsClassification(request)
    }
    
    suspend fun getGoodsClassification(id: UUID): GoodsClassificationResponse {
        val request = GetGoodsClassificationRequest.newBuilder()
            .setId(id.toString())
            .build()
        
        return client.getGoodsClassification(request)
    }
    
    suspend fun listGoodsClassifications(pageNumber: Int = 1, pageSize: Int = 10, declarationId: UUID? = null): ListGoodsClassificationsResponse {
        val request = ListGoodsClassificationsRequest.newBuilder()
            .setPageNumber(pageNumber)
            .setPageSize(pageSize)
            .apply { declarationId?.let { setDeclarationId(it.toString()) } }
            .build()
        
        return client.listGoodsClassifications(request)
    }
}