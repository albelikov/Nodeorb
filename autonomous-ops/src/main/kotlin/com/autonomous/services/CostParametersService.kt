package com.autonomous.services

import com.autonomous.data.entities.CostParameters
import com.autonomous.data.repositories.CostParametersRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CostParametersService(
    private val costParametersRepository: CostParametersRepository
) {

    fun createCostParameters(costParameters: CostParameters): CostParameters {
        return costParametersRepository.save(costParameters)
    }

    fun getCostParameters(parameterId: String): CostParameters? {
        return costParametersRepository.findByParameterId(parameterId)
    }

    fun updateCostParameters(costParameters: CostParameters): CostParameters {
        return costParametersRepository.save(costParameters)
    }

    fun deleteCostParameters(parameterId: String) {
        costParametersRepository.findByParameterId(parameterId)?.let {
            costParametersRepository.delete(it)
        }
    }

    fun listCostParameters(region: String? = null): List<CostParameters> {
        return when {
            region != null -> costParametersRepository.findByRegion(region)
            else -> costParametersRepository.findAll()
        }
    }

    fun getActiveCostParameters(): List<CostParameters> {
        val now = Instant.now()
        return costParametersRepository.findAll().filter {
            it.validFrom <= now && (it.validUntil == null || it.validUntil > now)
        }
    }

    fun getDefaultCostParameters(): CostParameters? {
        return costParametersRepository.findByParameterId("default")
    }

    fun getCostParametersForRegion(region: String): List<CostParameters> {
        val now = Instant.now()
        return costParametersRepository.findByRegion(region).filter {
            it.validFrom <= now && (it.validUntil == null || it.validUntil > now)
        }
    }
}