package com.tms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Сервис управления транспортировкой (TMS - Transportation Management System)
 *
 * Предоставляет функциональность:
 * - Планирование маршрутов
 * - Оптимизация доставки
 * - Отслеживание грузов
 * - Управление перевозчиками
 * - Расчет стоимости доставки
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties
@EnableScheduling
class TmsServiceApplication

fun main(args: Array<String>) {
    runApplication<TmsServiceApplication>(*args)
}
