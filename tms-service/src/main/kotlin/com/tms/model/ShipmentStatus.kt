package com.tms.model

enum class ShipmentStatus {
    CREATED,
    PLANNING,
    DISPATCHED,
    IN_TRANSIT,
    AT_PICKUP,
    AT_DELIVERY,
    DELIVERED,
    CANCELLED,
    DELAYED,
    EXCEPTION
}