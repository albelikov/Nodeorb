package com.freight.marketplace.repository

import com.freight.marketplace.entity.MasterOrderEntity
import com.freight.marketplace.entity.MasterOrderStatus
import com.freight.marketplace.entity.PartialOrderEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Репозиторий для работы с мастер-заказами
 */
interface MasterOrderRepository : JpaRepository<MasterOrderEntity, UUID> {
    
    /**
     * Поиск заказов по ID грузоотправителя
     */
    fun findByShipperId(shipperId: UUID, pageable: Pageable): Page<MasterOrderEntity>
    
    /**
     * Поиск всех заказов по ID грузоотправителя (без пагинации)
     */
    fun findByShipperId(shipperId: UUID): List<MasterOrderEntity>
    
    /**
     * Подсчет заказов по ID грузоотправителя
     */
    fun countByShipperId(shipperId: UUID): Long
    
    /**
     * Поиск заказов по ID грузоотправителя и нескольким статусам
     */
    fun findByShipperIdAndStatusIn(shipperId: UUID, statuses: List<MasterOrderStatus>): List<MasterOrderEntity>
    
    /**
     * Поиск заказов по ID грузоотправителя и статусу
     */
    fun findByShipperIdAndStatus(shipperId: UUID, status: MasterOrderStatus): List<MasterOrderEntity>
    
    /**
     * Поиск заказов по статусу
     */
    fun findByStatus(status: MasterOrderStatus, pageable: Pageable): Page<MasterOrderEntity>
    
    /**
     * Поиск заказов по нескольким статусам
     */
    fun findByStatusIn(statuses: List<MasterOrderStatus>, pageable: Pageable): Page<MasterOrderEntity>
    
    /**
     * Поиск активных заказов (открытых и частично заполненных)
     */
    fun findByStatusInAndRequiredDeliveryDateAfter(
        statuses: List<MasterOrderStatus>,
        cutoffDate: LocalDateTime,
        pageable: Pageable
    ): Page<MasterOrderEntity>
    
    /**
     * Поиск заказов по типу груза
     */
    fun findByCargoType(cargoType: String, pageable: Pageable): Page<MasterOrderEntity>
    
    /**
     * Поиск заказов по диапазону веса
     */
    fun findByTotalWeightBetween(minWeight: BigDecimal, maxWeight: BigDecimal, pageable: Pageable): Page<MasterOrderEntity>
    
    /**
     * Поиск заказов по диапазону объема
     */
    fun findByTotalVolumeBetween(minVolume: BigDecimal, maxVolume: BigDecimal, pageable: Pageable): Page<MasterOrderEntity>
    
    /**
     * Поиск заказов по дате создания
     */
    fun findByCreatedAtAfter(createdAt: LocalDateTime, pageable: Pageable): Page<MasterOrderEntity>
    
    /**
     * Поиск заказов по дате доставки
     */
    fun findByRequiredDeliveryDateBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable
    ): Page<MasterOrderEntity>
    
    /**
     * Поиск заказов с низким уровнем заполненности
     */
    @Query("SELECT m FROM MasterOrderEntity m WHERE m.remainingWeight > :minRemainingWeight AND m.status = :status")
    fun findByRemainingWeightGreaterThanAndStatus(
        @Param("minRemainingWeight") minRemainingWeight: BigDecimal,
        @Param("status") status: MasterOrderStatus,
        pageable: Pageable
    ): Page<MasterOrderEntity>
    
    /**
     * Поиск заказов, подходящих для автоматической отмены
     */
    @Query("SELECT m FROM MasterOrderEntity m WHERE m.requiredDeliveryDate < :cutoffTime AND m.status IN :statuses")
    fun findOrdersForAutoCancellation(
        @Param("cutoffTime") cutoffTime: LocalDateTime,
        @Param("statuses") statuses: List<MasterOrderStatus>,
        pageable: Pageable
    ): Page<MasterOrderEntity>
    
    /**
     * Поиск заказов, требующих напоминания
     */
    @Query("SELECT m FROM MasterOrderEntity m WHERE m.requiredDeliveryDate BETWEEN :startTime AND :endTime AND m.status IN :statuses")
    fun findOrdersForReminder(
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime,
        @Param("statuses") statuses: List<MasterOrderStatus>,
        pageable: Pageable
    ): Page<MasterOrderEntity>
    
    /**
     * Подсчет заказов по статусу
     */
    @Query("SELECT m.status, COUNT(m) FROM MasterOrderEntity m GROUP BY m.status")
    fun countByStatus(): List<Pair<MasterOrderStatus, Long>>
    
    /**
     * Подсчет заказов по типу груза
     */
    @Query("SELECT m.cargoType, COUNT(m) FROM MasterOrderEntity m GROUP BY m.cargoType")
    fun countByCargoType(): List<Pair<String, Long>>
    
    /**
     * Средний вес заказов по грузоотправителю
     */
    @Query("SELECT AVG(m.totalWeight) FROM MasterOrderEntity m WHERE m.shipperId = :shipperId")
    fun findAverageWeightByShipperId(@Param("shipperId") shipperId: UUID): BigDecimal?
    
    /**
     * Средний объем заказов по грузоотправителю
     */
    @Query("SELECT AVG(m.totalVolume) FROM MasterOrderEntity m WHERE m.shipperId = :shipperId")
    fun findAverageVolumeByShipperId(@Param("shipperId") shipperId: UUID): BigDecimal?
    
    /**
     * Поиск заказов с высоким приоритетом (мало времени до доставки)
     */
    @Query("SELECT m FROM MasterOrderEntity m WHERE m.requiredDeliveryDate BETWEEN :now AND :deadline AND m.status IN :statuses")
    fun findHighPriorityOrders(
        @Param("now") now: LocalDateTime,
        @Param("deadline") deadline: LocalDateTime,
        @Param("statuses") statuses: List<MasterOrderStatus>,
        pageable: Pageable
    ): Page<MasterOrderEntity>
    
    /**
     * Поиск заказов по географическому местоположению
     */
    @Query("SELECT m FROM MasterOrderEntity m WHERE ST_DWithin(m.pickupLocation, :location, :maxDistance)")
    fun findNearbyOrders(
        @Param("location") location: org.locationtech.jts.geom.Point,
        @Param("maxDistance") maxDistance: Double,
        pageable: Pageable
    ): Page<MasterOrderEntity>
    
    /**
     * Поиск заказов с определенным уровнем LTL
     */
    @Query("SELECT m FROM MasterOrderEntity m WHERE m.isLtlEnabled = :ltlEnabled")
    fun findByLtlEnabled(@Param("ltlEnabled") ltlEnabled: Boolean, pageable: Pageable): Page<MasterOrderEntity>
    
    /**
     * Поиск заказов по минимальному проценту загрузки
     */
    @Query("SELECT m FROM MasterOrderEntity m WHERE m.minLoadPercentage >= :minLoadPercentage")
    fun findByMinLoadPercentageGreaterThanEqual(
        @Param("minLoadPercentage") minLoadPercentage: Double,
        pageable: Pageable
    ): Page<MasterOrderEntity>
    
    /**
     * Поиск заказов с просроченной погрузкой
     */
    @Query("SELECT m FROM MasterOrderEntity m WHERE m.requiredDeliveryDate < :cutoffDate AND m.status NOT IN :completedStatuses")
    fun findOverdueOrders(
        @Param("cutoffDate") cutoffDate: LocalDateTime,
        @Param("completedStatuses") completedStatuses: List<MasterOrderStatus>,
        pageable: Pageable
    ): Page<MasterOrderEntity>
    
    /**
     * Поиск заказов по диапазону дат доставки
     */
    @Query("SELECT m FROM MasterOrderEntity m WHERE m.requiredDeliveryDate BETWEEN :startDate AND :endDate AND m.status = :status")
    fun findByRequiredDeliveryDateRangeAndStatus(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        @Param("status") status: MasterOrderStatus,
        pageable: Pageable
    ): Page<MasterOrderEntity>
    
    companion object {
        val ACTIVE_STATUSES = listOf(MasterOrderStatus.OPEN, MasterOrderStatus.PARTIALLY_FILLED)
        val COMPLETED_STATUSES = listOf(MasterOrderStatus.COMPLETED, MasterOrderStatus.CANCELLED)
    }
}