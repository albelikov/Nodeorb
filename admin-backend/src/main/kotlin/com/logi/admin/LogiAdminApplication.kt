package com.logi.admin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * Главный класс приложения административной панели Logi
 * 
 * @SpringBootApplication - Объединяет @Configuration, @EnableAutoConfiguration и @ComponentScan
 * @EnableDiscoveryClient - Регистрация сервиса в Service Discovery
 * @EnableJpaAuditing - Включение аудита JPA для автоматического заполнения полей createdAt, updatedAt
 * @EnableConfigurationProperties - Включение поддержки конфигурационных свойств
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableConfigurationProperties
class LogiAdminApplication

fun main(args: Array<String>) {
    runApplication<LogiAdminApplication>(*args)
}
