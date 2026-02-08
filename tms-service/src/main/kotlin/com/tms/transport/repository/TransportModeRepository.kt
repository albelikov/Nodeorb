package com.tms.transport.repository

import com.tms.transport.model.TransportMode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Репозиторий для работы с типами транспорта
 */
@Repository
interface TransportModeRepository : JpaRepository<TransportMode, Long> {

    /**
     * Найти активные типы транспорта
     */
    fun findByActiveTrue(): List<TransportMode>

    /**
     * Найти тип транспорта по коду
     */
    fun findByCode(code: String): TransportMode?

    /**
     * Найти типы транспорта, поддерживающие мультимодальные перевозки
     */
    fun findBySupportsMultimodalTrueAndActiveTrue(): List<TransportMode>
}