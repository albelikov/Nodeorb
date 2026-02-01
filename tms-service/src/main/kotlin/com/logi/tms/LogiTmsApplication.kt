package com.logi.tms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
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
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableConfigurationProperties
@EnableScheduling
class LogiTmsApplication

fun main(args: Array<String>) {
    runApplication<LogiTmsApplication>(*args)
}
