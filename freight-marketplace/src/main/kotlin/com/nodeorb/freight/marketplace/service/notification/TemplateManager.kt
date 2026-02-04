package com.nodeorb.freight.marketplace.service.notification

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

/**
 * Менеджер шаблонов сообщений
 * Реализует простую систему шаблонов для формирования человекочитаемых сообщений
 */
@Service
class TemplateManager {

    companion object {
        private val logger = LoggerFactory.getLogger(TemplateManager::class.java)
    }

    /**
     * Шаблоны сообщений для разных типов событий
     */
    private val templates = mapOf(
        // Заказы
        "order.cancelled" to "Заказ {orderId} отменен. Средства возвращены на ваш баланс.",
        "order.awarded" to "Поздравляем! Ваша ставка на заказ {orderId} принята. Заказ передан в работу.",
        "order.progress.update" to "Заказ {orderId}: обновлен статус выполнения - {progress}.",
        
        // Ставки
        "bid.placed" to "Ваша ставка {bidId} на заказ {orderId} успешно размещена.",
        "bid.awarded" to "Поздравляем! Ваша ставка {bidId} на заказ {orderId} выиграна. Средства заблокированы.",
        "bid.rejected" to "К сожалению, ваша ставка {bidId} на заказ {orderId} не была принята.",
        "bid.sniper.detected" to "ВНИМАНИЕ! Обнаружена снайперская ставка {bidId} на заказ {orderId}. Требуется срочное решение.",
        
        // Финансы
        "escrow.locked" to "Средства в размере {amount} заблокированы на эскроу-счете (контракт {contractId}).",
        "escrow.funding.confirmed" to "Финансирование подтверждено. Средства {amount} на эскроу-счете готовы к использованию (контракт {contractId}).",
        "escrow.released" to "Средства {amount} освобождены со счета (контракт {contractId}). Операция завершена успешно.",
        "escrow.disputed" to "СПОР! Средства {amount} на счете (контракт {contractId}) заморожены. Причина: {reason}.",
        
        // Маркетинг
        "marketing.promo" to "Специальное предложение! Новые заказы доступны по выгодным ценам.",
        "marketing.news" to "Новости платформы: {news}. Подробнее в разделе Новости.",
        "marketing.reminder" to "Напоминаем: у вас есть неактивные предложения. Проверьте статус ваших ставок."
    )

    /**
     * Форматирует сообщение по шаблону
     * @param templateKey Ключ шаблона
     * @param data Данные для подстановки
     * @return Отформатированное сообщение
     */
    fun formatMessage(templateKey: String, data: Map<String, Any>): String {
        val template = templates[templateKey]
            ?: return "Шаблон сообщения не найден: $templateKey"
        
        var formattedMessage = template
        
        // Заменяем плейсхолдеры на реальные значения
        data.forEach { (key, value) ->
            val placeholder = "{$key}"
            formattedMessage = formattedMessage.replace(placeholder, value.toString())
        }
        
        // Заменяем оставшиеся плейсхолдеры на "N/A"
        formattedMessage = formattedMessage.replace(Regex("\\{[^}]+\\}"), "N/A")
        
        logger.debug("Formatted message: $templateKey -> $formattedMessage")
        return formattedMessage
    }

    /**
     * Получает шаблон по ключу
     * @param templateKey Ключ шаблона
     * @return Шаблон или null, если не найден
     */
    fun getTemplate(templateKey: String): String? {
        return templates[templateKey]
    }

    /**
     * Проверяет, существует ли шаблон
     * @param templateKey Ключ шаблона
     * @return true, если шаблон существует
     */
    fun hasTemplate(templateKey: String): Boolean {
        return templates.containsKey(templateKey)
    }

    /**
     * Получает все доступные шаблоны
     * @return Map с ключами и шаблонами
     */
    fun getAllTemplates(): Map<String, String> {
        return templates.toMap()
    }

    /**
     * Добавляет новый шаблон (для администраторов)
     * @param templateKey Ключ шаблона
     * @param template Текст шаблона
     */
    fun addTemplate(templateKey: String, template: String) {
        logger.info("Adding new template: $templateKey")
        // В реальной системе здесь может быть проверка прав доступа
        // templates += templateKey to template
    }

    /**
     * Обновляет существующий шаблон (для администраторов)
     * @param templateKey Ключ шаблона
     * @param template Новый текст шаблона
     */
    fun updateTemplate(templateKey: String, template: String) {
        logger.info("Updating template: $templateKey")
        // В реальной системе здесь может быть проверка прав доступа
        // if (templates.containsKey(templateKey)) {
        //     templates += templateKey to template
        // }
    }

    /**
     * Удаляет шаблон (для администраторов)
     * @param templateKey Ключ шаблона
     */
    fun removeTemplate(templateKey: String) {
        logger.info("Removing template: $templateKey")
        // В реальной системе здесь может быть проверка прав доступа
        // templates -= templateKey
    }

    /**
     * Получает список всех доступных плейсхолдеров для шаблона
     * @param templateKey Ключ шаблона
     * @return Список плейсхолдеров
     */
    fun getPlaceholders(templateKey: String): List<String> {
        val template = templates[templateKey] ?: return emptyList()
        val placeholderPattern = Regex("\\{([^}]+)\\}")
        return placeholderPattern.findAll(template).map { it.groupValues[1] }.toList()
    }

    /**
     * Проверяет, все ли необходимые данные присутствуют для шаблона
     * @param templateKey Ключ шаблона
     * @param data Данные для проверки
     * @return true, если все данные присутствуют
     */
    fun validateTemplateData(templateKey: String, data: Map<String, Any>): Boolean {
        val placeholders = getPlaceholders(templateKey)
        return placeholders.all { data.containsKey(it) }
    }

    /**
     * Форматирует сообщение с валидацией данных
     * @param templateKey Ключ шаблона
     * @param data Данные для подстановки
     * @return Отформатированное сообщение или сообщение об ошибке
     */
    fun formatMessageSafe(templateKey: String, data: Map<String, Any>): String {
        if (!hasTemplate(templateKey)) {
            return "Ошибка: шаблон '$templateKey' не найден"
        }
        
        if (!validateTemplateData(templateKey, data)) {
            val missingPlaceholders = getPlaceholders(templateKey).filter { !data.containsKey(it) }
            return "Ошибка: отсутствуют данные для плейсхолдеров: ${missingPlaceholders.joinToString(", ")}"
        }
        
        return formatMessage(templateKey, data)
    }
}