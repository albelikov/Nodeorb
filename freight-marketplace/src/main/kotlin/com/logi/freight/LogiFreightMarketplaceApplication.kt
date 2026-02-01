package com.logi.freight

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * Грузовая биржа (Freight Marketplace)
 * 
 * Предоставляет функциональность:
 * - Публикация грузов и транспорта
 * - Поиск и сопоставление грузов/транспорта
 * - Аукционное ценообразование
 * - Рейтинговая система перевозчиков
 * - Управление договорами
 * - Эскроу-счета для расчетов
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableConfigurationProperties
class LogiFreightMarketplaceApplication

fun main(args: Array<String>) {
    runApplication<LogiFreightMarketplaceApplication>(*args)
}
