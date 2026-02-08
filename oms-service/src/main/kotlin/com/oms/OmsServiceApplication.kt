package com.oms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
// import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Сервис управления заказами (OMS - Order Management System)
 *
 * Предоставляет функциональность:
 * - Создание и обработка заказов
 * - Управление жизненным циклом заказа
 * - Расчет стоимости доставки
 * - Выбор оптимального маршрута
 * - Отслеживание статуса заказа
 * - Обработка возвратов
 * - Интеграция с TMS и WMS
 */
@SpringBootApplication
// @EnableDiscoveryClient
@EnableJpaAuditing
@EnableConfigurationProperties
@EnableScheduling
class OmsServiceApplication

fun main(args: Array<String>) {
    runApplication<OmsServiceApplication>(*args)
}