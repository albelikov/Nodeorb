package com.logistics.scm.service

import com.logistics.scm.dao.AppealDAO
import com.logistics.scm.dao.ManualEntryValidationDAO
import com.logistics.scm.validation.AppealRequest
import com.logistics.scm.validation.AppealResponse
import com.logistics.scm.validation.AppealResult
import com.logistics.scm.validation.AppealUpdateRequest
import com.logistics.scm.validation.AppealUpdateResponse
import com.logistics.scm.validation.AppealQuery
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppealService(
    private val appealDAO: AppealDAO,
    private val validationDAO: ManualEntryValidationDAO
) {
    
    fun submitAppeal(recordHash: String, justification: String, evidenceUrl: String): AppealResponse {
        // Validate input parameters
        if (recordHash.isBlank()) {
            throw IllegalArgumentException("Record hash cannot be empty")
        }
        
        if (justification.isBlank()) {
            throw IllegalArgumentException("Justification cannot be empty")
        }
        
        if (evidenceUrl.isBlank()) {
            throw IllegalArgumentException("Evidence URL cannot be empty")
        }
        
        try {
            // Submit appeal and get the hash chain result
            val appealHash = appealDAO.submitAppeal(recordHash, justification, evidenceUrl)
            
            return AppealResponse.newBuilder()
                .setAppealId(appealHash)
                .setRecordHash(recordHash)
                .setStatus("PENDING")
                .setMessage("Appeal submitted successfully. Validation record status updated to PENDING_REVIEW.")
                .build()
                
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException("Failed to submit appeal: ${e.message}", e)
        }
    }
    
    fun getAppeal(recordHash: String): AppealResult {
        if (recordHash.isBlank()) {
            throw IllegalArgumentException("Record hash cannot be empty")
        }
        
        val appeal = appealDAO.getAppealByRecordHash(recordHash)
            ?: throw IllegalArgumentException("No appeal found for record hash: $recordHash")
        
        return AppealResult.newBuilder()
            .setAppealId(appeal.id)
            .setRecordHash(appeal.recordHash)
            .setJustification(appeal.justification)
            .setEvidenceUrl(appeal.evidenceUrl)
            .setStatus(appeal.status)
            .setCreatedAt(appeal.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .build()
    }
    
    fun updateAppealStatus(appealId: String, status: String, reviewNotes: String? = null): AppealUpdateResponse {
        if (appealId.isBlank()) {
            throw IllegalArgumentException("Appeal ID cannot be empty")
        }
        
        if (status !in listOf("APPROVED", "REJECTED")) {
            throw IllegalArgumentException("Status must be either APPROVED or REJECTED")
        }
        
        try {
            appealDAO.updateAppealStatus(appealId, status, reviewNotes)
            
            return AppealUpdateResponse.newBuilder()
                .setAppealId(appealId)
                .setStatus(status)
                .setMessage("Appeal status updated successfully")
                .build()
                
        } catch (e: Exception) {
            throw RuntimeException("Failed to update appeal status: ${e.message}", e)
        }
    }
    
    fun getAppealHistory(recordHash: String): List<com.logistics.scm.dao.AppealRecord> {
        if (recordHash.isBlank()) {
            throw IllegalArgumentException("Record hash cannot be empty")
        }
        
        return appealDAO.getAppealHistory(recordHash)
    }
    
    fun verifyAppealIntegrity(): Boolean {
        return appealDAO.verifyAppealIntegrity()
    }
    
    fun submitAppealFromRequest(request: AppealRequest): AppealResponse {
        return submitAppeal(
            recordHash = request.recordHash,
            justification = request.justification,
            evidenceUrl = request.evidenceUrl
        )
    }
    
    fun getAppealFromQuery(query: AppealQuery): AppealResult {
        return getAppeal(query.recordHash)
    }
    
    fun updateAppealStatusFromRequest(request: AppealUpdateRequest): AppealUpdateResponse {
        return updateAppealStatus(
            appealId = request.appealId,
            status = request.status,
            reviewNotes = request.reviewNotes
        )
    }
}