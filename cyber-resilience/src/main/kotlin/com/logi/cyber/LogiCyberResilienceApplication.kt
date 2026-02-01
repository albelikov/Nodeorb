package com.logi.cyber

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Киберустойчивость (Cyber Resilience)
 * 
 * Предоставляет функциональность:
 * - Мониторинг и обнаружение угроз
 * - Предотвращение вторжений (IDS/IPS)
 * - Rate limiting и защита от DDoS
 * - Анализ аномалий в поведении
 * - Геолокационные проверки
 * - Vulnerability scanning
 * - Incident response automation
 * - Security compliance reporting
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
class LogiCyberResilienceApplication

fun main(args: Array<String>) {
    runApplication<LogiCyberResilienceApplication>(*args)
}
