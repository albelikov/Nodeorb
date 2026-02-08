package com.wms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
// import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Сервис управления складом (WMS - Warehouse Management System)
 *
 * Предоставляет функциональность:
 * - Управление ячейками хранения
 * - Inventory management
 * - Обработка заказов
 * - Комплектация заказов
 * - Отгрузка и приемка
 * - Интеграция с RFID/сканерами штрих-кодов
 * - ABC-анализ inventory
 */
@SpringBootApplication
// @EnableDiscoveryClient
@EnableJpaAuditing
@EnableConfigurationProperties
@EnableScheduling
class WmsServiceApplication

fun main(args: Array<String>) {
    runApplication<WmsServiceApplication>(*args)
}