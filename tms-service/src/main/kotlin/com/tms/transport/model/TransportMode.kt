package com.tms.transport.model

import jakarta.persistence.*

/**
 * Тип транспортного средства/вида перевозки
 */
@Entity
@Table(name = "transport_modes")
data class TransportMode(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val code: String,

    @Column(nullable = false)
    val name: String,

    @Column(length = 1000)
    val description: String? = null,

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(nullable = false)
    val supportsMultimodal: Boolean = true,

    @Column(nullable = false)
    val priority: Int = 0
) {
    override fun toString(): String {
        return "TransportMode(id=$id, code='$code', name='$name', active=$active, supportsMultimodal=$supportsMultimodal, priority=$priority)"
    }
}