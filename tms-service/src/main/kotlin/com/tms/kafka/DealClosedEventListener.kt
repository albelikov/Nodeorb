package com.tms.kafka

import com.tms.dto.DealClosedEvent
import com.tms.service.ShipmentService
import com.tms.config.KafkaConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class DealClosedEventListener(
    private val shipmentService: ShipmentService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(DealClosedEventListener::class.java)
    }

    @KafkaListener(
        topics = [KafkaConfig.DEAL_CLOSED_TOPIC],
        groupId = KafkaConfig.GROUP_ID,
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleDealClosedEvent(
        record: ConsumerRecord<String, Any>,
        acknowledgment: Acknowledgment
    ) {
        logger.info("Received deal closed event: ${record.key()} -> ${record.value()}")
        try {
            // Приводим событие к нужному типу
            val dealClosedEvent = when (val value = record.value()) {
                is DealClosedEvent -> value
                is Map<*, *> -> mapToDealClosedEvent(value)
                else -> {
                    logger.error("Unknown event type: ${value::class.java}")
                    acknowledgment.acknowledge()
                    return
                }
            }

            // Создаем отгрузку
            shipmentService.createShipmentFromDeal(dealClosedEvent)
            logger.info("Shipment created from deal closed event: ${dealClosedEvent.orderId}")

            // Подтверждаем обработку сообщения
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            logger.error("Error processing deal closed event: ${record.value()}", e)
            // В случае ошибки можно выбрать стратегию (повтор, отправка в DLQ и т.д.)
            acknowledgment.acknowledge() // Пока просто подтверждаем обработку
        }
    }

    private fun mapToDealClosedEvent(map: Map<*, *>): DealClosedEvent {
        return DealClosedEvent(
            bidId = map["bidId"] as String,
            orderId = map["orderId"] as String,
            carrierId = map["carrierId"] as String,
            amount = (map["amount"] as Number).toDouble(),
            origin = map["origin"] as String,
            destination = map["destination"] as String,
            weight = (map["weight"] as Number).toDouble(),
            dimensions = com.tms.dto.DimensionsDto(
                length = (map["dimensions"] as Map<*, *>).let { (it["length"] as Number).toDouble() },
                width = (map["dimensions"] as Map<*, *>).let { (it["width"] as Number).toDouble() },
                height = (map["dimensions"] as Map<*, *>).let { (it["height"] as Number).toDouble() }
            ),
            timestamp = (map["timestamp"] as Number).toLong()
        )
    }
}