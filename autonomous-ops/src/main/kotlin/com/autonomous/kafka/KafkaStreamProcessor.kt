package com.autonomous.kafka

import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaStreamProcessor(
    @Value("\${spring.kafka.bootstrap-servers:localhost:9092}")
    private val bootstrapServers: String
) {

    private val messageBuffer = mutableListOf<String>()

    @KafkaListener(topics = ["logistics-events"], groupId = "autonomous-ops-group")
    fun processLogisticsEvent(event: String) {
        messageBuffer.add(event)
        println("Processed logistics event: $event")
    }

    @KafkaListener(topics = ["fleet-events"], groupId = "autonomous-ops-group")
    fun processFleetEvent(event: String) {
        messageBuffer.add(event)
        println("Processed fleet event: $event")
    }

    @KafkaListener(topics = ["weather-events"], groupId = "autonomous-ops-group")
    fun processWeatherEvent(event: String) {
        messageBuffer.add(event)
        println("Processed weather event: $event")
    }

    fun getMessageBuffer(): List<String> {
        return messageBuffer.toList()
    }

    fun clearBuffer() {
        messageBuffer.clear()
    }

    fun getBufferSize(): Int {
        return messageBuffer.size
    }
}