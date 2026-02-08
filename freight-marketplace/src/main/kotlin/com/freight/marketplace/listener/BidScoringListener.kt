package com.freight.marketplace.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.freight.marketplace.dto.BidScoringEvent
import com.freight.marketplace.dto.BidScoringResult
import com.freight.marketplace.dto.ScoringStatus
import com.freight.marketplace.service.ScoringService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service

/**
 * Слушатель Kafka для асинхронного расчета скоринга ставок
 * Обрабатывает события BidScoringEvent и публикует результаты
 */
@Service
class BidScoringListener(
    private val scoringService: ScoringService,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val logger = LoggerFactory.getLogger(BidScoringListener::class.java)
        private const val BID_SCORING_TOPIC = "bid-scoring-events"
        private const val BID_SCORING_RESULT_TOPIC = "bid-scoring-results"
    }

    /**
     * Обрабатывает событие расчета скоринга ставки
     */
    @KafkaListener(
        topics = [BID_SCORING_TOPIC],
        groupId = "scoring-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleBidScoringEvent(
        record: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.info("Received bid scoring event: ${record.value()}")
            
            val event = objectMapper.readValue(record.value(), BidScoringEvent::class.java)
            
            // Выполняем расчет скоринга
            val result = processBidScoring(event)
            
            // Публикуем результат
            kafkaTemplate.send(BID_SCORING_RESULT_TOPIC, event.bidId.toString(), result)
            
            // Подтверждаем обработку
            acknowledgment.acknowledge()
            
            logger.info("Successfully processed bid scoring for bid: ${event.bidId}")
            
        } catch (e: Exception) {
            logger.error("Error processing bid scoring event: ${record.value()}", e)
            
            // В случае ошибки создаем событие с ошибкой
            val errorResult = BidScoringResult(
                bidId = record.key()?.let { java.util.UUID.fromString(it) } ?: java.util.UUID.randomUUID(),
                matchingScore = 0.0,
                scoreBreakdown = "{}",
                status = ScoringStatus.FAILED,
                errorMessage = e.message
            )
            
            kafkaTemplate.send(BID_SCORING_RESULT_TOPIC, errorResult)
            acknowledgment.acknowledge()
        }
    }

    /**
     * Выполняет расчет скоринга для ставки
     */
    private fun processBidScoring(event: BidScoringEvent): BidScoringResult {
        return try {
            // Вызываем сервис для расчета и сохранения скоринга
            scoringService.calculateAndSaveScore(event.bidId)
            
            // Получаем обновленную ставку для извлечения результатов
            // В реальной системе здесь будет вызов репозитория для получения обновленной ставки
            // Пока возвращаем заглушку с успешным статусом
            
            BidScoringResult(
                bidId = event.bidId,
                matchingScore = 85.0, // Заглушка - в реальной системе будет реальный балл
                scoreBreakdown = "{}", // Заглушка - в реальной системе будет JSON с детализацией
                status = ScoringStatus.SUCCESS
            )
            
        } catch (e: Exception) {
            logger.error("Failed to calculate scoring for bid: ${event.bidId}", e)
            
            BidScoringResult(
                bidId = event.bidId,
                matchingScore = 0.0,
                scoreBreakdown = "{}",
                status = ScoringStatus.FAILED,
                errorMessage = e.message
            )
        }
    }
}