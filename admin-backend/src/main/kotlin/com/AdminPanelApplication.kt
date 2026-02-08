package com

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
// import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling
import com.config.ServiceConfig

/**
 * Главный класс приложения административной панели Nodeorb
 *
 * @SpringBootApplication - Объединяет @Configuration, @EnableAutoConfiguration и @ComponentScan
 * @EnableDiscoveryClient - Регистрация сервиса в Service Discovery
 * @EnableJpaAuditing - Включение аудита JPA для автоматического заполнения полей createdAt, updatedAt
 * @EnableConfigurationProperties - Включение поддержки конфигурационных свойств
 * @EnableScheduling - Включение планирования задач
 */
@SpringBootApplication
// @EnableDiscoveryClient
@EnableJpaAuditing
@EnableConfigurationProperties(ServiceConfig::class)
@EnableScheduling
class AdminPanelApplication

fun main(args: Array<String>) {
    runApplication<AdminPanelApplication>(*args)
}
