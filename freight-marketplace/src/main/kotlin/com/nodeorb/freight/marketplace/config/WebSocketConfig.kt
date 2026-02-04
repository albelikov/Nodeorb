package com.nodeorb.freight.marketplace.config

import com.nodeorb.freight.marketplace.gateway.MarketplaceGateway
import com.nodeorb.freight.marketplace.gateway.AuthenticationHandshakeInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor
import org.springframework.web.socket.server.HandshakeFailureException
import org.springframework.web.socket.server.HandshakeHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import org.springframework.web.socket.server.support.OriginHandshakeInterceptor
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.cors.CorsUtils
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.socket.server.support.HandshakeFailureException
import java.util.*
import java.util.logging.Logger

/**
 * Конфигурация WebSocket для Marketplace Gateway
 * Настройка Socket.io интеграции и CORS
 */
@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {

    companion object {
        private val logger = Logger.getLogger(WebSocketConfig::class.java.name)
    }

    @Bean
    fun marketplaceGateway(): MarketplaceGateway {
        return MarketplaceGateway()
    }

    @Bean
    fun authenticationHandshakeInterceptor(): AuthenticationHandshakeInterceptor {
        return AuthenticationHandshakeInterceptor()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/ws/**", configuration)
        return source
    }

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        // Регистрация WebSocket endpoint
        registry.addHandler(marketplaceGateway(), "/ws/marketplace")
            .addInterceptors(
                authenticationHandshakeInterceptor(),
                HttpSessionHandshakeInterceptor(),
                OriginHandshakeInterceptor(),
                CorsHandshakeInterceptor()
            )
            .setAllowedOrigins("*")
            .withSockJS() // Поддержка SockJS для совместимости с Socket.io
            .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js")

        // Регистрация STOMP endpoint (если потребуется)
        registry.addHandler(marketplaceGateway(), "/ws/stomp")
            .addInterceptors(authenticationHandshakeInterceptor())
            .setAllowedOrigins("*")
    }
}

/**
 * CORS Handshake Interceptor для WebSocket
 */
class CorsHandshakeInterceptor : HandshakeInterceptor {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: org.springframework.web.socket.WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        // Добавляем CORS заголовки
        response.headers.add("Access-Control-Allow-Origin", "*")
        response.headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        response.headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-User-Id, X-User-Role")
        response.headers.add("Access-Control-Allow-Credentials", "true")
        
        // Обработка preflight запросов
        if (request.method?.name == "OPTIONS") {
            response.statusCode = HttpStatus.OK
            return false // Прерываем handshake для OPTIONS запросов
        }
        
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: org.springframework.web.socket.WebSocketHandler,
        exception: java.lang.Exception?
    ) {
        // Дополнительная логика после handshake
    }
}

/**
 * Аутентификационный Handshake Interceptor
 */
class AuthenticationHandshakeInterceptor : HandshakeInterceptor {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: org.springframework.web.socket.WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        try {
            // Извлечение аутентификационных данных из headers
            val headers = request.headers
            
            val authorizationHeader = headers.getFirst(HttpHeaders.AUTHORIZATION)
            val userId = headers.getFirst("X-User-Id")
            val userRole = headers.getFirst("X-User-Role")
            val clientType = headers.getFirst("X-Client-Type") ?: "web"
            
            // Проверка JWT токена (если используется)
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                val token = authorizationHeader.substring(7)
                if (!validateJwtToken(token)) {
                    throw HandshakeFailureException("Invalid JWT token")
                }
            }
            
            // Проверка обязательных headers
            if (userId == null || userRole == null) {
                throw HandshakeFailureException("Missing required headers: X-User-Id and X-User-Role")
            }
            
            // Валидация userId
            try {
                UUID.fromString(userId)
            } catch (e: IllegalArgumentException) {
                throw HandshakeFailureException("Invalid user ID format")
            }
            
            // Сохраняем аутентификационные данные в attributes
            attributes["userId"] = userId
            attributes["userRole"] = userRole
            attributes["clientType"] = clientType
            attributes["authenticated"] = true
            
            logger.info("WebSocket authentication successful for user $userId with role $userRole")
            return true
            
        } catch (e: HandshakeFailureException) {
            logger.severe("WebSocket authentication failed: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.severe("WebSocket authentication error: ${e.message}")
            throw HandshakeFailureException("Authentication error: ${e.message}")
        }
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: org.springframework.web.socket.WebSocketHandler,
        exception: java.lang.Exception?
    ) {
        // Логирование успешного подключения
        if (exception == null) {
            val userId = request.headers.getFirst("X-User-Id")
            logger.info("WebSocket connection established for user $userId")
        } else {
            logger.severe("WebSocket connection failed: ${exception.message}")
        }
    }

    /**
     * Валидация JWT токена (заглушка)
     */
    private fun validateJwtToken(token: String): Boolean {
        // Здесь должна быть логика валидации JWT токена
        // Для примера возвращаем true
        return true
    }
}

/**
 * JWT Authentication Handshake Handler
 */
class JwtAuthenticationHandshakeHandler : DefaultHandshakeHandler() {

    override fun determineUser(
        request: ServerHttpRequest,
        wsHandler: org.springframework.web.socket.WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): org.springframework.web.socket.User? {
        val userId = attributes["userId"] as? String
        val userRole = attributes["userRole"] as? String
        
        return if (userId != null && userRole != null) {
            org.springframework.web.socket.User(userId, setOf(userRole))
        } else {
            null
        }
    }
}

/**
 * WebSocket Security Configuration
 */
@Configuration
class WebSocketSecurityConfig {

    @Bean
    fun jwtAuthenticationHandshakeHandler(): JwtAuthenticationHandshakeHandler {
        return JwtAuthenticationHandshakeHandler()
    }
}

/**
 * WebSocket Connection Manager
 */
@Component
class WebSocketConnectionManager {

    companion object {
        private val logger = Logger.getLogger(WebSocketConnectionManager::class.java.name)
    }

    @Autowired
    private lateinit var marketplaceGateway: MarketplaceGateway

    /**
     * Проверка активности соединения
     */
    fun isConnectionActive(sessionId: String): Boolean {
        return marketplaceGateway.sessions.containsKey(sessionId)
    }

    /**
     * Получение количества активных соединений
     */
    fun getActiveConnectionsCount(): Int {
        return marketplaceGateway.sessions.size
    }

    /**
     * Получение количества подписчиков на заказ
     */
    fun getSubscribersCount(orderId: UUID): Int {
        return marketplaceGateway.getConnectedClientsCount(orderId)
    }

    /**
     * Проверка подключения пользователя к заказу
     */
    fun isUserSubscribed(userId: UUID, orderId: UUID): Boolean {
        return marketplaceGateway.isUserConnectedToOrder(userId, orderId)
    }

    /**
     * Закрытие всех соединений (для административных целей)
     */
    fun closeAllConnections() {
        marketplaceGateway.closeAllSessions()
        logger.info("All WebSocket connections closed")
    }
}

/**
 * WebSocket Metrics
 */
@Component
class WebSocketMetrics {

    companion object {
        private val logger = Logger.getLogger(WebSocketMetrics::class.java.name)
    }

    @Autowired
    private lateinit var connectionManager: WebSocketConnectionManager

    /**
     * Получение метрик WebSocket соединений
     */
    fun getConnectionMetrics(): Map<String, Any> {
        return mapOf(
            "activeConnections" to connectionManager.getActiveConnectionsCount(),
            "totalSubscriptions" to getTotalSubscriptions(),
            "averageConnectionsPerOrder" to getAverageConnectionsPerOrder(),
            "timestamp" to java.time.LocalDateTime.now()
        )
    }

    private fun getTotalSubscriptions(): Int {
        // Здесь должна быть логика подсчета общего количества подписок
        return 0
    }

    private fun getAverageConnectionsPerOrder(): Double {
        val activeConnections = connectionManager.getActiveConnectionsCount()
        val totalOrders = 0 // Здесь должна быть логика подсчета заказов
        return if (totalOrders > 0) activeConnections.toDouble() / totalOrders else 0.0
    }
}