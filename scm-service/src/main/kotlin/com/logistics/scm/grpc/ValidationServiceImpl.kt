package com.logistics.scm.grpc

import com.logistics.scm.dao.ManualEntryValidationDAO
import com.logistics.scm.dao.MarketPriceDAO
import com.logistics.scm.service.ValidationEngine
import com.logistics.scm.validation.CostRequest
import com.logistics.scm.validation.CostResponse
import com.logistics.scm.validation.ValidationServiceGrpcKt
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ValidationServiceImpl : ValidationServiceGrpcKt.ValidationServiceCoroutineImplBase() {
    
    private val marketPriceDAO = MarketPriceDAO()
    private val validationDAO = ManualEntryValidationDAO()
    private val validationEngine = ValidationEngine(marketPriceDAO, validationDAO)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    override suspend fun validateCost(request: CostRequest): CostResponse {
        return validationEngine.validate(request)
    }
    
    fun validateCostAsync(
        request: CostRequest,
        responseObserver: StreamObserver<CostResponse>
    ) {
        scope.launch {
            try {
                val response = validationEngine.validate(request)
                responseObserver.onNext(response)
                responseObserver.onCompleted()
            } catch (e: Exception) {
                responseObserver.onError(e)
            }
        }
    }
    
    fun verifyDataIntegrity(): Boolean {
        return validationEngine.verifyDataIntegrity()
    }
    
    fun getValidationHistory(orderId: String): List<com.logistics.scm.dao.ValidationRecord> {
        return validationEngine.getValidationHistory(orderId)
    }
}