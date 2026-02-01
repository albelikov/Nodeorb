package com.logi.iam

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * Модуль управления идентификацией и доступом (IAM)
 * 
 * Предоставляет функциональность:
 * - Аутентификация и авторизация пользователей
 * - Управление ролями и разрешениями
 * - Интеграция с Keycloak
 * - JWT токены
 * - Rate limiting
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableConfigurationProperties
class LogiIamApplication

fun main(args: Array<String>) {
    runApplication<LogiIamApplication>(*args)
}
