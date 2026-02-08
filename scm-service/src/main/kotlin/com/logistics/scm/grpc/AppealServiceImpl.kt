package com.logistics.scm.grpc

import com.logistics.scm.dao.AppealDAO
import com.logistics.scm.dao.ManualEntryValidationDAO
import com.logistics.scm.service.AppealService
import com.logistics.scm.validation.AppealRequest
import com.logistics.scm.validation.AppealResponse
import com.logistics.scm.validation.AppealResult
import com.logistics.scm.validation.AppealUpdateRequest
import com.logistics.scm.validation.AppealUpdateResponse
import com.logistics.scm.validation.AppealQuery
import com.logistics.scm.validation.AppealServiceGrpcKt
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppealServiceImpl : AppealServiceGrpcKt.AppealServiceCoroutineImplBase() {
    
    private val appealDAO = AppealDAO()
    private val validationDAO = ManualEntryValidationDAO()
    private val appealService = AppealService(appealDAO, validationDAO)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    override suspend fun submitAppeal(request: AppealRequest): AppealResponse {
        return appealService.submitAppealFromRequest(request)
    }
    
    override suspend fun getAppeal(request: AppealQuery): AppealResult {
        return appealService.getAppealFromQuery(request)
    }
    
    override suspend fun updateAppealStatus(request: AppealUpdateRequest): AppealUpdateResponse {
        return appealService.updateAppealStatusFromRequest(request)
    }
    
    fun submitAppealAsync(
        request: AppealRequest,
        responseObserver: StreamObserver<AppealResponse>
    ) {
        scope.launch {
            try {
                val response = appealService.submitAppealFromRequest(request)
                responseObserver.onNext(response)
                responseObserver.onCompleted()
            } catch (e: Exception) {
                responseObserver.onError(e)
            }
        }
    }
    
    fun getAppealAsync(
        request: AppealQuery,
        responseObserver: StreamObserver<AppealResult>
    ) {
        scope.launch {
            try {
                val response = appealService.getAppealFromQuery(request)
                responseObserver.onNext(response)
                responseObserver.onCompleted()
            } catch (e: Exception) {
                responseObserver.onError(e)
            }
        }
    }
    
    fun updateAppealStatusAsync(
        request: AppealUpdateRequest,
        responseObserver: StreamObserver<AppealUpdateResponse>
    ) {
        scope.launch {
            try {
                val response = appealService.updateAppealStatusFromRequest(request)
                responseObserver.onNext(response)
                responseObserver.onCompleted()
            } catch (e: Exception) {
                responseObserver.onError(e)
            }
        }
    }
    
    fun verifyAppealIntegrity(): Boolean {
        return appealService.verifyAppealIntegrity()
    }
    
    fun getAppealHistory(recordHash: String): List<com.logistics.scm.dao.AppealRecord> {
        return appealService.getAppealHistory(recordHash)
    }
}