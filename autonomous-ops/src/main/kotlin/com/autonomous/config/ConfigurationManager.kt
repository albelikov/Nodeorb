package com.autonomous.config

import org.springframework.stereotype.Service

@Service
class ConfigurationManager {

    private val config = mutableMapOf<String, Any>(
        "ai.enabled" to true,
        "model.path" to "/models/autonomous/",
        "inference.timeout" to 5000,
        "cache.ttl" to 3600,
        "decision.mode" to "autonomous",
        "human.oversight" to true,
        "escalation.threshold" to 0.85,
        "max.autonomous.decisions" to 100,
        "optimization.enabled" to true,
        "optimization.interval" to 30000,
        "window.size" to 3600000,
        "predictive.maintenance.enabled" to true,
        "alert.threshold" to 0.7,
        "prediction.horizon" to 604800000,
        "edge.enabled" to true,
        "sync.interval" to 10000,
        "offline.mode" to true,
        "autonomous.vehicles.enabled" to true,
        "protocol" to "v2x",
        "safety.mode" to "active",
        "max.speed" to 60
    )

    fun getProperty(key: String, defaultValue: Any): Any {
        return config.getOrDefault(key, defaultValue)
    }

    fun setProperty(key: String, value: Any) {
        config[key] = value
    }

    fun getBooleanProperty(key: String, defaultValue: Boolean): Boolean {
        return getProperty(key, defaultValue).toString().toBoolean()
    }

    fun getNumberProperty(key: String, defaultValue: Number): Number {
        val value = getProperty(key, defaultValue)
        return when (value) {
            is Number -> value
            is String -> value.toDoubleOrNull() ?: defaultValue
            else -> defaultValue
        }
    }

    fun getStringProperty(key: String, defaultValue: String): String {
        return getProperty(key, defaultValue).toString()
    }

    fun reloadConfiguration() {
        println("Reloading configuration from external source")
    }

    fun getAllProperties(): Map<String, Any> {
        return config.toMap()
    }
}