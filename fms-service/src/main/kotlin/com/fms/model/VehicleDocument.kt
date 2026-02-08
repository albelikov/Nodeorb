package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "vehicle_documents")
data class VehicleDocument(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    var vehicle: Vehicle? = null,

    @Column(nullable = false)
    var documentType: String = "",

    @Column(nullable = false)
    var documentNumber: String = "",

    @Column(nullable = false)
    var issueDate: LocalDateTime = LocalDateTime.now(),

    var expiryDate: LocalDateTime? = null,

    var fileUrl: String = "",

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)