package com

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * Главный класс приложения SCM-Service
 * Центральный узел безопасности для всей экосистемы Nodeorb
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
@EnableCaching
class ScmApplication

fun main(args: Array<String>) {
    runApplication<ScmApplication>(*args) {
        // Настройка логирования
        setBannerMode(org.springframework.boot.Banner.Mode.OFF)
        
        // Настройка профилей
        setAdditionalProfiles("scm-service")
    }
}