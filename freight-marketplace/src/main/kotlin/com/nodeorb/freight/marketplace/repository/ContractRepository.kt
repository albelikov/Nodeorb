package com.nodeorb.freight.marketplace.repository

import com.nodeorb.freight.marketplace.entity.deal.ContractEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Репозиторий для управления контрактами
 */
@Repository
interface ContractRepository : JpaRepository<ContractEntity, UUID> {

    /**
     * Находит контракт по ID ставки
     * @param bidId ID ставки
     * @return Контракт или null
     */
    fun findByBidId(bidId: UUID): ContractEntity?

    /**
     * Находит контракт по ID заказа
     * @param masterOrderId ID заказа
     * @return Список контрактов
     */
    fun findByMasterOrderId(masterOrderId: UUID): List<ContractEntity>

    /**
     * Находит контракты по статусу
     * @param status Статус контракта
     * @return Список контрактов
     */
    fun findByStatus(status: com.nodeorb.freight.marketplace.entity.deal.ContractStatus): List<ContractEntity>

    /**
     * Находит контракты в определенном статусе для определенного перевозчика
     * @param carrierId ID перевозчика
     * @param status Статус контракта
     * @return Список контрактов
     */
    @Query("SELECT c FROM ContractEntity c WHERE c.bidId IN " +
           "(SELECT b.id FROM BidEntity b WHERE b.carrierId = :carrierId) " +
           "AND c.status = :status")
    fun findByCarrierIdAndStatus(
        @Param("carrierId") carrierId: UUID,
        @Param("status") status: com.nodeorb.freight.marketplace.entity.deal.ContractStatus
    ): List<ContractEntity>

    /**
     * Проверяет, существует ли контракт для ставки
     * @param bidId ID ставки
     * @return true, если контракт существует
     */
    @Query("SELECT COUNT(c) > 0 FROM ContractEntity c WHERE c.bidId = :bidId")
    fun existsByBidId(@Param("bidId") bidId: UUID): Boolean

    /**
     * Находит контракты в статусе IN_TRANSIT для проверки e-POD
     * @return Список контрактов, ожидающих подтверждения доставки
     */
    fun findByStatus(status: com.nodeorb.freight.marketplace.entity.deal.ContractStatus): List<ContractEntity>
}