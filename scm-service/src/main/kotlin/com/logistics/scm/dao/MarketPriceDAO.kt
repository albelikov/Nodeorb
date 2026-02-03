package com.logistics.scm.dao

import com.logistics.scm.database.MarketPriceMedians
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime

class MarketPriceDAO {
    
    fun getMedian(category: String, regionId: String): BigDecimal? {
        return transaction {
            MarketPriceMedians
                .select { (MarketPriceMedians.category eq category) and (MarketPriceMedians.regionId eq regionId) }
                .orderBy(MarketPriceMedians.updatedAt to SortOrder.DESC)
                .limit(1)
                .map { it[MarketPriceMedians.medianValue] }
                .firstOrNull()
        }
    }
    
    fun updateMedian(category: String, regionId: String, medianValue: BigDecimal) {
        transaction {
            val existing = MarketPriceMedians
                .select { (MarketPriceMedians.category eq category) and (MarketPriceMedians.regionId eq regionId) }
                .firstOrNull()
            
            if (existing != null) {
                MarketPriceMedians.update({ 
                    (MarketPriceMedians.category eq category) and (MarketPriceMedians.regionId eq regionId) 
                }) {
                    it[this.medianValue] = medianValue
                    it[this.updatedAt] = LocalDateTime.now()
                }
            } else {
                MarketPriceMedians.insert {
                    it[this.category] = category
                    it[this.regionId] = regionId
                    it[this.medianValue] = medianValue
                    it[this.updatedAt] = LocalDateTime.now()
                }
            }
        }
    }
}