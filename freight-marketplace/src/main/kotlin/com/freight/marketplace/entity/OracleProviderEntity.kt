package com.freight.marketplace.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * Сущность провайдера данных для Market Oracle
 * Хранит настройки провайдеров в базе данных
 */
@Entity
@Table(name = "insight_providers")
data class OracleProviderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "provider_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var providerType: ProviderType,

    @Column(name = "api_url")
    var apiUrl: String? = null,

    @Column(name = "api_key")
    var apiKey: String? = null,

    @Column(name = "data_file_path")
    var dataFilePath: String? = null,

    @Column(name = "region")
    var region: String? = null,

    @Column(name = "weight", nullable = false)
    var weight: Double = 1.0,

    @Column(name = "is_enabled", nullable = false)
    var isEnabled: Boolean = true,

    @Column(name = "priority", nullable = false)
    var priority: Int = 1,

    @Column(name = "consensus_enabled", nullable = false)
    var consensusEnabled: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)

/**
 * Типы провайдеров для хранения в БД
 */
enum class ProviderType {
    OIL_BULLETIN,    // Реальный HTTP-клиент к внешнему API
    REGIONAL_JSON,   // Парсер локальных данных
    MOCK             // Резервный провайдер
}

/**
 * DTO для создания нового провайдера
 */
data class CreateOracleProviderRequest(
    val name: String,
    val providerType: ProviderType,
    val apiUrl: String? = null,
    val apiKey: String? = null,
    val dataFilePath: String? = null,
    val region: String? = null,
    val weight: Double = 1.0,
    val isEnabled: Boolean = true,
    val priority: Int = 1,
    val consensusEnabled: Boolean = false
)

/**
 * DTO для обновления провайдера
 */
data class UpdateOracleProviderRequest(
    val name: String? = null,
    val apiUrl: String? = null,
    val apiKey: String? = null,
    val dataFilePath: String? = null,
    val region: String? = null,
    val weight: Double? = null,
    val isEnabled: Boolean? = null,
    val priority: Int? = null,
    val consensusEnabled: Boolean? = null
)

/**
 * DTO для ответа с информацией о провайдере
 */
data class OracleProviderResponse(
    val id: UUID,
    val name: String,
    val providerType: ProviderType,
    val apiUrl: String?,
    val dataFilePath: String?,
    val region: String?,
    val weight: Double,
    val isEnabled: Boolean,
    val priority: Int,
    val consensusEnabled: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)