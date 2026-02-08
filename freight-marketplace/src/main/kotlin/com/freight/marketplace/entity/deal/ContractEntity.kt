package com.freight.marketplace.entity.deal

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * Сущность контракта для управления финансовыми операциями
 */
@Entity
@Table(name = "contracts")
data class ContractEntity(
    @Id
    @GeneratedValue
    val id: UUID? = null,
    
    @Column(nullable = false)
    val masterOrderId: UUID,
    
    @Column(nullable = false)
    val bidId: UUID,
    
    @Column(nullable = false, precision = 15, scale = 2)
    val amount: java.math.BigDecimal,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ContractStatus = ContractStatus.PENDING_FUNDS,
    
    @Column(columnDefinition = "TEXT")
    var evidenceHash: String? = null,
    
    @CreationTimestamp
    @Column(nullable = false)
    val createdAt: LocalDateTime? = null,
    
    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)

/**
 * Статусы контракта для управления жизненным циклом финансовых операций
 */
enum class ContractStatus {
    /**
     * Средства заблокированы, ожидание подтверждения
     */
    PENDING_FUNDS,
    
    /**
     * Средства заблокированы и готовы к использованию
     */
    FUNDED,
    
    /**
     * Груз в пути, средства находятся в эскроу
     */
    IN_TRANSIT,
    
    /**
     * Средства освобождены после успешной доставки
     */
    RELEASED,
    
    /**
     * Спор по сделке, средства заморожены
     */
    DISPUTED
}