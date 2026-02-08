package com.nodeorb.shared.cargo

import java.io.Serializable
import java.math.BigDecimal

data class CargoDetails(
    val weight: BigDecimal,
    val volume: BigDecimal,
    val packageCount: Int,
    val cargoType: CargoType,
    val description: String? = null,
    val specialHandling: String? = null,
    val temperatureMin: BigDecimal? = null,
    val temperatureMax: BigDecimal? = null,
    val hazmat: Boolean = false,
    val hazmatClass: String? = null
) : Serializable

enum class CargoType {
    GENERAL,
    PERISHABLE,
    DANGEROUS,
    FRAGILE,
    OVERSIZE,
    LIQUID,
    REFRIGERATED
}