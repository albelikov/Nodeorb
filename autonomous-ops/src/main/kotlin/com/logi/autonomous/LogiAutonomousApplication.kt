package com.logi.autonomous

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Автономные операции (Autonomous Operations)
 * 
 * Предоставляет функциональность:
 * - Автономное принятие решений на основе AI/ML
 * - Оптимизация маршрутов в реальном времени
 * - Автоматическое перераспределение ресурсов
 * - Предиктивное обслуживание
 * - Адаптивное управление рисками
 * - Edge computing для критических операций
 * - Интеграция с автономным транспортом
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
class LogiAutonomousApplication

fun main(args: Array<String>) {
    runApplication<LogiAutonomousApplication>(*args)
}
