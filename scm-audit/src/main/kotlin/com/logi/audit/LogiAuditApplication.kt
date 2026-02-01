package com.logi.audit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Модуль аудита системы (SCM-Audit)
 * 
 * Предоставляет функциональность:
 * - Логирование всех действий пользователей
 * - Отслеживание изменений данных
 * - Хранение аудиторских событий
 * - Аналитика активности
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
class LogiAuditApplication

fun main(args: Array<String>) {
    runApplication<LogiAuditApplication>(*args)
}
