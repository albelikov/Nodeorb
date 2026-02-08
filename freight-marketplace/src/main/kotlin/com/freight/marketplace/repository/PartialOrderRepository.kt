package com.freight.marketplace.repository

import com.freight.marketplace.entity.PartialOrderEntity
import com.freight.marketplace.entity.PartialOrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Репозиторий для работы с частичными заказами
 */
interface PartialOrderRepository : JpaRepository<PartialOrderEntity, UUID> {
    
    /**
     * Поиск частичных заказов по ID мастер-заказа
     */
    fun findByMasterOrderId(masterOrderId: UUID, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск всех частичных заказов по ID мастер-заказа (без пагинации)
     */
    fun findByMasterOrderId(masterOrderId: UUID): List<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов по статусу
     */
    fun findByStatus(status: PartialOrderStatus, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов по нескольким статусам
     */
    fun findByStatusIn(statuses: List<PartialOrderStatus>, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов по ID перевозчика
     */
    fun findByAssignedCarrierId(carrierId: UUID, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов по ID заявки
     */
    fun findByAssignedBidId(bidId: UUID, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск доступных для назначения частичных заказов
     */
    fun findByStatusAndAssignedCarrierIdIsNull(status: PartialOrderStatus, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов по диапазону веса
     */
    fun findByWeightBetween(minWeight: BigDecimal, maxWeight: BigDecimal, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов по диапазону объема
     */
    fun findByVolumeBetween(minVolume: BigDecimal, maxVolume: BigDecimal, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов по проценту от общего объема
     */
    fun findByPercentageBetween(minPercentage: Double, maxPercentage: Double, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов по дате создания
     */
    fun findByCreatedAtAfter(createdAt: LocalDateTime, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов по дате обновления
     */
    fun findByUpdatedAtAfter(updatedAt: LocalDateTime, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск просроченных частичных заказов
     */
    @Query("SELECT p FROM PartialOrderEntity p WHERE p.status = :status AND p.createdAt < :cutoffTime")
    fun findExpiredPartialOrders(
        @Param("status") status: PartialOrderStatus,
        @Param("cutoffTime") cutoffTime: LocalDateTime,
        pageable: Pageable
    ): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов, требующих проверки на просроченность
     */
    @Query("SELECT p FROM PartialOrderEntity p WHERE p.status = :status AND p.createdAt BETWEEN :startTime AND :endTime")
    fun findPartialOrdersForExpirationCheck(
        @Param("status") status: PartialOrderStatus,
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime,
        pageable: Pageable
    ): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов по мастер-заказу и статусу
     */
    fun findByMasterOrderIdAndStatus(masterOrderId: UUID, status: PartialOrderStatus, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов по мастер-заказу и нескольким статусам
     */
    fun findByMasterOrderIdAndStatusIn(masterOrderId: UUID, statuses: List<PartialOrderStatus>, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Подсчет частичных заказов по статусу
     */
    @Query("SELECT p.status, COUNT(p) FROM PartialOrderEntity p GROUP BY p.status")
    fun countByStatus(): List<Pair<PartialOrderStatus, Long>>
    
    /**
     * Подсчет частичных заказов по мастер-заказу
     */
    @Query("SELECT p.masterOrder.id, COUNT(p) FROM PartialOrderEntity p GROUP BY p.masterOrder.id")
    fun countByMasterOrder(): List<Pair<UUID, Long>>
    
    /**
     * Суммарный вес частичных заказов по мастер-заказу
     */
    @Query("SELECT p.masterOrder.id, SUM(p.weight) FROM PartialOrderEntity p WHERE p.status = :status GROUP BY p.masterOrder.id")
    fun sumWeightByMasterOrderAndStatus(@Param("status") status: PartialOrderStatus): List<Pair<UUID, BigDecimal>>
    
    /**
     * Суммарный объем частичных заказов по мастер-заказу
     */
    @Query("SELECT p.masterOrder.id, SUM(p.volume) FROM PartialOrderEntity p WHERE p.status = :status GROUP BY p.masterOrder.id")
    fun sumVolumeByMasterOrderAndStatus(@Param("status") status: PartialOrderStatus): List<Pair<UUID, BigDecimal>>
    
    /**
     * Средний процент заполнения по мастер-заказу
     */
    @Query("SELECT p.masterOrder.id, AVG(p.percentage) FROM PartialOrderEntity p WHERE p.status = :status GROUP BY p.masterOrder.id")
    fun avgPercentageByMasterOrderAndStatus(@Param("status") status: PartialOrderStatus): List<Pair<UUID, Double>>
    
    /**
     * Поиск частичных заказов с назначенным перевозчиком
     */
    @Query("SELECT p FROM PartialOrderEntity p WHERE p.assignedCarrierId IS NOT NULL")
    fun findAssignedPartialOrders(pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов без назначенного перевозчика
     */
    @Query("SELECT p FROM PartialOrderEntity p WHERE p.assignedCarrierId IS NULL")
    fun findUnassignedPartialOrders(pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов по диапазону дат
     */
    @Query("SELECT p FROM PartialOrderEntity p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    fun findByCreatedAtBetween(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов с определенным весом
     */
    @Query("SELECT p FROM PartialOrderEntity p WHERE p.weight = :weight")
    fun findByWeight(@Param("weight") weight: BigDecimal, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов с определенным объемом
     */
    @Query("SELECT p FROM PartialOrderEntity p WHERE p.volume = :volume")
    fun findByVolume(@Param("volume") volume: BigDecimal, pageable: Pageable): Page<PartialOrderEntity>
    
    /**
     * Поиск частичных заказов с определенным процентом
     */
    @Query("SELECT p FROM PartialOrderEntity p WHERE p.percentage = :percentage")
    fun findByPercentage(@Param("percentage") percentage: Double, pageable: Pageable): Page<PartialOrderEntity>
    
    companion object {
        val AVAILABLE_STATUSES = listOf(PartialOrderStatus.AVAILABLE, PartialOrderStatus.BIDDING)
        val COMPLETED_STATUSES = listOf(PartialOrderStatus.COMPLETED, PartialOrderStatus.CANCELLED)
    }
}