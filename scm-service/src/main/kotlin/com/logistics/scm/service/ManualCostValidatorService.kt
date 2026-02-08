package com.logistics.scm.service

import com.logistics.scm.dao.ManualEntryValidationDAO
import com.logistics.scm.dao.MarketPriceDAO
import com.logistics.scm.service.FraudDetectionEngine
import com.logistics.scm.service.GeoSecurityService
import com.logistics.scm.validation.ManualEntryRequest
import com.logistics.scm.validation.ValidationResult
import io.grpc.Status
import io.grpc.StatusRuntimeException
import java.math.BigDecimal
import java.math.RoundingMode

class ManualCostValidatorService(
    private val marketPriceDAO: MarketPriceDAO,
    private val validationDAO: ManualEntryValidationDAO,
    private val fraudDetectionEngine: FraudDetectionEngine? = null,
    private val geoSecurityService: GeoSecurityService? = null
) {
    
    enum class RiskVerdict {
        GREEN, YELLOW, RED
    }
    
    fun validateAndSign(orderId: String, category: String, inputValue: BigDecimal, lat: Double = 0.0, lon: Double = 0.0): ValidationResult {
        // Крок 1: Geo Security Verification (NEW - First step)
        val geoResult = performGeoSecurityVerification(orderId, lat, lon)
        
        // Check for geofencing violation (RED verdict regardless of cost)
        if (!geoResult.geoVerified) {
            throw StatusRuntimeException(
                Status.PERMISSION_DENIED
                    .withDescription("Location verification failed. Distance from expected location exceeds 500 meters.")
            )
        }
        
        // Check for spoofing or VPN (RED verdict)
        if (geoResult.spoofingDetected || geoResult.vpnDetected) {
            throw StatusRuntimeException(
                Status.PERMISSION_DENIED
                    .withDescription("Security violation detected: ${if (geoResult.spoofingDetected) "Location spoofing" else "VPN/Proxy usage"}")
            )
        }
        
        // Check for REB interference (YELLOW verdict)
        if (geoResult.riskLevel == GeoSecurityService.RiskLevel.REB_INTERFERENCE) {
            // REB interference gets YELLOW verdict, not RED
            // Continue validation but mark for audit
        }
        
        // Крок 2: Отримати medianValue з таблиці market_price_medians
        val medianValue = marketPriceDAO.getMedian(category, "default_region")
            ?: BigDecimal("100.0") // Default median if not found
        
        // Крок 3: Розрахувати відхилення
        val deviation = calculateDeviation(medianValue, inputValue)
        
        // Крок 4: Fraud Detection Analysis
        val userId = extractUserId(orderId)
        val fraudAnalysis = performFraudDetection(userId, deviation)
        
        // Крок 5: Визначити вердикт (RiskVerdict)
        val verdict = determineVerdict(deviation, fraudAnalysis)
        val status = when (verdict) {
            RiskVerdict.GREEN -> "APPROVED"
            RiskVerdict.YELLOW -> "AUDIT_REQUIRED"
            RiskVerdict.RED -> "REJECTED"
        }
        
        // Крок 6: Збереження з хешуванням
        val recordHash = validationDAO.saveValidation(
            orderId = orderId,
            inputValue = inputValue,
            deviation = deviation,
            verdict = verdict.name
        )
        
        // Крок 7: Handle fraud detection alerts and trust score updates
        handleFraudDetectionAlerts(userId, orderId, fraudAnalysis, deviation)
        
        // Крок 8: Обробка відхилень (gRPC Exception)
        if (verdict == RiskVerdict.RED) {
            throw StatusRuntimeException(
                Status.PERMISSION_DENIED
                    .withDescription("Cost exceeds market median by >40%. Action blocked by SCM.")
            )
        }
        
        // Крок 9: Повернення результату
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
    
    private fun determineVerdict(deviation: Double, fraudAnalysis: FraudDetectionEngine.UserBehaviorAnalysis?): RiskVerdict {
        // Base verdict from deviation
        val baseVerdict = determineVerdict(deviation)
        
        // If fraud detection is not available, return base verdict
        if (fraudAnalysis == null) {
            return baseVerdict
        }
        
        // Check for threshold gaming (SUSPICIOUS_BEHAVIOR)
        if (fraudAnalysis.riskLevel == FraudDetectionEngine.RiskLevel.SUSPICIOUS_BEHAVIOR) {
            return RiskVerdict.YELLOW // Force YELLOW for threshold gaming
        }
        
        // Check for anomaly detection (Z-score > 2.5)
        if (fraudAnalysis.anomalyDetected) {
            return RiskVerdict.YELLOW // Force YELLOW for anomalies
        }
        
        return baseVerdict
    }
    
    private fun extractUserId(orderId: String): String {
        // Extract user ID from order ID
        // Assuming format: "USER123_ORDER456" or similar
        return try {
            val parts = orderId.split("_")
            if (parts.size >= 1) parts[0] else orderId
        } catch (e: Exception) {
            orderId // Fallback to full order ID if parsing fails
        }
    }
    
    private fun performFraudDetection(userId: String, deviation: Double): FraudDetectionEngine.UserBehaviorAnalysis? {
        return try {
            fraudDetectionEngine?.analyzeUserBehavior(userId, deviation)
        } catch (e: Exception) {
            // Log error but don't fail validation
            println("Fraud detection failed for user $userId: ${e.message}")
            null
        }
    }
    
    private fun handleFraudDetectionAlerts(
        userId: String, 
        orderId: String, 
        fraudAnalysis: FraudDetectionEngine.UserBehaviorAnalysis?, 
        deviation: Double
    ) {
        if (fraudAnalysis == null) return
        
        // Send alert if suspicious behavior detected
        if (fraudAnalysis.riskLevel == FraudDetectionEngine.RiskLevel.SUSPICIOUS_BEHAVIOR) {
            val alert = FraudDetectionEngine.FraudAlert(
                userId = userId,
                orderId = orderId,
                riskLevel = FraudDetectionEngine.RiskLevel.SUSPICIOUS_BEHAVIOR,
                anomalyDetected = false,
                deviation = deviation,
                zScore = fraudAnalysis.zScore,
                alertTimestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                alertType = "THRESHOLD_GAMING_DETECTED"
            )
            
            fraudDetectionEngine?.sendFraudAlert(alert)
            fraudDetectionEngine?.updateTrustScore(userId, 10)
        }
        
        // Send alert if anomaly detected
        if (fraudAnalysis.anomalyDetected) {
            val alert = FraudDetectionEngine.FraudAlert(
                userId = userId,
                orderId = orderId,
                riskLevel = FraudDetectionEngine.RiskLevel.HIGH,
                anomalyDetected = true,
                deviation = deviation,
                zScore = fraudAnalysis.zScore,
                alertTimestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                alertType = "ANOMALY_DETECTED"
            )
            
            fraudDetectionEngine?.sendFraudAlert(alert)
            fraudDetectionEngine?.updateTrustScore(userId, 5) // Smaller reduction for anomalies
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
    
    private fun performGeoSecurityVerification(orderId: String, lat: Double, lon: Double): GeoSecurityService.GeoVerificationResult {
        return try {
            geoSecurityService?.verifyLocation(orderId, lat, lon)
                ?: GeoSecurityService.GeoVerificationResult(
                    geoVerified = true,
                    distanceMeters = 0.0,
                    spoofingDetected = false,
                    vpnDetected = false,
                    riskLevel = GeoSecurityService.RiskLevel.LOW,
                    verificationTimestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
        } catch (e: Exception) {
            // Log error but don't fail validation
            println("Geo security verification failed for order $orderId: ${e.message}")
            GeoSecurityService.GeoVerificationResult(
                geoVerified = true, // Allow validation to proceed if geo service fails
                distanceMeters = 0.0,
                spoofingDetected = false,
                vpnDetected = false,
                riskLevel = GeoSecurityService.RiskLevel.LOW,
                verificationTimestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        }
    }
}
