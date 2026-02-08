package com.fms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Сервис управления автопарком (FMS - Fleet Management System)
 *
 * Предоставляет функциональность:
 * - Управление транспортными средствами
 * - Мониторинг местоположения в реальном времени
 * - Отслеживание состояния транспортных средств
 * - Управление водителями
 * - Техническое обслуживание
 * - Интеграция с телематическими системами
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties
@EnableScheduling
class FmsServiceApplication

fun main(args: Array<String>) {
    runApplication<FmsServiceApplication>(*args)
}
