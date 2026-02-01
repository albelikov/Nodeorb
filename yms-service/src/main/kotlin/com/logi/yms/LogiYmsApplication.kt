package com.logi.yms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Сервис управления территорией (YMS - Yard Management System)
 * 
 * Предоставляет функциональность:
 * - Управление парком грузовиков (yard)
 * - Планирование и оптимизация yard операций
 * - Координация водителей и прицепов
 * - Мониторинг занятости слотов
 * - Управление доками (door management)
 * - Интеграция с WMS для приемки/отгрузки
 * - Аналитика использования территории
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableConfigurationProperties
@EnableScheduling
class LogiYmsApplication

fun main(args: Array<String>) {
    runApplication<LogiYmsApplication>(*args)
}
