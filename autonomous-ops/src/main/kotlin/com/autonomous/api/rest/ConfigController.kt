package com.autonomous.api.rest

import com.autonomous.config.ConfigurationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/config")
class ConfigController {

    @Autowired
    private lateinit var configManager: ConfigurationManager

    @GetMapping("/properties")
    fun getAllProperties(): Map<String, Any> {
        return configManager.getAllProperties()
    }

    @GetMapping("/properties/{key}")
    fun getProperty(@PathVariable key: String, @RequestParam(defaultValue = "null") defaultValue: String): Any {
        return configManager.getProperty(key, if (defaultValue == "null") null else defaultValue)
    }

    @PostMapping("/properties")
    fun setProperty(@RequestBody request: ConfigRequest): Map<String, Any> {
        configManager.setProperty(request.key, request.value)
        return mapOf("success" to true, "message" to "Property updated")
    }

    @DeleteMapping("/properties/{key}")
    fun removeProperty(@PathVariable key: String): Map<String, Any> {
        // Not implemented in ConfigurationManager
        return mapOf("success" to false, "message" to "Not implemented")
    }

    @PostMapping("/reload")
    fun reloadConfig(): Map<String, Any> {
        configManager.reloadConfiguration()
        return mapOf("success" to true, "message" to "Configuration reloaded")
    }
}

data class ConfigRequest(
    val key: String,
    val value: Any
)