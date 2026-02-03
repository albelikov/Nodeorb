package com.logistics.scm.database

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal

object MarketPriceMedians : UUIDTable("market_price_medians") {
    val category = varchar("category", 50)
    val regionId = varchar("region_id", 100)
    val medianValue = decimal("median_value", 15, 2)
    val updatedAt = datetime("updated_at")
    
    init {
        uniqueIndex(category, regionId)
    }
}