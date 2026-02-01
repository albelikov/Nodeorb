package com.logi.reverselogistics

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Обратная логистика (Reverse Logistics)
 * 
 * Предоставляет функциональность:
 * - Обработка возвратов от клиентов
 * - Управление гарантийными случаями
 * - Контроль качества возвращенных товаров
 * - Переработка и утилизация
 * - Возврат поставщикам (RMA)
 * - Списание и утилизация
 * - Аналитика возвратов
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableConfigurationProperties
@EnableScheduling
class LogiReverseLogisticsApplication

fun main(args: Array<String>) {
    runApplication<LogiReverseLogisticsApplication>(*args)
}
