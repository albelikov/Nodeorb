package com.autonomous.edge

import org.springframework.stereotype.Service
import java.time.Instant

@Service
class EdgeComputing {

    private val offlineCache = mutableMapOf<String, Any>()
    private var lastSyncTime = Instant.now()

    fun processDataLocally(data: Map<String, Any>): Map<String, Any> {
        // Simple local processing
        return data.mapValues { (_, value) ->
            when (value) {
                is Number -> value.toDouble() * 1.1
                is String -> value.toUpperCase()
                else -> value
            }
        }
    }

    fun isOffline(): Boolean {
        return lastSyncTime.plusMinutes(10).isBefore(Instant.now())
    }

    fun cacheData(key: String, value: Any) {
        offlineCache[key] = value
    }

    fun getCachedData(key: String): Any? {
        return offlineCache[key]
    }

    fun syncWithCloud() {
        lastSyncTime = Instant.now()
        println("Syncing offline data with cloud: $offlineCache")
        offlineCache.clear()
    }

    fun getCacheSize(): Int {
        return offlineCache.size
    }
}