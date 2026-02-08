package com.nodeorb.scmclient.config

import java.time.Duration

/**
 * Конфігурація SCM Client
 */
data class SCMClientConfig(
    /**
     * Хост SCM сервісу
     */
    val host: String = "localhost",
    
    /**
     * Порт SCM сервісу
     */
    val port: Int = 9090,
    
    /**
     * Максимальна кількість спроб підключення
     */
    val maxRetries: Int = 3,
    
    /**
     * Час keep-alive в секундах
     */
    val keepAliveTime: Long = 30,
    
    /**
     * Таймаут keep-alive в секундах
     */
    val keepAliveTimeout: Long = 5,
    
    /**
     * Таймаут операції в секундах
     */
    val operationTimeout: Long = 30,
    
    /**
     * Стратегія поведінки при недоступності SCM
     * true - FAIL_OPEN (дозволяти дію), false - FAIL_CLOSED (блокувати)
     */
    val defaultAllowOnFailure: Boolean = true,
    
    /**
     * Включити логування
     */
    val enableLogging: Boolean = true,
    
    /**
     * Рівень логування (DEBUG, INFO, WARN, ERROR)
     */
    val logLevel: String = "INFO",
    
    /**
     * Налаштування Circuit Breaker
     */
    val circuitBreakerConfig: CircuitBreakerConfig = CircuitBreakerConfig()
)

/**
 * Конфігурація Circuit Breaker
 */
data class CircuitBreakerConfig(
    /**
     * Відсоток помилок для відкриття Circuit Breaker
     */
    val failureRateThreshold: Double = 50.0,
    
    /**
     * Мінімальна кількість викликів для аналізу
     */
    val minimumNumberOfCalls: Int = 5,
    
    /**
     * Час у мілісекундах, на який Circuit Breaker залишається у стані OPEN
     */
    val waitDurationInOpenState: Duration = Duration.ofSeconds(30),
    
    /**
     * Розмір вікна для обліку помилок
     */
    val slidingWindowSize: Int = 10,
    
    /**
     * Тип вікна (COUNT_BASED або TIME_BASED)
     */
    val slidingWindowType: String = "COUNT_BASED",
    
    /**
     * Включити Circuit Breaker
     */
    val enabled: Boolean = true
)