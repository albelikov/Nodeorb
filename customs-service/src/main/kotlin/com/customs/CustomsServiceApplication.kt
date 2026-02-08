package com.customs

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Таможенный сервис (Customs Service)
 *
 * Предоставляет функциональность:
 * - Оформление таможенных деклараций
 * - Электронный документооборот с таможней
 * - Расчет таможенных платежей
 * - Валидация товаров и классификация ТН ВЭД
 * - Интеграция с ЕАЭС системами
 * - Генерация PDF деклараций
 * - Архивирование документов
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableConfigurationProperties
@EnableScheduling
class CustomsServiceApplication

fun main(args: Array<String>) {
    runApplication<CustomsServiceApplication>(*args)
}