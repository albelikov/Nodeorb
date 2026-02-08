package com.autonomous.monitoring

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class MetricsCollector(
    private val meterRegistry: MeterRegistry
) {

    private var aiInferenceTime = mutableListOf<Long>()
    private var decisionCount = 0
    private var escalationCount = 0
    private var predictionAccuracy = 0.0
    private var resourceUtilization = 0.0

    @Timed(value = "autonomous_ops_ai_inference_time", description = "Time taken to perform AI inference")
    fun recordInferenceTime(duration: Duration) {
        aiInferenceTime.add(duration.toMillis())
    }

    fun recordDecision() {
        decisionCount++
    }

    fun recordEscalation() {
        escalationCount++
    }

    fun recordPredictionAccuracy(accuracy: Double) {
        predictionAccuracy = accuracy
    }

    fun recordResourceUtilization(utilization: Double) {
        resourceUtilization = utilization
    }

    fun getAverageInferenceTime(): Double {
        return if (aiInferenceTime.isNotEmpty()) {
            aiInferenceTime.average()
        } else {
            0.0
        }
    }

    fun getDecisionCount(): Int {
        return decisionCount
    }

    fun getEscalationCount(): Int {
        return escalationCount
    }

    fun getPredictionAccuracy(): Double {
        return predictionAccuracy
    }

    fun getResourceUtilization(): Double {
        return resourceUtilization
    }

    fun reset() {
        aiInferenceTime.clear()
        decisionCount = 0
        escalationCount = 0
        predictionAccuracy = 0.0
        resourceUtilization = 0.0
    }
}