package com.logistics.scm.service

import com.logistics.scm.service.ClickHouseService
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.Properties

class FraudDetectionEngine(
    private val clickHouseService: ClickHouseService
) {
    
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val deviationThreshold = 0.35..0.399
    private val suspiciousBehaviorThreshold = 0.70
    private val zScoreThreshold = 2.5
    
    data class UserBehaviorAnalysis(
        val userId: String,
        val meanDeviation: Double,
        val deviationInRangePercentage: Double,
        val zScore: Double,
        val riskLevel: RiskLevel,
        val anomalyDetected: Boolean,
        val analysisTimestamp: String
    )
    
    enum class RiskLevel {
        LOW, MEDIUM, HIGH, SUSPICIOUS_BEHAVIOR
    }
    
    data class FraudAlert(
        val userId: String,
        val orderId: String,
        val riskLevel: RiskLevel,
        val anomalyDetected: Boolean,
        val deviation: Double,
        val zScore: Double,
        val alertTimestamp: String,
        val alertType: String
    )
    
    fun analyzeUserBehavior(userId: String, currentDeviation: Double): UserBehaviorAnalysis {
        try {
            // Get last 20 MANUAL_COST_ENTRY events for this user from ClickHouse
            val userEvents = getUserEventsFromClickHouse(userId, 20)
            
            if (userEvents.isEmpty()) {
                return UserBehaviorAnalysis(
                    userId = userId,
                    meanDeviation = currentDeviation,
                    deviationInRangePercentage = 0.0,
                    zScore = 0.0,
                    riskLevel = RiskLevel.LOW,
                    anomalyDetected = false,
                    analysisTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            }
            
            // Calculate statistical metrics
            val meanDeviation = calculateMeanDeviation(userEvents)
            val deviationInRangePercentage = calculateDeviationInRangePercentage(userEvents)
            val zScore = calculateZScore(userEvents, currentDeviation)
            
            // Determine risk level
            val riskLevel = determineRiskLevel(deviationInRangePercentage, zScore)
            val anomalyDetected = zScore > zScoreThreshold
            
            return UserBehaviorAnalysis(
                userId = userId,
                meanDeviation = meanDeviation,
                deviationInRangePercentage = deviationInRangePercentage,
                zScore = zScore,
                riskLevel = riskLevel,
                anomalyDetected = anomalyDetected,
                analysisTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to analyze user behavior for user $userId: ${e.message}", e)
        }
    }
    
    fun detectThresholdGaming(userId: String): Boolean {
        val userEvents = getUserEventsFromClickHouse(userId, 20)
        if (userEvents.size < 5) return false
        
        val inRangeCount = userEvents.count { event ->
            event.deviation in deviationThreshold
        }
        
        val percentageInRange = inRangeCount.toDouble() / userEvents.size
        return percentageInRange >= suspiciousBehaviorThreshold
    }
    
    fun calculateZScore(userId: String, currentDeviation: Double): Double {
        val userEvents = getUserEventsFromClickHouse(userId, 20)
        if (userEvents.size < 3) return 0.0
        
        val deviations = userEvents.map { it.deviation }.toDoubleArray()
        val stats = DescriptiveStatistics(deviations)
        
        val mean = stats.mean
        val standardDeviation = stats.standardDeviation
        
        return if (standardDeviation == 0.0) 0.0
        else (currentDeviation - mean) / standardDeviation
    }
    
    fun sendFraudAlert(alert: FraudAlert) {
        try {
            val producer = createKafkaProducer()
            val message = objectMapper.writeValueAsString(alert)
            val record = ProducerRecord("security.alerts", alert.userId, message)
            producer.send(record)
            producer.flush()
            producer.close()
        } catch (e: Exception) {
            throw RuntimeException("Failed to send fraud alert: ${e.message}", e)
        }
    }
    
    fun updateTrustScore(userId: String, reductionPoints: Int = 10) {
        try {
            // This would typically update the compliance_passports table
            // For now, we'll log the action
            println("TRUST_SCORE_UPDATE: User $userId trust score reduced by $reductionPoints points due to suspicious behavior")
            
            // In a real implementation, this would be:
            // val updateSQL = "UPDATE compliance_passports SET trust_score = trust_score - ? WHERE user_id = ?"
            // Execute with proper database connection
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to update trust score for user $userId: ${e.message}", e)
        }
    }
    
    private fun getUserEventsFromClickHouse(userId: String, limit: Int): List<UserEvent> {
        val selectSQL = """
            SELECT order_id, risk_verdict, input_data, current_hash, prev_hash, event_type, event_time
            FROM scm_audit_log
            WHERE order_id LIKE ? AND event_type = 'VALIDATION'
            ORDER BY event_time DESC
            LIMIT ?
        """.trimIndent()
        
        val events = mutableListOf<UserEvent>()
        
        try {
            val connection = clickHouseService.getConnection()
            connection.prepareStatement(selectSQL).use { statement ->
                statement.setString(1, "$userId%")
                statement.setInt(2, limit)
                
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val inputData = resultSet.getString("input_data")
                        val deviation = extractDeviationFromInputData(inputData)
                        
                        val event = UserEvent(
                            orderId = resultSet.getString("order_id"),
                            deviation = deviation,
                            riskVerdict = resultSet.getString("risk_verdict"),
                            eventTime = resultSet.getString("event_time")
                        )
                        events.add(event)
                    }
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to get user events from ClickHouse: ${e.message}", e)
        }
        
        return events
    }
    
    private fun extractDeviationFromInputData(inputData: String): Double {
        // Parse deviation from input_data field
        // This assumes the format contains deviation information
        // In a real implementation, this would be more robust
        return try {
            inputData.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }
    
    private fun calculateMeanDeviation(events: List<UserEvent>): Double {
        if (events.isEmpty()) return 0.0
        return events.map { it.deviation }.average()
    }
    
    private fun calculateDeviationInRangePercentage(events: List<UserEvent>): Double {
        if (events.isEmpty()) return 0.0
        val inRangeCount = events.count { it.deviation in deviationThreshold }
        return (inRangeCount.toDouble() / events.size) * 100.0
    }
    
    private fun calculateZScore(events: List<UserEvent>, currentDeviation: Double): Double {
        if (events.size < 3) return 0.0
        
        val deviations = events.map { it.deviation }.toDoubleArray()
        val stats = DescriptiveStatistics(deviations)
        
        val mean = stats.mean
        val standardDeviation = stats.standardDeviation
        
        return if (standardDeviation == 0.0) 0.0
        else (currentDeviation - mean) / standardDeviation
    }
    
    private fun determineRiskLevel(deviationInRangePercentage: Double, zScore: Double): RiskLevel {
        return when {
            deviationInRangePercentage >= (suspiciousBehaviorThreshold * 100) -> RiskLevel.SUSPICIOUS_BEHAVIOR
            zScore > zScoreThreshold -> RiskLevel.HIGH
            deviationInRangePercentage >= 50.0 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }
    
    private fun createKafkaProducer(): KafkaProducer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092"
        props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        props["value.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        props["acks"] = "all"
        props["retries"] = 3
        props["linger.ms"] = 100
        
        return KafkaProducer(props)
    }
    
    data class UserEvent(
        val orderId: String,
        val deviation: Double,
        val riskVerdict: String,
        val eventTime: String
    )
}