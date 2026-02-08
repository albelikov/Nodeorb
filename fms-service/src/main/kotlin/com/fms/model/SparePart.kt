package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "spare_parts")
data class SparePart(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var partNumber: String = "",

    @Column(nullable = false)
    var name: String = "",

    var description: String = "",

    var quantity: Int = 0,

    var cost: Double = 0.0,

    var supplier: String = "",

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)