package com.autonomous.controller

import com.autonomous.data.entities.CostParameters
import com.autonomous.services.CostParametersService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/cost-parameters")
class CostParametersController(
    private val costParametersService: CostParametersService
) {

    @PostMapping
    fun createCostParameters(@RequestBody costParameters: CostParameters): ResponseEntity<CostParameters> {
        val createdCostParams = costParametersService.createCostParameters(costParameters.copy(
            validFrom = costParameters.validFrom ?: Instant.now()
        ))
        return ResponseEntity.ok(createdCostParams)
    }

    @GetMapping("/{parameterId}")
    fun getCostParameters(@PathVariable parameterId: String): ResponseEntity<CostParameters?> {
        val costParams = costParametersService.getCostParameters(parameterId)
        return if (costParams != null) {
            ResponseEntity.ok(costParams)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{parameterId}")
    fun updateCostParameters(
        @PathVariable parameterId: String,
        @RequestBody costParameters: CostParameters
    ): ResponseEntity<CostParameters?> {
        val existingCostParams = costParametersService.getCostParameters(parameterId)
        return if (existingCostParams != null) {
            val updatedCostParams = costParametersService.updateCostParameters(costParameters.copy(
                id = existingCostParams.id,
                parameterId = existingCostParams.parameterId
            ))
            ResponseEntity.ok(updatedCostParams)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{parameterId}")
    fun deleteCostParameters(@PathVariable parameterId: String): ResponseEntity<Void> {
        costParametersService.deleteCostParameters(parameterId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun listCostParameters(@RequestParam(required = false) region: String?): ResponseEntity<List<CostParameters>> {
        val costParams = costParametersService.listCostParameters(region)
        return ResponseEntity.ok(costParams)
    }

    @GetMapping("/active")
    fun getActiveCostParameters(): ResponseEntity<List<CostParameters>> {
        val costParams = costParametersService.getActiveCostParameters()
        return ResponseEntity.ok(costParams)
    }

    @GetMapping("/default")
    fun getDefaultCostParameters(): ResponseEntity<CostParameters?> {
        val costParams = costParametersService.getDefaultCostParameters()
        return if (costParams != null) {
            ResponseEntity.ok(costParams)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/region/{region}")
    fun getCostParametersForRegion(@PathVariable region: String): ResponseEntity<List<CostParameters>> {
        val costParams = costParametersService.getCostParametersForRegion(region)
        return if (costParams.isNotEmpty()) {
            ResponseEntity.ok(costParams)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}