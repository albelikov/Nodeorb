package com.nodeorb.shared.common

import java.io.Serializable
import java.time.Instant
import java.util.*

data class Location(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val address: Address? = null
) : Serializable

data class Address(
    val country: String,
    val region: String,
    val city: String,
    val street: String,
    val buildingNumber: String,
    val postalCode: String,
    val additionalInfo: String? = null
) : Serializable

data class ContactInfo(
    val phoneNumber: String,
    val email: String,
    val website: String? = null
) : Serializable

data class Money(
    val amount: java.math.BigDecimal,
    val currency: String = "USD"
) : Serializable {
    fun convertTo(targetCurrency: String, exchangeRate: java.math.BigDecimal): Money {
        return Money(amount.multiply(exchangeRate), targetCurrency)
    }
}

data class Document(
    val id: UUID = UUID.randomUUID(),
    val documentType: String,
    val documentNumber: String,
    val issueDate: java.time.LocalDate,
    val expiryDate: java.time.LocalDate?,
    val issuedBy: String,
    val fileUrl: String?,
    val metadata: Map<String, String> = emptyMap()
) : Serializable

interface Auditable {
    val createdAt: Instant
    val createdBy: UUID
    val updatedAt: Instant
    val updatedBy: UUID
}

interface EntityStatus {
    val code: String
    val description: String
    val isFinal: Boolean
}