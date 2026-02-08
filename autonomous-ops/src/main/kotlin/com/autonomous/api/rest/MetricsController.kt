package com.autonomous.api.rest

import com.autonomous.monitoring.MetricsCollector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/metrics")
class MetricsController {

    @Autowired
    private lateinit var metricsCollector: MetricsCollector

    @GetMapping
    fun getMetrics(): Map<String, Any> {
        return mapOf(
            "ai_inference_time" to metricsCollector.getAverageInferenceTime(),
            "decision_count" to metricsCollector.getDecisionCount(),
            "escalation_count" to metricsCollector.getEscalationCount(),
            "prediction_accuracy" to metricsCollector.getPredictionAccuracy(),
            "resource_utilization" to metricsCollector.getResourceUtilization()
        )
    }

    @PostMapping("/reset")
    fun resetMetrics(): Map<String, Any> {
        metricsCollector.reset()
        return mapOf("success" to true, "message" to "Metrics reset")
    }
}