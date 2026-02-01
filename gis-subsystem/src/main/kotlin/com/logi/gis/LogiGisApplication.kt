package com.logi.gis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

/**
 * Геоинформационная подсистема (GIS)
 * 
 * Предоставляет функциональность:
 * - Геокодирование и обратное геокодирование
 * - Расчет маршрутов и оптимизация
 * - Управление геозонами
 * - Анализ пространственных данных
 * - Визуализация карт
 * - Импорт и экспорт геоданных
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties
class LogiGisApplication

fun main(args: Array<String>) {
    runApplication<LogiGisApplication>(*args)
}
