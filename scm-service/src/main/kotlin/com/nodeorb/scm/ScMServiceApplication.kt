package com.nodeorb.scm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Сводный сервис безопасности и контроля (SCM) с киберустойчивостью
 * 
 * Объединяет функциональность:
 * - Аудит безопасности (SCM Audit)
 * - Защита данных (SCM Data Protection) 
 * - Управление идентификацией и доступом (SCM IAM)
 * - Киберустойчивость (Cyber Resilience)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
class ScMServiceApplication

fun main(args: Array<String>) {
    runApplication<ScMServiceApplication>(*args)
}
