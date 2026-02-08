package com.tms.transport.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Конфигурация транспортной платформы
 */
@Component
@ConfigurationProperties(prefix = "nodeorb.transport")
data class TransportPlatformConfig(
    val multimodal: MultimodalConfig = MultimodalConfig(),
    val contracts: ContractsConfig = ContractsConfig(),
    val tariffs: TariffsConfig = TariffsConfig(),
    val integrations: IntegrationsConfig = IntegrationsConfig(),
    val lastMile: LastMileConfig = LastMileConfig()
)

/**
 * Конфигурация мультимодальных перевозок
 */
data class MultimodalConfig(
    val enabled: Boolean = true,
    val modes: List<String> = listOf("road", "rail", "air", "sea", "courier"),
    val transferPoints: TransferPointsConfig = TransferPointsConfig()
)

/**
 * Конфигурация точек пересадки
 */
data class TransferPointsConfig(
    val maxHubSize: Int = 100,
    val transferTimeBuffer: Int = 3600
)

/**
 * Конфигурация управления договорами
 */
data class ContractsConfig(
    val versioning: Boolean = true,
    val approvalWorkflow: Boolean = true,
    val autoRenewal: Boolean = false,
    val notificationDays: Int = 30
)

/**
 * Конфигурация тарифов
 */
data class TariffsConfig(
    val calculationMode: String = "weight-distance",
    val currency: String = "RUB",
    val fuelSurchargeEnabled: Boolean = true,
    val fuelSurchargeAdjustment: String = "daily"
)

/**
 * Конфигурация интеграций с внешними системами
 */
data class IntegrationsConfig(
    val railway: RailwayConfig = RailwayConfig(),
    val air: AirConfig = AirConfig(),
    val sea: SeaConfig = SeaConfig()
)

/**
 * Конфигурация интеграции с железнодорожными системами
 */
data class RailwayConfig(
    val enabled: Boolean = true,
    val apiUrl: String = ""
)

/**
 * Конфигурация интеграции с авиа системами
 */
data class AirConfig(
    val enabled: Boolean = true,
    val apiUrl: String = ""
)

/**
 * Конфигурация интеграции с морскими системами
 */
data class SeaConfig(
    val enabled: Boolean = true,
    val apiUrl: String = ""
)

/**
 * Конфигурация последней мили
 */
data class LastMileConfig(
    val enabled: Boolean = true,
    val providers: List<String> = listOf("dpd", "cdek", "russian-post"),
    val optimization: Boolean = true
)