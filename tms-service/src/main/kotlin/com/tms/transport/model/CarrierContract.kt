package com.tms.transport.model

import jakarta.persistence.*
import java.time.LocalDate

/**
 * Контракт с перевозчиком
 */
@Entity
@Table(name = "carrier_contracts")
data class CarrierContract(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val contractNumber: String,

    @Column(nullable = false)
    val carrierId: Long,

    @Column(nullable = false)
    val carrierName: String,

    @Column(nullable = false)
    val validFrom: LocalDate,

    @Column(nullable = false)
    val validTo: LocalDate,

    @Column(nullable = false)
    val status: String = "ACTIVE", // ACTIVE, EXPIRED, TERMINATED, PENDING

    @Column(nullable = false)
    val version: Int = 1,

    @Column(nullable = false)
    val autoRenewal: Boolean = false,

    @Column(nullable = false)
    val currency: String = "RUB",

    @Column(nullable = false)
    val minWeight: Double = 0.0,

    @Column(nullable = false)
    val maxWeight: Double = Double.MAX_VALUE,

    @Column(nullable = false)
    val minVolume: Double = 0.0,

    @Column(nullable = false)
    val maxVolume: Double = Double.MAX_VALUE,

    @Column(nullable = false)
    val baseRate: Double = 0.0,

    @Column(nullable = false)
    val perKmRate: Double = 0.0,

    @Column(nullable = false)
    val perKgRate: Double = 0.0,

    @Column(nullable = false)
    val perM3Rate: Double = 0.0,

    @Column(nullable = false)
    val fuelSurchargeEnabled: Boolean = true,

    @Column(length = 2000)
    val termsAndConditions: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    val createdBy: String? = null,

    @Column
    val updatedAt: LocalDate? = null,

    @Column
    val updatedBy: String? = null
) {
    override fun toString(): String {
        return "CarrierContract(id=$id, contractNumber='$contractNumber', carrierName='$carrierName', validFrom=$validFrom, validTo=$validTo, status='$status')"
    }
}