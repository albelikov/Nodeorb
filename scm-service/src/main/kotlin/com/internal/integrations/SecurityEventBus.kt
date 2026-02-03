package com.internal.integrations

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Security Event Bus - шина событий безопасности для интеграции с Kafka
 * Обеспечивает реальное время реагирование на угрозы
 */
@Service
class SecurityEventBus(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(SecurityEventBus::class.java)

    companion object {
        const val TOPIC_SECURITY_EVENTS = "security.events"
        const val TOPIC_AUDIT_LOGS = "audit.logs"
        const val TOPIC_THREAT_INTELLIGENCE = "threat.intelligence"
    }

    /**
     * Отправка события безопасности
     */
    fun sendSecurityEvent(event: SecurityEvent) {
        try {
            val eventJson = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(TOPIC_SECURITY_EVENTS, event.eventId, eventJson)
            logger.debug("Security event sent: ${event.eventId}")
        } catch (e: Exception) {
            logger.error("Failed to send security event: ${event.eventId}", e)
        }
    }

    /**
     * Отправка аудит-лога
     */
    fun sendAuditLog(auditLog: AuditLog) {
        try {
            val logJson = objectMapper.writeValueAsString(auditLog)
            kafkaTemplate.send(TOPIC_AUDIT_LOGS, auditLog.logId, logJson)
            logger.debug("Audit log sent: ${auditLog.logId}")
        } catch (e: Exception) {
            logger.error("Failed to send audit log: ${auditLog.logId}", e)
        }
    }

    /**
     * Отправка данных разведки угроз
     */
    fun sendThreatIntelligence(threat: ThreatIntelligence) {
        try {
            val threatJson = objectMapper.writeValueAsString(threat)
            kafkaTemplate.send(TOPIC_THREAT_INTELLIGENCE, threat.threatId, threatJson)
            logger.debug("Threat intelligence sent: ${threat.threatId}")
        } catch (e: Exception) {
            logger.error("Failed to send threat intelligence: ${threat.threatId}", e)
        }
    }

    /**
     * Блокировка пользователя через Security Event Bus
     */
    fun blockUser(userId: String, reason: String, sourceService: String) {
        val event = SecurityEvent(
            eventId = "BLOCK_USER_${System.currentTimeMillis()}",
            eventType = "USER_BLOCKED",
            timestamp = Instant.now(),
            userId = userId,
            sourceService = sourceService,
            details = mapOf(
                "reason" to reason,
                "source" to "SCM_SERVICE"
            )
        )
        
        sendSecurityEvent(event)
        logger.warn("User blocked via Security Event Bus: $userId, reason: $reason")
    }

    /**
     * Срабатывание аномалии цен
     */
    fun triggerPriceAnomaly(
        userId: String,
        orderId: String,
        deviation: Double,
        suggestedMedian: Double
    ) {
        val event = SecurityEvent(
            eventId = "PRICE_ANOMALY_${System.currentTimeMillis()}",
            eventType = "PRICE_ANOMALY_DETECTED",
            timestamp = Instant.now(),
            userId = userId,
            sourceService = "SCM_SERVICE",
            details = mapOf(
                "order_id" to orderId,
                "deviation" to deviation.toString(),
                "suggested_median" to suggestedMedian.toString(),
                "risk_level" to determineRiskLevel(deviation)
            )
        )
        
        sendSecurityEvent(event)
        logger.warn("Price anomaly detected for user: $userId, deviation: $deviation")
    }

    /**
     * Срабатывание геозоны
     */
    fun triggerGeofenceViolation(
        userId: String,
        latitude: Double,
        longitude: Double,
        geofenceType: String,
        violationReason: String
    ) {
        val event = SecurityEvent(
            eventId = "GEOFENCE_VIOLATION_${System.currentTimeMillis()}",
            eventType = "GEOFENCE_VIOLATION",
            timestamp = Instant.now(),
            userId = userId,
            sourceService = "SCM_SERVICE",
            details = mapOf(
                "latitude" to latitude.toString(),
                "longitude" to longitude.toString(),
                "geofence_type" to geofenceType,
                "violation_reason" to violationReason
            )
        )
        
        sendSecurityEvent(event)
        logger.warn("Geofence violation for user: $userId, location: $latitude, $longitude")
    }

    /**
     * Срабатывание часов работы (ELD)
     */
    fun triggerHoursOfServiceViolation(
        driverId: String,
        vehicleId: String,
        remainingHours: Int,
        violationReason: String
    ) {
        val event = SecurityEvent(
            eventId = "HOS_VIOLATION_${System.currentTimeMillis()}",
            eventType = "HOURS_OF_SERVICE_VIOLATION",
            timestamp = Instant.now(),
            userId = driverId,
            sourceService = "SCM_SERVICE",
            details = mapOf(
                "vehicle_id" to vehicleId,
                "remaining_hours" to remainingHours.toString(),
                "violation_reason" to violationReason
            )
        )
        
        sendSecurityEvent(event)
        logger.warn("Hours of Service violation for driver: $driverId, vehicle: $vehicleId")
    }

    /**
     * Срабатывание биометрической проверки
     */
    fun triggerBiometricFailure(
        userId: String,
        sessionId: String,
        failureReason: String
    ) {
        val event = SecurityEvent(
            eventId = "BIOMETRIC_FAILURE_${System.currentTimeMillis()}",
            eventType = "BIOMETRIC_AUTHENTICATION_FAILED",
            timestamp = Instant.now(),
            userId = userId,
            sourceService = "SCM_SERVICE",
            details = mapOf(
                "session_id" to sessionId,
                "failure_reason" to failureReason
            )
        )
        
        sendSecurityEvent(event)
        logger.warn("Biometric authentication failed for user: $userId, session: $sessionId")
    }

    /**
     * Определение уровня риска на основе отклонения цены
     */
    private fun determineRiskLevel(deviation: Double): String {
        return when {
            deviation <= 0.15 -> "LOW"
            deviation <= 0.40 -> "MEDIUM"
            else -> "HIGH"
        }
    }
}

/**
 * Модель события безопасности
 */
data class SecurityEvent(
    val eventId: String,
    val eventType: String,
    val timestamp: Instant,
    val userId: String,
    val sourceService: String,
    val details: Map<String, String>
)

/**
 * Модель аудит-лога
 */
data class AuditLog(
    val logId: String,
    val eventType: String,
    val timestamp: Instant,
    val userId: String,
    val serviceId: String,
    val action: String,
    val resource: String,
    val result: String,
    val details: Map<String, Any>
)

/**
 * Модель данных разведки угроз
 */
data class ThreatIntelligence(
    val threatId: String,
    val threatType: String,
    val severity: String,
    val indicators: List<String>,
    val description: String,
    val timestamp: Instant,
    val source: String
)