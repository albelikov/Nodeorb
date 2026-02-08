package com.logistics.scm.service

import com.logistics.scm.dao.ManualEntryValidationDAO
import com.logistics.scm.dao.MarketPriceDAO
import com.logistics.scm.validation.CostRequest
import com.logistics.scm.validation.CostResponse
import io.grpc.Status
import java.math.BigDecimal
import java.math.RoundingMode

class ValidationEngine(
    private val marketPriceDAO: MarketPriceDAO,
    private val validationDAO: ManualEntryValidationDAO
) {
    
    companion object {
        const val AUDIT_THRESHOLD = 20.0
        const val REJECT_THRESHOLD = 40.0
    }
    
    fun validate(request: CostRequest): CostResponse {
        val median = marketPriceDAO.getMedian(request.category, "default_region")
            ?: BigDecimal("100.0") // Default median if not found
        
        val inputValue = BigDecimal(request.inputValue.toString())
        val deviation = calculateDeviation(median, inputValue)
        
        val verdict = determineVerdict(deviation)
        val status = when (verdict) {
            "RED" -> "REJECTED"
            "YELLOW" -> "AUDIT_REQUIRED"
            else -> "APPROVED"
        }
        
        // Save validation record with hash chaining
        validationDAO.saveValidation(
            orderId = request.orderId,
            inputValue = inputValue,
            deviation = deviation,
            verdict = verdict
        )
        
        return when (verdict) {
            "RED" -> {
                throw Status.PERMISSION_DENIED
                    .withDescription("Price too high (Rejected by SCM)")
                    .asException()
            }
            "YELLOW" -> {
                CostResponse.newBuilder()
                    .setStatus("AUDIT_REQUIRED")
                    .setDeviation(deviation)
                    .build()
            }
            else -> {
                CostResponse.newBuilder()
                    .setStatus("APPROVED")
                    .setDeviation(deviation)
                    .build()
            }
        }
    }
    
    private fun calculateDeviation(median: BigDecimal, inputValue: BigDecimal): Double {
        if (median.compareTo(BigDecimal.ZERO) == 0) {
            return 100.0 // High deviation if median is zero
        }
        
        val difference = inputValue.subtract(median)
        val deviation = difference.divide(median, 4, RoundingMode.HALF_UP)
        return deviation.multiply(BigDecimal("100")).toDouble()
    }
    
    private fun determineVerdict(deviation: Double): String {
        return when {
            deviation > REJECT_THRESHOLD -> "RED"
            deviation > AUDIT_THRESHOLD -> "YELLOW"
            else -> "GREEN"
        }
    }
    
    fun verifyDataIntegrity(): Boolean {
        return validationDAO.verifyIntegrity()
    }
    
    fun getValidationHistory(orderId: String): List<com.logistics.scm.dao.ValidationRecord> {
        return validationDAO.getValidationHistory(orderId)
    }
}