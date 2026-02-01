package com.logi.transport

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Транспортная платформа (Transport Platform)
 * 
 * Предоставляет функциональность:
 * - Мультимодальные перевозки
 * - Управление договорами с перевозчиками
 * - Координация различных видов транспорта
 * - Цифровое документооборот
 * - Электронные подписи
 * - Управление тарифами
 * - Интеграция с ж/д и авиа системами
 * - Координация последней мили
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableConfigurationProperties
@EnableScheduling
class LogiTransportPlatformApplication

fun main(args: Array<String>) {
    runApplication<LogiTransportPlatformApplication>(*args)
}
