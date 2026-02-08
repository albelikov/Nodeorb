package com.nodeorb.shared.order

import com.nodeorb.shared.common.EntityStatus
import com.nodeorb.shared.cargo.CargoDetails
import java.io.Serializable
import java.time.Instant
import java.util.*

data class BusinessOrder(
    val id: UUID = UUID.randomUUID(),
    val orderNumber: String,
    val customerId: UUID,
    val transportRequests: List<UUID> = emptyList(),
    val status: BusinessOrderStatus = BusinessOrderStatus.DRAFT,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) : Serializable

data class TransportRequest(
    val id: UUID = UUID.randomUUID(),
    val businessOrderId: UUID,
    val pickupLocation: com.nodeorb.shared.common.Location,
    val deliveryLocation: com.nodeorb.shared.common.Location,
    val cargo: CargoDetails,
    val status: TransportRequestStatus = TransportRequestStatus.PENDING,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) : Serializable

enum class BusinessOrderStatus : EntityStatus {
    DRAFT {
        override val code = "DRAFT"
        override val description = "Черновик"
        override val isFinal = false
    },
    CONFIRMED {
        override val code = "CONFIRMED"
        override val description = "Подтвержден"
        override val isFinal = false
    },
    IN_PROGRESS {
        override val code = "IN_PROGRESS"
        override val description = "В обработке"
        override val isFinal = false
    },
    DELIVERED {
        override val code = "DELIVERED"
        override val description = "Доставлен"
        override val isFinal = true
    },
    CANCELLED {
        override val code = "CANCELLED"
        override val description = "Отменен"
        override val isFinal = true
    }
}

enum class TransportRequestStatus : EntityStatus {
    PENDING {
        override val code = "PENDING"
        override val description = "В ожидании"
        override val isFinal = false
    },
    ASSIGNED {
        override val code = "ASSIGNED"
        override val description = "Назначен"
        override val isFinal = false
    },
    IN_TRANSIT {
        override val code = "IN_TRANSIT"
        override val description = "В пути"
        override val isFinal = false
    },
    DELIVERED {
        override val code = "DELIVERED"
        override val description = "Доставлен"
        override val isFinal = true
    },
    CANCELLED {
        override val code = "CANCELLED"
        override val description = "Отменен"
        override val isFinal = true
    }
}