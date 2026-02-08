package com.fms.repository

import com.fms.model.SparePart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SparePartRepository : JpaRepository<SparePart, UUID> {
    fun findByPartNumber(partNumber: String): SparePart?
    fun findByNameContaining(name: String): List<SparePart>
    fun findBySupplier(supplier: String): List<SparePart>
}