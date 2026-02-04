package com.nodeorb.freight.marketplace.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.logging.Logger

/**
 * Конфигурация для фоновых задач и планировщика
 */
@Configuration
@EnableScheduling
class SchedulingConfig : SchedulingConfigurer {

    companion object {
        private val logger = Logger.getLogger(SchedulingConfig::class.java.name)
    }

    /**
     * Настройка пула потоков для выполнения scheduled задач
     */
    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(4) { runnable ->
            val thread = Thread(runnable, "order-execution-scheduler")
            thread.isDaemon = true
            thread
        }
        
        taskRegistrar.setScheduler(scheduler)
        
        logger.info("Configured scheduled task executor with 4 threads")
    }
}

/**
 * Конфигурация параметров планировщика
 */
@Configuration
class SchedulerProperties {
    
    /**
     * Интервал проверки заполненности заказов (в минутах)
     */
    val orderFillingCheckInterval: Long = 15
    
    /**
     * Время предупреждения до погрузки (в часах)
     */
    val warningHoursBeforePickup: Long = 2
    
    /**
     * Порог заполненности для предупреждения (в процентах)
     */
    val warningThresholdPercentage: Double = 50.0
    
    /**
     * Интервал проверки просроченных заказов (в минутах)
     */
    val expiredOrdersCheckInterval: Long = 30
    
    /**
     * Интервал очистки просроченных токенов (в минутах)
     */
    val tokenCleanupInterval: Long = 60
}