package com.logistics.scm.service

import com.logistics.scm.dao.ManualEntryValidationDAO
import com.logistics.scm.dao.MarketPriceDAO
import com.logistics.scm.validation.ManualEntryRequest
import com.logistics.scm.validation.ValidationResult
import io.grpc.Status
import io.grpc.StatusRuntimeException
import java.math.BigDecimal
import java.math.RoundingMode

class ManualCostValidatorService(
    private val marketPriceDAO: MarketPriceDAO,
    private val validationDAO: ManualEntryValidationDAO
) {
    
    enum class RiskVerdict {
        GREEN, YELLOW, RED
    }
    
    fun validateAndSign(orderId: String, category: String, inputValue: BigDecimal): ValidationResult {
        // Крок 1: Отримати medianValue з таблиці market_price_medians
        val medianValue = marketPriceDAO.getMedian(category, "default_region")
            ?: BigDecimal("100.0") // Default median if not found
        
        // Крок 2: Розрахувати відхилення
        val deviation = calculateDeviation(medianValue, inputValue)
        
        // Крок 3: Визначити вердикт (RiskVerdict)
        val verdict = determineVerdict(deviation)
        val status = when (verdict) {
            RiskVerdict.GREEN -> "APPROVED"
            RiskVerdict.YELLOW -> "AUDIT_REQUIRED"
            RiskVerdict.RED -> "REJECTED"
        }
        
        // Крок 4: Збереження з хешуванням
        val recordHash = validationDAO.saveValidation(
            orderId = orderId,
            inputValue = inputValue,
            deviation = deviation,
            verdict = verdict.name
        )
        
        // Обробка відхилень (gRPC Exception)
        if (verdict == RiskVerdict.RED) {
            throw StatusRuntimeException(
                Status.PERMISSION_DENIED
                    .withDescription("Cost exceeds market median by >40%. Action blocked by SCM.")
            )
        }
        
        // Повернення результату
        return ValidationResult.newBuilder()
            .setStatus(status)
            .setDeviation(deviation)
            .setRecordHash(recordHash)
            .setAuditPriority(if (verdict == RiskVerdict.YELLOW) "high" else "")
            .build()
    }
    
    private fun calculateDeviation(median: BigDecimal, inputValue: BigDecimal): Double {
        if (median.compareTo(BigDecimal.ZERO) == 0) {
            return 100.0 // High deviation if median is zero
        }
        
        val difference = inputValue.subtract(median)
        val deviation = difference.divide(median, 4, RoundingMode.HALF_UP)
        return deviation.multiply(BigDecimal("100")).toDouble()
    }
    
    private fun determineVerdict(deviation: Double): RiskVerdict {
        return when {
            deviation <= 20.0 -> RiskVerdict.GREEN
            deviation > 20.0 && deviation <= 40.0 -> RiskVerdict.YELLOW
            else -> RiskVerdict.RED
        }
    }
    
    fun checkManualEntry(request: ManualEntryRequest): ValidationResult {
        val inputValue = BigDecimal(request.inputValue.toString())
        return validateAndSign(
            orderId = request.orderId,
            category = request.category,
            inputValue = inputValue
        )
    }
    
    fun verifyDataIntegrity(): Boolean {
        return validationDAO.verifyIntegrity()
    }
    
    fun getValidationHistory(orderId: String): List<com.logistics.scm.dao.ValidationRecord> {
        return validationDAO.getValidationHistory(orderId)
    }
}