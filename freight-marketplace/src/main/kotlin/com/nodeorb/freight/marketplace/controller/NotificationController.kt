package com.nodeorb.freight.marketplace.controller

import com.nodeorb.freight.marketplace.service.notification.NotificationRouterService
import com.nodeorb.freight.marketplace.service.notification.NotificationSender
import com.nodeorb.freight.marketplace.service.notification.Priority
import com.nodeorb.freight.marketplace.service.notification.Channel
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST контроллер для управления уведомлениями
 */
@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationSender: NotificationSender,
    private val notificationRouterService: NotificationRouterService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(NotificationController::class.java)
    }

    /**
     * Отправка тестового уведомления
     * @param userId ID пользователя
     * @param priority Приоритет (P0, P1, P2)
     * @param channels Каналы (PUSH, SMS, IN_APP)
     * @param templateKey Ключ шаблона
     * @param templateData Данные для шаблона
     * @return Результат отправки
     */
    @PostMapping("/send-test")
    fun sendTestNotification(
        @RequestParam userId: UUID,
        @RequestParam priority: Priority,
        @RequestParam channels: Set<Channel>,
        @RequestParam templateKey: String,
        @RequestBody(required = false) templateData: Map<String, Any> = emptyMap()
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Sending test notification to user: $userId, priority: $priority, channels: $channels")
        
        try {
            notificationSender.sendTestNotification(
                userId = userId,
                priority = priority,
                channels = channels,
                templateKey = templateKey,
                templateData = templateData
            )
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "userId" to userId,
                "priority" to priority.name,
                "channels" to channels.map { it.name },
                "templateKey" to templateKey,
                "templateData" to templateData,
                "message" to "Test notification sent successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error sending test notification to user: $userId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "userId" to userId,
                "error" to e.message
            ))
        }
    }

    /**
     * Получение статистики по уведомлениям
     * @return Статистика отправленных уведомлений
     */
    @GetMapping("/statistics")
    fun getNotificationStatistics(): ResponseEntity<Map<String, Any>> {
        logger.info("Getting notification statistics")
        
        try {
            val statistics = notificationSender.getNotificationStatistics()
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "statistics" to statistics,
                "message" to "Notification statistics retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting notification statistics", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }

    /**
     * Проверка доступности каналов для пользователя
     * @param userId ID пользователя
     * @param channels Запрашиваемые каналы
     * @return Доступные каналы
     */
    @GetMapping("/available-channels/{userId}")
    fun getAvailableChannels(
        @PathVariable userId: UUID,
        @RequestParam channels: Set<Channel>
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Checking available channels for user: $userId, requested: $channels")
        
        try {
            val availableChannels = notificationSender.getAvailableChannels(userId, channels)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "userId" to userId,
                "requestedChannels" to channels.map { it.name },
                "availableChannels" to availableChannels.map { it.name },
                "message" to "Available channels checked successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error checking available channels for user: $userId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "userId" to userId,
                "error" to e.message
            ))
        }
    }

    /**
     * Отправка уведомления с учетом настроек пользователя
     * @param userId ID пользователя
     * @param priority Приоритет
     * @param channels Каналы
     * @param templateKey Ключ шаблона
     * @param templateData Данные для шаблона
     * @return Результат отправки
     */
    @PostMapping("/send-with-preferences")
    fun sendWithUserPreferences(
        @RequestParam userId: UUID,
        @RequestParam priority: Priority,
        @RequestParam channels: Set<Channel>,
        @RequestParam templateKey: String,
        @RequestBody(required = false) templateData: Map<String, Any> = emptyMap()
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Sending notification with user preferences to user: $userId")
        
        try {
            val notification = com.nodeorb.freight.marketplace.service.notification.Notification(
                userId = userId,
                priority = priority,
                channels = channels,
                templateKey = templateKey,
                templateData = templateData,
                createdAt = java.time.LocalDateTime.now()
            )
            
            notificationSender.sendWithUserPreferences(notification)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "userId" to userId,
                "priority" to priority.name,
                "templateKey" to templateKey,
                "message" to "Notification sent with user preferences"
            ))
        } catch (e: Exception) {
            logger.error("Error sending notification with user preferences to user: $userId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "userId" to userId,
                "error" to e.message
            ))
        }
    }

    /**
     * Получение списка всех доступных шаблонов
     * @return Список шаблонов
     */
    @GetMapping("/templates")
    fun getAllTemplates(): ResponseEntity<Map<String, Any>> {
        logger.info("Getting all available templates")
        
        try {
            // В реальной системе здесь будет вызов TemplateManager
            val templates = mapOf(
                "order.cancelled" to "Заказ {orderId} отменен. Средства возвращены на ваш баланс.",
                "order.awarded" to "Поздравляем! Ваша ставка на заказ {orderId} принята. Заказ передан в работу.",
                "order.progress.update" to "Заказ {orderId}: обновлен статус выполнения - {progress}.",
                "bid.placed" to "Ваша ставка {bidId} на заказ {orderId} успешно размещена.",
                "bid.awarded" to "Поздравляем! Ваша ставка {bidId} на заказ {orderId} выиграна. Средства заблокированы.",
                "bid.rejected" to "К сожалению, ваша ставка {bidId} на заказ {orderId} не была принята.",
                "bid.sniper.detected" to "ВНИМАНИЕ! Обнаружена снайперская ставка {bidId} на заказ {orderId}. Требуется срочное решение.",
                "escrow.locked" to "Средства в размере {amount} заблокированы на эскроу-счете (контракт {contractId}).",
                "escrow.funding.confirmed" to "Финансирование подтверждено. Средства {amount} на эскроу-счете готовы к использованию (контракт {contractId}).",
                "escrow.released" to "Средства {amount} освобождены со счета (контракт {contractId}). Операция завершена успешно.",
                "escrow.disputed" to "СПОР! Средства {amount} на счете (контракт {contractId}) заморожены. Причина: {reason}.",
                "marketing.promo" to "Специальное предложение! Новые заказы доступны по выгодным ценам.",
                "marketing.news" to "Новости платформы: {news}. Подробнее в разделе Новости.",
                "marketing.reminder" to "Напоминаем: у вас есть неактивные предложения. Проверьте статус ваших ставок."
            )
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "templates" to templates,
                "count" to templates.size,
                "message" to "Templates retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting templates", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }

    /**
     * Проверка валидации данных для шаблона
     * @param templateKey Ключ шаблона
     * @param templateData Данные для проверки
     * @return Результат валидации
     */
    @PostMapping("/validate-template")
    fun validateTemplateData(
        @RequestParam templateKey: String,
        @RequestBody templateData: Map<String, Any>
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Validating template data for template: $templateKey")
        
        try {
            // В реальной системе здесь будет вызов TemplateManager.validateTemplateData()
            val placeholders = when (templateKey) {
                "order.cancelled" -> listOf("orderId")
                "order.awarded" -> listOf("orderId")
                "order.progress.update" -> listOf("orderId", "progress")
                "bid.placed" -> listOf("bidId", "orderId")
                "bid.awarded" -> listOf("bidId", "orderId")
                "bid.rejected" -> listOf("bidId", "orderId")
                "bid.sniper.detected" -> listOf("bidId", "orderId")
                "escrow.locked" -> listOf("contractId", "amount")
                "escrow.funding.confirmed" -> listOf("contractId", "amount")
                "escrow.released" -> listOf("contractId", "amount")
                "escrow.disputed" -> listOf("contractId", "reason")
                else -> emptyList()
            }
            
            val missingPlaceholders = placeholders.filter { !templateData.containsKey(it) }
            val isValid = missingPlaceholders.isEmpty()
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "templateKey" to templateKey,
                "placeholders" to placeholders,
                "providedData" to templateData.keys.toList(),
                "missingPlaceholders" to missingPlaceholders,
                "isValid" to isValid,
                "message" to if (isValid) "Template data is valid" else "Template data is invalid"
            ))
        } catch (e: Exception) {
            logger.error("Error validating template data for template: $templateKey", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "templateKey" to templateKey,
                "error" to e.message
            ))
        }
    }

    /**
     * Форматирование сообщения по шаблону
     * @param templateKey Ключ шаблона
     * @param templateData Данные для шаблона
     * @return Отформатированное сообщение
     */
    @PostMapping("/format-message")
    fun formatMessage(
        @RequestParam templateKey: String,
        @RequestBody templateData: Map<String, Any>
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Formatting message for template: $templateKey")
        
        try {
            // В реальной системе здесь будет вызов TemplateManager.formatMessageSafe()
            val templates = mapOf(
                "order.cancelled" to "Заказ {orderId} отменен. Средства возвращены на ваш баланс.",
                "order.awarded" to "Поздравляем! Ваша ставка на заказ {orderId} принята. Заказ передан в работу.",
                "order.progress.update" to "Заказ {orderId}: обновлен статус выполнения - {progress}.",
                "bid.placed" to "Ваша ставка {bidId} на заказ {orderId} успешно размещена.",
                "bid.awarded" to "Поздравляем! Ваша ставка {bidId} на заказ {orderId} выиграна. Средства заблокированы.",
                "bid.rejected" to "К сожалению, ваша ставка {bidId} на заказ {orderId} не была принята.",
                "bid.sniper.detected" to "ВНИМАНИЕ! Обнаружена снайперская ставка {bidId} на заказ {orderId}. Требуется срочное решение.",
                "escrow.locked" to "Средства в размере {amount} заблокированы на эскроу-счете (контракт {contractId}).",
                "escrow.funding.confirmed" to "Финансирование подтверждено. Средства {amount} на эскроу-счете готовы к использованию (контракт {contractId}).",
                "escrow.released" to "Средства {amount} освобождены со счета (контракт {contractId}). Операция завершена успешно.",
                "escrow.disputed" to "СПОР! Средства {amount} на счете (контракт {contractId}) заморожены. Причина: {reason}."
            )
            
            val template = templates[templateKey] ?: "Шаблон сообщения не найден: $templateKey"
            var formattedMessage = template
            
            templateData.forEach { (key, value) ->
                val placeholder = "{$key}"
                formattedMessage = formattedMessage.replace(placeholder, value.toString())
            }
            
            formattedMessage = formattedMessage.replace(Regex("\\{[^}]+\\}"), "N/A")
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "templateKey" to templateKey,
                "template" to template,
                "templateData" to templateData,
                "formattedMessage" to formattedMessage,
                "message" to "Message formatted successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error formatting message for template: $templateKey", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "templateKey" to templateKey,
                "error" to e.message
            ))
        }
    }

    /**
     * Получение информации о системе уведомлений
     * @return Информация о системе
     */
    @GetMapping("/system-info")
    fun getSystemInfo(): ResponseEntity<Map<String, Any>> {
        logger.info("Getting notification system info")
        
        try {
            val statistics = notificationSender.getNotificationStatistics()
            
            val systemInfo = mapOf(
                "notificationSystem" to mapOf(
                    "status" to "active",
                    "version" to "1.0.0",
                    "kafkaTopics" to listOf("order.events", "bid.events", "finance.events")
                ),
                "priorities" to mapOf(
                    "P0" to "Critical: Арбитраж, снайперские ставки, отмена заказа",
                    "P1" to "Important: Новая ставка, подтверждение фонда",
                    "P2" to "Normal: Изменение прогресс-бара, маркетинг"
                ),
                "channels" to mapOf(
                    "PUSH" to "Push-уведомления на устройства",
                    "SMS" to "SMS-сообщения (эмуляция)",
                    "IN_APP" to "In-app уведомления в приложении"
                ),
                "statistics" to statistics
            )
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "systemInfo" to systemInfo,
                "message" to "System info retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting system info", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
}