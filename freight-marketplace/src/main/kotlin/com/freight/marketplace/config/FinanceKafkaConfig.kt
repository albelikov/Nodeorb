package com.freight.marketplace.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import java.util.*

/**
 * Конфигурация Kafka для финансовых операций
 */
@Configuration
@EnableKafka
class FinanceKafkaConfig {

    companion object {
        const val ESCROW_LOCKED_TOPIC = "finance.escrow.locked"
        const val ESCROW_RELEASED_TOPIC = "finance.escrow.released"
        const val ESCROW_CONFIRMED_TOPIC = "finance.escrow.confirmed"
        const val ESCROW_IN_TRANSIT_TOPIC = "finance.escrow.in_transit"
        const val ESCROW_DISPUTED_TOPIC = "finance.escrow.disputed"
        const val GROUP_ID = "finance-service"
    }

    @Bean
    fun financeProducerFactory(): ProducerFactory<String, Any> {
        val configProps = HashMap<String, Any>()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        configProps[ProducerConfig.ACKS_CONFIG] = "all"
        configProps[ProducerConfig.RETRIES_CONFIG] = 3
        configProps[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = true
        configProps[ProducerConfig.COMPRESSION_TYPE_CONFIG] = "lz4"
        configProps[ProducerConfig.BATCH_SIZE_CONFIG] = 16384
        configProps[ProducerConfig.LINGER_MS_CONFIG] = 5
        
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun financeKafkaTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(financeProducerFactory())
    }

    @Bean
    fun financeConsumerFactory(): ConsumerFactory<String, Any> {
        val configProps = HashMap<String, Any>()
        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        configProps[ConsumerConfig.GROUP_ID_CONFIG] = GROUP_ID
        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        configProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        configProps[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false // Ручное подтверждение
        configProps[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 100
        configProps[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = 30000
        configProps[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] = 3000
        
        return DefaultKafkaConsumerFactory(
            configProps,
            StringDeserializer(),
            JsonDeserializer<Any>(Any::class.java, false)
        )
    }

    @Bean
    fun financeKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = financeConsumerFactory()
        factory.containerProperties.ackMode = org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.pollTimeout = 3000
        factory.setConcurrency(3)
        return factory
    }
}