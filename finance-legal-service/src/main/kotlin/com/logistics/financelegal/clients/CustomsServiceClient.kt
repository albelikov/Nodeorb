package com.logistics.financelegal.clients

import com.logistics.customs.*
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.TimeUnit

class CustomsServiceClient(
    private val host: String = "localhost",
    private val port: Int = 50056
) {
    private val channel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build()
    
    private val stub = CustomsServiceGrpcKt.CustomsServiceCoroutineStub(channel)
    
    suspend fun createDeclaration(request: CreateDeclarationRequest): DeclarationResponse {
        return stub.createDeclaration(request)
    }
    
    suspend fun getDeclaration(request: GetDeclarationRequest): DeclarationResponse {
        return stub.getDeclaration(request)
    }
    
    suspend fun updateDeclaration(request: UpdateDeclarationRequest): DeclarationResponse {
        return stub.updateDeclaration(request)
    }
    
    suspend fun listDeclarations(request: ListDeclarationsRequest): ListDeclarationsResponse {
        return stub.listDeclarations(request)
    }
    
    suspend fun createDocument(request: CreateDocumentRequest): DocumentResponse {
        return stub.createDocument(request)
    }
    
    suspend fun getDocument(request: GetDocumentRequest): DocumentResponse {
        return stub.getDocument(request)
    }
    
    suspend fun listDocuments(request: ListDocumentsRequest): ListDocumentsResponse {
        return stub.listDocuments(request)
    }
    
    suspend fun createPayment(request: CreatePaymentRequest): PaymentResponse {
        return stub.createPayment(request)
    }
    
    suspend fun getPayment(request: GetPaymentRequest): PaymentResponse {
        return stub.getPayment(request)
    }
    
    suspend fun listPayments(request: ListPaymentsRequest): ListPaymentsResponse {
        return stub.listPayments(request)
    }
    
    suspend fun createGoodsClassification(request: CreateGoodsClassificationRequest): GoodsClassificationResponse {
        return stub.createGoodsClassification(request)
    }
    
    suspend fun getGoodsClassification(request: GetGoodsClassificationRequest): GoodsClassificationResponse {
        return stub.getGoodsClassification(request)
    }
    
    suspend fun listGoodsClassifications(request: ListGoodsClassificationsRequest): ListGoodsClassificationsResponse {
        return stub.listGoodsClassifications(request)
    }
    
    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}

// Singleton client instance
object CustomsClient {
    private var instance: CustomsServiceClient? = null
    
    fun getInstance(): CustomsServiceClient {
        if (instance == null) {
            instance = CustomsServiceClient()
        }
        return instance!!
    }
}