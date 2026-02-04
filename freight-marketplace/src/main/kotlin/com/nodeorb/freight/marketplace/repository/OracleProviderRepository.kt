package com.nodeorb.freight.marketplace.repository

import com.nodeorb.freight.marketplace.entity.OracleProviderEntity
import com.nodeorb.freight.marketplace.entity.ProviderType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Репозиторий для управления сущностями провайдеров
 */
@Repository
interface OracleProviderRepository : JpaRepository<OracleProviderEntity, UUID> {

    /**
     * Находит активные провайдеры, отсортированные по приоритету
     */
    fun findByIsEnabledTrueOrderByPriorityAsc(): List<OracleProviderEntity>

    /**
     * Находит провайдеры по типу
     */
    fun findByProviderType(providerType: ProviderType): List<OracleProviderEntity>

    /**
     * Находит активные провайдеры по типу
     */
    fun findByProviderTypeAndIsEnabledTrue(providerType: ProviderType): List<OracleProviderEntity>

    /**
     * Проверяет существование провайдера по имени
     */
    fun existsByName(name: String): Boolean

    /**
     * Проверяет существование провайдера по имени (кроме указанного ID)
     */
    fun existsByNameAndIdNot(name: String, id: UUID): Boolean

    /**
     * Находит провайдера по имени
     */
    fun findByName(name: String): OracleProviderEntity?

    /**
     * Находит активные провайдеры, включенные в консенсус
     */
    @Query("SELECT p FROM OracleProviderEntity p WHERE p.isEnabled = true AND p.consensusEnabled = true ORDER BY p.priority ASC")
    fun findActiveConsensusProviders(): List<OracleProviderEntity>

    /**
     * Находит провайдера с максимальным приоритетом (основной)
     */
    @Query("SELECT p FROM OracleProviderEntity p WHERE p.isEnabled = true ORDER BY p.priority ASC")
    fun findPrimaryProvider(): OracleProviderEntity?

    /**
     * Обновляет вес провайдера
     */
    @Query("UPDATE OracleProviderEntity p SET p.weight = :weight, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    fun updateWeight(@Param("id") id: UUID, @Param("weight") weight: Double): Int

    /**
     * Обновляет статус включения
     */
    @Query("UPDATE OracleProviderEntity p SET p.isEnabled = :enabled, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    fun updateEnabled(@Param("id") id: UUID, @Param("enabled") enabled: Boolean): Int

    /**
     * Обновляет приоритет
     */
    @Query("UPDATE OracleProviderEntity p SET p.priority = :priority, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    fun updatePriority(@Param("id") id: UUID, @Param("priority") priority: Int): Int

    /**
     * Обновляет статус консенсуса
     */
    @Query("UPDATE OracleProviderEntity p SET p.consensusEnabled = :consensusEnabled, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    fun updateConsensusEnabled(@Param("id") id: UUID, @Param("consensusEnabled") consensusEnabled: Boolean): Int

    /**
     * Считает количество активных провайдеров
     */
    @Query("SELECT COUNT(p) FROM OracleProviderEntity p WHERE p.isEnabled = true")
    fun countActiveProviders(): Long

    /**
     * Считает количество провайдеров в консенсусе
     */
    @Query("SELECT COUNT(p) FROM OracleProviderEntity p WHERE p.isEnabled = true AND p.consensusEnabled = true")
    fun countConsensusProviders(): Long
}