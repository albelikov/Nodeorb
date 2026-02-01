package com.logi.dataprotection

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

/**
 * Модуль защиты данных (SCM-Data Protection)
 * 
 * Предоставляет функциональность:
 * - Шифрование данных
 * - Маскирование персональных данных
 * - GDPR compliance
 * - Управление ключами шифрования
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties
class LogiDataProtectionApplication

fun main(args: Array<String>) {
    runApplication<LogiDataProtectionApplication>(*args)
}
