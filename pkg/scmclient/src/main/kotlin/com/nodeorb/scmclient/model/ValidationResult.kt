package com.nodeorb.scmclient.model

/**
 * Результат валідації вартості операції
 */
data class ValidationResult(
    /**
     * Чи дозволена операція
     */
    val allowed: Boolean,
    
    /**
     * Причина рішення
     */
    val reason: String,
    
    /**
     * Рівень ризику (0.0 - 100.0)
     */
    val riskScore: Double,
    
    /**
     * Ідентифікатор політики, яка була застосована
     */
    val policyId: String?,
    
    /**
     * Чи SCM був недоступний під час перевірки
     */
    val scmOffline: Boolean = false
)

/**
 * Контекст для валідації вартості
 */
data class ValidationContext(
    /**
     * Ідентифікатор користувача
     */
    val userId: String,
    
    /**
     * Ідентифікатор замовлення
     */
    val orderId: String,
    
    /**
     * Категорія операції
     */
    val category: String,
    
    /**
     * Вартість операції
     */
    val value: Double,
    
    /**
     * Широта
     */
    val latitude: Double,
    
    /**
     * Довгота
     */
    val longitude: Double,
    
    /**
     * Додатковий контекст
     */
    val additionalContext: Map<String, String> = emptyMap()
)

/**
 * Запит на подання апеляції
 */
data class AppealRequest(
    /**
     * Хеш запису, що апелюється
     */
    val recordHash: String,
    
    /**
     * Текст апеляції
     */
    val text: String,
    
    /**
     * URL доказів
     */
    val evidenceUrl: String,
    
    /**
     * Додатковий контекст
     */
    val additionalContext: Map<String, String> = emptyMap()
)

/**
 * Запит на перевірку доступу
 */
data class AccessCheckRequest(
    /**
     * Ідентифікатор користувача
     */
    val userId: String,
    
    /**
     * Дія, яку потрібно перевірити
     */
    val action: String,
    
    /**
     * Контекст перевірки
     */
    val context: Map<String, String>
)

/**
 * Результат перевірки доступу
 */
data class AccessCheckResult(
    /**
     * Чи дозволена дія
     */
    val allowed: Boolean,
    
    /**
     * Причина рішення
     */
    val reason: String,
    
    /**
     * Ідентифікатор політики, яка була застосована
     */
    val policyId: String?,
    
    /**
     * Чи SCM був недоступний під час перевірки
     */
    val scmOffline: Boolean = false
)